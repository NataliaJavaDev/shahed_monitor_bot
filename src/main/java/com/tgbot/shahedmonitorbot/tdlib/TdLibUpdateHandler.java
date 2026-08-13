package com.tgbot.shahedmonitorbot.tdlib;

import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tgbot.shahedmonitorbot.config.AppProperties;
import com.tgbot.shahedmonitorbot.monitoring.source.ChatInfoService;
import com.tgbot.shahedmonitorbot.monitoring.source.MonitoredSourceService;
import com.tgbot.shahedmonitorbot.monitoring.source.UnknownSourceCandidateService;
// import com.tgbot.shahedmonitorbot.processing.AlertMessageFormatter;
import com.tgbot.shahedmonitorbot.processing.MessageAnalysis;
import com.tgbot.shahedmonitorbot.processing.MessageAnalysisService;
import com.tgbot.shahedmonitorbot.processing.MessageIntent;
import com.tgbot.shahedmonitorbot.processing.RecentMessageCacheService;
import com.tgbot.shahedmonitorbot.sender.AnalysisMessageFormatter;
import com.tgbot.shahedmonitorbot.sender.TelegramSenderService;
import com.tgbot.shahedmonitorbot.tdlib.history.TdHistoryMessage;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class TdLibUpdateHandler {

    private final ObjectMapper objectMapper;
    private final AppProperties appProperties;
    private final ChatInfoService chatInfoService;
    private final MonitoredSourceService monitoredSourceService;
    private final UnknownSourceCandidateService unknownSourceCandidateService;
    private final AnalysisMessageFormatter analysisMessageFormatter;
    // private final AlertMessageFormatter alertMessageFormatter;
    private final TelegramSenderService telegramSenderService;
    private final MessageAnalysisService messageAnalysisService;
    private final RecentMessageCacheService recentMessageCacheService;
    private final PendingPhotoMessageService pendingPhotoMessageService;
    private final TdLibClientService tdLibClientService;

    public TdLibUpdateHandler(
            ObjectMapper objectMapper,
            AppProperties appProperties,
            ChatInfoService chatInfoService,
            MonitoredSourceService monitoredSourceService,
            UnknownSourceCandidateService unknownSourceCandidateService,
            AnalysisMessageFormatter analysisMessageFormatter,
            // AlertMessageFormatter alertMessageFormatter,
            TelegramSenderService telegramSenderService,
            MessageAnalysisService messageAnalysisService,
            RecentMessageCacheService recentMessageCacheService,
            PendingPhotoMessageService pendingPhotoMessageService,
            TdLibClientService tdLibClientService
    ) {
        this.objectMapper = objectMapper;
        this.appProperties = appProperties;
        this.chatInfoService = chatInfoService;
        this.monitoredSourceService = monitoredSourceService;
        this.unknownSourceCandidateService = unknownSourceCandidateService;
        this.analysisMessageFormatter = analysisMessageFormatter;
        // this.alertMessageFormatter = alertMessageFormatter;
        this.telegramSenderService = telegramSenderService;
        this.messageAnalysisService = messageAnalysisService;
        this.recentMessageCacheService = recentMessageCacheService;
        this.pendingPhotoMessageService = pendingPhotoMessageService;
        this.tdLibClientService = tdLibClientService;
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

            if ("updateFile".equals(type)) {
                handleFileUpdate(root);
                return;
            }

            if (!"updateNewMessage".equals(type)) {
                return;
            }

            JsonNode message = root.path("message");

            String chatId = message.path("chat_id").asText();
            TdMessageContent content = extractContent(message);
            String text = content.text();

            if (chatId.equals(appProperties.telegram().targetChannelId())) {
                return;
            }

            recentMessageCacheService.add(
                chatId,
                new TdHistoryMessage(
                        message.path("id").asLong(),
                        LocalDateTime.ofInstant(
                                Instant.ofEpochSecond(
                                        message.path("date").asLong()
                                ),
                                ZoneId.of("Europe/Kyiv")
                        ),
                        text
                )
            );

            if (appProperties.monitor().ignoredChatIds() != null
                    && appProperties.monitor().ignoredChatIds().contains(chatId)) {
                return;
            }

            var source = monitoredSourceService.findByChatId(chatId);

            if (source == null) {
                unknownSourceCandidateService.register(
                        chatId,
                        chatInfoService.getTitle(chatId),
                        text
                );

                System.out.println("""
                        UNKNOWN CHAT DETECTED
                        CHAT_ID: %s
                        TITLE: %s
                        TEXT: %s
                        """.formatted(
                        chatId,
                        chatInfoService.getTitle(chatId),
                        text
                ));

                return;
            }

            unknownSourceCandidateService.remove(chatId);

            if (!source.active()) {
                return;
            }

            MessageAnalysis analysis = messageAnalysisService.analyze(chatId, text);

            if (analysis == null) {
                return;
            }

            if (analysis.duplicate()
                    && !canSendDuplicateUpdate(analysis.intent())) {
                return;
            }

            // telegramSenderService.sendToChat(
            //         appProperties.telegram().targetChannelId(),
            //         alertMessageFormatter.format(
            //                 source.title(),
            //                 text
            //         )
            // );

            if (content.photoFileId() != null) {
                pendingPhotoMessageService.save(
                        content.photoFileId(),
                        new PendingPhotoMessage(
                                chatId,
                                source.title(),
                                text,
                                analysis
                        )
                );

                tdLibClientService.downloadFile(content.photoFileId());

                return;
            }

            telegramSenderService.sendToChat(
                appProperties.telegram().targetChannelId(),
                analysisMessageFormatter.formatDebug(
                        analysis,
                        source.title(),
                        text
                )
            );

        } catch (Exception e) {
            System.out.println(
                    "Failed to handle TDLib update: " + e.getMessage()
            );
        }
    }

    private boolean canSendDuplicateUpdate(MessageIntent intent) {
        return switch (intent) {
            case COUNT_UPDATE,
                 ROUTE_UPDATE,
                 ATTENTION -> true;

            default -> false;
        };
    }

    private TdMessageContent extractContent(JsonNode message) {
        JsonNode content = message.path("content");

        String contentType = content.path("@type").asText();

        if ("messageText".equals(contentType)) {
            String text = content
                    .path("text")
                    .path("text")
                    .asText("");

            return new TdMessageContent(
                    text,
                    null
            );
        }

        if ("messagePhoto".equals(contentType)) {
            String text = content
                    .path("caption")
                    .path("text")
                    .asText("");

            JsonNode sizes = content
                    .path("photo")
                    .path("sizes");

            Integer photoFileId = findLargestPhotoFileId(sizes);

            return new TdMessageContent(
                    text,
                    photoFileId
            );
        }

        return new TdMessageContent(
                "",
                null
        );
    }

    private Integer findLargestPhotoFileId(JsonNode sizes) {
        if (!sizes.isArray() || sizes.isEmpty()) {
            return null;
        }

        JsonNode largestSize = null;
        long largestArea = -1;

        for (JsonNode size : sizes) {
            int width = size.path("width").asInt(0);
            int height = size.path("height").asInt(0);

            long area = (long) width * height;

            if (area > largestArea) {
                largestArea = area;
                largestSize = size;
            }
        }

        if (largestSize == null) {
            return null;
        }

        int fileId = largestSize
                .path("photo")
                .path("id")
                .asInt(0);

        return fileId > 0 ? fileId : null;
    }

    private void handleFileUpdate(JsonNode root) {
        JsonNode file = root.path("file");

        int fileId = file.path("id").asInt(0);

        if (fileId == 0) {
            return;
        }

        PendingPhotoMessage pending = pendingPhotoMessageService.get(fileId);

        if (pending == null) {
            return;
        }

        JsonNode local = file.path("local");

        boolean downloaded = local.path("is_downloading_completed").asBoolean(false);

        if (!downloaded) {
            return;
        }

        String localPath = local.path("path").asText("");

        if (localPath.isBlank()) {
            return;
        }

        pendingPhotoMessageService.remove(fileId);

        telegramSenderService.sendPhotoToChat(
                appProperties.telegram().targetChannelId(),
                localPath,
                analysisMessageFormatter.formatDebug(
                        pending.analysis(),
                        pending.sourceTitle(),
                        pending.originalText()
                )
        );
    }
}