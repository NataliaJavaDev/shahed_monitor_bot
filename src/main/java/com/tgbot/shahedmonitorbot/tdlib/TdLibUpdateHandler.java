package com.tgbot.shahedmonitorbot.tdlib;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tgbot.shahedmonitorbot.config.AppProperties;
import com.tgbot.shahedmonitorbot.monitoring.source.ChatInfoService;
import com.tgbot.shahedmonitorbot.monitoring.source.MonitoredSourceService;
import com.tgbot.shahedmonitorbot.monitoring.source.UnknownSourceCandidateService;
import com.tgbot.shahedmonitorbot.sender.AnalysisMessageFormatter;
import com.tgbot.shahedmonitorbot.sender.TelegramSenderService;
import com.tgbot.shahedmonitorbot.processing.MessageAnalysisService;
import com.tgbot.shahedmonitorbot.processing.MessageIntent;
import com.tgbot.shahedmonitorbot.processing.MessageAnalysis;

import org.springframework.stereotype.Service;

@Service
public class TdLibUpdateHandler {

    private final ObjectMapper objectMapper;
    private final AppProperties appProperties;
    private final ChatInfoService chatInfoService;
    private final MonitoredSourceService monitoredSourceService;
    private final UnknownSourceCandidateService unknownSourceCandidateService;
    private final AnalysisMessageFormatter analysisMessageFormatter;
    private final TelegramSenderService telegramSenderService;
    private final MessageAnalysisService messageAnalysisService;

    public TdLibUpdateHandler(
            ObjectMapper objectMapper,
            AppProperties appProperties,
            ChatInfoService chatInfoService,
            MonitoredSourceService monitoredSourceService,
            UnknownSourceCandidateService unknownSourceCandidateService,
            AnalysisMessageFormatter analysisMessageFormatter,
            TelegramSenderService telegramSenderService,
            MessageAnalysisService messageAnalysisService
            
    ) {
        this.objectMapper = objectMapper;
        this.appProperties = appProperties;
        this.chatInfoService = chatInfoService;
        this.monitoredSourceService = monitoredSourceService;
        this.unknownSourceCandidateService = unknownSourceCandidateService;
        this.analysisMessageFormatter = analysisMessageFormatter;
        this.telegramSenderService = telegramSenderService;
        this.messageAnalysisService = messageAnalysisService;
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

            MessageAnalysis analysis = messageAnalysisService.analyze(text);

            if (analysis == null) {
                return;
            }
            
            if (analysis.duplicate() && !canSendDuplicateUpdate(analysis.intent())) {
                return;
            }

            telegramSenderService.sendToChat(
                appProperties.telegram().targetChannelId(),
                analysisMessageFormatter.format(
                        analysis,
                        source.title(),
                        chatId,
                        text
                )
            );

        } catch (Exception e) {
            System.out.println("Failed to handle TDLib update: " + e.getMessage());
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

        return "[unsupported message type: " + contentType + "]";
    }
}