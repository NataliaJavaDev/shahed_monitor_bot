package com.tgbot.shahedmonitorbot.tdlib;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tgbot.shahedmonitorbot.config.AppProperties;
import com.tgbot.shahedmonitorbot.monitoring.source.ChatInfoService;
import com.tgbot.shahedmonitorbot.monitoring.source.MonitoredSourceService;
import com.tgbot.shahedmonitorbot.monitoring.source.UnknownSourceCandidateService;
import com.tgbot.shahedmonitorbot.deduplication.DeduplicationService;
import com.tgbot.shahedmonitorbot.sender.TelegramSenderService;
import com.tgbot.shahedmonitorbot.context.EventContextService;
import org.springframework.stereotype.Service;
import com.tgbot.shahedmonitorbot.processing.MonitorFilterService;
import com.tgbot.shahedmonitorbot.processing.MonitorMatch;

import java.util.Optional;

@Service
public class TdLibUpdateHandler {

    private final ObjectMapper objectMapper;
    private final AppProperties appProperties;
    private final ChatInfoService chatInfoService;
    private final MonitoredSourceService monitoredSourceService;
    private final UnknownSourceCandidateService unknownSourceCandidateService;
    private final DeduplicationService deduplicationService;
    private final TelegramSenderService telegramSenderService;
    private final EventContextService eventContextService;
    private final MonitorFilterService monitorFilterService;

    public TdLibUpdateHandler(
            ObjectMapper objectMapper,
            AppProperties appProperties,
            ChatInfoService chatInfoService,
            MonitoredSourceService monitoredSourceService,
            UnknownSourceCandidateService unknownSourceCandidateService,
            DeduplicationService deduplicationService,
            TelegramSenderService telegramSenderService,
            EventContextService eventContextService,
            MonitorFilterService monitorFilterService
            
    ) {
        this.objectMapper = objectMapper;
        this.appProperties = appProperties;
        this.chatInfoService = chatInfoService;
        this.monitoredSourceService = monitoredSourceService;
        this.unknownSourceCandidateService = unknownSourceCandidateService;
        this.deduplicationService = deduplicationService;
        this.telegramSenderService = telegramSenderService;
        this.eventContextService = eventContextService;
        this.monitorFilterService = monitorFilterService;
    }

    public void handle(String update) {

        try {
            JsonNode root = objectMapper.readTree(update);

            String type = root.path("@type").asText();

            if ("updateNewChat".equals(type)) {
                JsonNode chat = root.path("chat");

                chatInfoService.saveTitle(
                        chat.path("id").asText(),
                        chat.path("title").asText()
                );

                return;
            }

            if ("updateChatTitle".equals(type)) {
                chatInfoService.saveTitle(
                        root.path("chat_id").asText(),
                        root.path("title").asText()
                );

                return;
            }

            if (!"updateNewMessage".equals(type)) {
                return;
            }

            JsonNode message = root.path("message");

            String chatId = message.path("chat_id").asText();
            String text = extractText(message);

            if (monitoredSourceService.isMonitored(chatId)) {

                unknownSourceCandidateService.remove(chatId);

            } else {

                if (chatId.equals(appProperties.telegram().targetChannelId())) {
                    return;
                }

                if (appProperties.monitor().ignoredChatIds() != null
                        && appProperties.monitor().ignoredChatIds().contains(chatId)) {
                    return;
                }

                unknownSourceCandidateService.register(
                        chatId,
                        chatInfoService.getTitle(chatId),
                        text
                );

                System.out.println("""
                        UNKNOWN CHAT DETECTED
                        CHAT_ID: %s
                        TEXT: %s
                        """.formatted(chatId, text));

                return;
            }

            var source = monitoredSourceService.findByChatId(chatId);

            Optional<MonitorMatch> match = monitorFilterService.findMatch(text);

            if (match.isEmpty()) {
                return;
            }

            MonitorMatch monitorMatch = match.get();

            if (deduplicationService.isDuplicate(monitorMatch)) {
                return;
            }

            eventContextService.save(monitorMatch);

            var currentContext = eventContextService.getLastEvent();

            String contextInfo = currentContext
                    .map(context -> "%s::%s".formatted(
                            formatNullable(context.targetCategory()),
                            formatNullable(context.locationCategory())
                    ))
                    .orElse("-");

            String messageToSend = """
                🧠 Аналіз повідомлення

                📡 Джерело: %s
                🆔 Chat ID: %s

                📂 Тип збігу:
                %s

                🎯 Знайдена ціль:
                %s

                🧩 Категорія цілі:
                %s

                🧭 Напрямок:
                %s

                📍 Знайдена локація:
                %s

                🧩 Категорія локації:
                %s

                🔑 Ключ антидубля:
                %s::%s

                🧠 Поточний контекст:
                %s

                💬 Оригінальне повідомлення:

                %s
                """.formatted(
                source.title(),
                chatId,
                monitorMatch.matchType().displayName(),
                formatNullable(monitorMatch.matchedTarget()),
                formatNullable(monitorMatch.targetCategory()),
                formatNullable(monitorMatch.direction()),
                formatNullable(monitorMatch.matchedLocation()),
                formatNullable(monitorMatch.locationCategory()),
                formatNullable(monitorMatch.targetCategory()),
                formatNullable(monitorMatch.locationCategory()),
                contextInfo,
                text
            );

            telegramSenderService.sendToChat(
                    appProperties.telegram().targetChannelId(),
                    messageToSend
            );

        } catch (Exception e) {
            System.out.println("Failed to handle TDLib update: " + e.getMessage());
        }
    }

    private String formatNullable(String value) {
        
        if (value == null || value.isBlank()) {
            return "-";
        }

        return value;
    }

    private String extractText(JsonNode message) {
        JsonNode content = message.path("content");

        String contentType = content.path("@type").asText();

        if ("messageText".equals(contentType)) {
            return content
                    .path("text")
                    .path("text")
                    .asText("");
        }

        if ("messagePhoto".equals(contentType)) {
            return content
                    .path("caption")
                    .path("text")
                    .asText("");
        }

        return "[unsupported message type: " + contentType + "]";
    }
}