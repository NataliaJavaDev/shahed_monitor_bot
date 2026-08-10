package com.tgbot.shahedmonitorbot.tdlib;

import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tgbot.shahedmonitorbot.config.AppProperties;
import com.tgbot.shahedmonitorbot.monitoring.source.ChatInfoService;
import com.tgbot.shahedmonitorbot.monitoring.source.MonitoredSourceService;
import com.tgbot.shahedmonitorbot.monitoring.source.UnknownSourceCandidateService;
import com.tgbot.shahedmonitorbot.processing.AlertMessageFormatter;
import com.tgbot.shahedmonitorbot.processing.MessageAnalysis;
import com.tgbot.shahedmonitorbot.processing.MessageAnalysisService;
import com.tgbot.shahedmonitorbot.processing.MessageIntent;
import com.tgbot.shahedmonitorbot.processing.RecentMessageCacheService;
// import com.tgbot.shahedmonitorbot.sender.AnalysisMessageFormatter;
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
    // private final AnalysisMessageFormatter analysisMessageFormatter;
    private final AlertMessageFormatter alertMessageFormatter;
    private final TelegramSenderService telegramSenderService;
    private final MessageAnalysisService messageAnalysisService;
    private final RecentMessageCacheService recentMessageCacheService;

    public TdLibUpdateHandler(
            ObjectMapper objectMapper,
            AppProperties appProperties,
            ChatInfoService chatInfoService,
            MonitoredSourceService monitoredSourceService,
            UnknownSourceCandidateService unknownSourceCandidateService,
            AlertMessageFormatter alertMessageFormatter,
            TelegramSenderService telegramSenderService,
            MessageAnalysisService messageAnalysisService,
            RecentMessageCacheService recentMessageCacheService
    ) {
        this.objectMapper = objectMapper;
        this.appProperties = appProperties;
        this.chatInfoService = chatInfoService;
        this.monitoredSourceService = monitoredSourceService;
        this.unknownSourceCandidateService = unknownSourceCandidateService;
        this.alertMessageFormatter = alertMessageFormatter;
        this.telegramSenderService = telegramSenderService;
        this.messageAnalysisService = messageAnalysisService;
        this.recentMessageCacheService = recentMessageCacheService;
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

            MessageAnalysis analysis =
                    messageAnalysisService.analyze(chatId, text);

            if (analysis == null) {
                return;
            }

            if (analysis.duplicate()
                    && !canSendDuplicateUpdate(analysis.intent())) {
                return;
            }

            telegramSenderService.sendToChat(
                    appProperties.telegram().targetChannelId(),
                    alertMessageFormatter.format(
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

        return "";
    }
}