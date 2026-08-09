package com.tgbot.shahedmonitorbot.tdlib;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tgbot.shahedmonitorbot.config.AppProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
public class TemporaryHistoryExportService {

    private static final String EXPORT_EXTRA_PREFIX = "HISTORY_EXPORT:";
    private static final DateTimeFormatter FILE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm");

    private final TdLibClientService tdLibClientService;
    private final AppProperties appProperties;
    private final ObjectMapper objectMapper;

    private final LocalDateTime from = LocalDateTime.of(2026, 8, 2, 0, 0);
    private final LocalDateTime to = LocalDateTime.of(2026, 8, 8, 0, 0);
    private final ZoneId zoneId = ZoneId.of("Europe/Kyiv");

    private final Path exportDirectory = Path.of("exports").resolve(
            "history_%s__%s".formatted(
                    from.format(FILE_FORMAT),
                    to.format(FILE_FORMAT)
            )
    );

    private final Path analysisDirectory = Path.of("exports").resolve("analysis");

    private boolean started = false;

    public TemporaryHistoryExportService(
            TdLibClientService tdLibClientService,
            AppProperties appProperties,
            ObjectMapper objectMapper
    ) {
        this.tdLibClientService = tdLibClientService;
        this.appProperties = appProperties;
        this.objectMapper = objectMapper;
    }

    public void startExport() {
        if (started) {
            return;
        }

        started = true;

        try {
            Files.createDirectories(exportDirectory);
            Files.createDirectories(analysisDirectory);
        } catch (IOException e) {
            System.out.println("Failed to prepare export directory: " + e.getMessage());
            return;
        }

        if (appProperties.monitor().sources() == null) {
            return;
        }

        appProperties.monitor().sources().stream()
                .filter(source -> Boolean.TRUE.equals(source.active()))
                .forEach(source -> requestHistory(source.chatId(), 0));

        System.out.println("History export started -> " + exportDirectory.toAbsolutePath());
    }

    public void handle(String update) {
        if (update == null || !update.contains("\"@type\":\"messages\"")) {
            return;
        }

        try {
            JsonNode root = objectMapper.readTree(update);
            String extra = root.path("@extra").asText("");

            if (!extra.startsWith(EXPORT_EXTRA_PREFIX)) {
                return;
            }

            String chatId = extra.substring(EXPORT_EXTRA_PREFIX.length());

            JsonNode messages = root.path("messages");

            if (!messages.isArray() || messages.isEmpty()) {
                return;
            }

            long oldestMessageId = 0;
            boolean shouldContinue = true;

            for (JsonNode message : messages) {
                long messageId = message.path("id").asLong();
                int unixDate = message.path("date").asInt();

                LocalDateTime messageTime = LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(unixDate),
                        zoneId
                );

                if (oldestMessageId == 0 || messageId < oldestMessageId) {
                    oldestMessageId = messageId;
                }

                if (messageTime.isBefore(from)) {
                    shouldContinue = false;
                    continue;
                }

                if (messageTime.isAfter(to)) {
                    continue;
                }

                String text = extractText(message);

                if (text.isBlank()) {
                    continue;
                }

                appendMessage(chatId, messageTime, text);
            }

            if (shouldContinue && oldestMessageId != 0) {
                requestHistory(chatId, oldestMessageId);
            }

        } catch (Exception e) {
            System.out.println("Failed to handle history export update: " + e.getMessage());
        }
    }

    private void requestHistory(String chatId, long fromMessageId) {
        tdLibClientService.send("""
                {
                  "@type": "getChatHistory",
                  "@extra": "%s%s",
                  "chat_id": %s,
                  "from_message_id": %d,
                  "offset": 0,
                  "limit": 100,
                  "only_local": false
                }
                """.formatted(
                EXPORT_EXTRA_PREFIX,
                chatId,
                chatId,
                fromMessageId
        ));
    }

    private String extractText(JsonNode message) {
        JsonNode content = message.path("content");
        String contentType = content.path("@type").asText();

        if ("messageText".equals(contentType)) {
            return content.path("text").path("text").asText("");
        }

        if ("messagePhoto".equals(contentType)) {
            return content.path("caption").path("text").asText("");
        }

        return "";
    }

    private void appendMessage(
            String chatId,
            LocalDateTime messageTime,
            String text
    ) throws IOException {
        Path filePath = exportDirectory.resolve(fileNameForChat(chatId));

        String block = """
                [%s]
                chatId=%s

                %s

                ----------------------------------------

                """.formatted(
                messageTime,
                chatId,
                text
        );

        Files.writeString(
                filePath,
                block,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
        );
    }

    private String fileNameForChat(String chatId) {
        return appProperties.monitor().sources().stream()
                .filter(source -> source.chatId().equals(chatId))
                .findFirst()
                .map(AppProperties.Source::title)
                .map(this::sanitizeFileName)
                .orElse(chatId) + ".txt";
    }

    private String sanitizeFileName(String value) {
        if (value == null || value.isBlank()) {
            return "unknown";
        }

        return value
                .replaceAll("[\\\\/:*?\"<>|]", "_")
                .replaceAll("\\s+", " ")
                .trim();
    }
}