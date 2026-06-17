package com.tgbot.shahedmonitorbot.tdlib;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tgbot.shahedmonitorbot.admin.service.KeywordAdminService;
import com.tgbot.shahedmonitorbot.config.AppProperties;
import com.tgbot.shahedmonitorbot.monitoring.source.ChatInfoService;
import com.tgbot.shahedmonitorbot.monitoring.source.MonitoredSourceService;
import com.tgbot.shahedmonitorbot.monitoring.source.UnknownSourceCandidateService;
import com.tgbot.shahedmonitorbot.processing.DuplicateMessageService;
import com.tgbot.shahedmonitorbot.sender.TelegramSenderService;
import org.springframework.stereotype.Service;

@Service
public class TdLibUpdateHandler {

    private final MonitoredSourceService monitoredSourceService;
    private final ObjectMapper objectMapper;
    private final KeywordAdminService keywordAdminService;
    private final TelegramSenderService telegramSenderService;
    private final AppProperties appProperties;
    private final DuplicateMessageService duplicateMessageService;
    private final UnknownSourceCandidateService unknownSourceCandidateService;
    private final ChatInfoService chatInfoService;

    public TdLibUpdateHandler(
            MonitoredSourceService monitoredSourceService,
            ObjectMapper objectMapper,
            KeywordAdminService keywordAdminService,
            TelegramSenderService telegramSenderService,
            AppProperties appProperties,
            DuplicateMessageService duplicateMessageService,
            UnknownSourceCandidateService unknownSourceCandidateService,
            ChatInfoService chatInfoService
    ) {
        this.monitoredSourceService = monitoredSourceService;
        this.objectMapper = objectMapper;
        this.keywordAdminService = keywordAdminService;
        this.telegramSenderService = telegramSenderService;
        this.appProperties = appProperties;
        this.duplicateMessageService = duplicateMessageService;
        this.unknownSourceCandidateService = unknownSourceCandidateService;
        this.chatInfoService = chatInfoService;
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

            System.out.println("NEW MESSAGE FROM MONITORED SOURCE");
            System.out.println("SOURCE: " + source.title());
            System.out.println("CHAT_ID: " + chatId);
            System.out.println("TEXT: " + text);

            String matchedKeyword = keywordAdminService.findMatchedKeyword(text);

            if (matchedKeyword == null) {
                return;
            }

            if (duplicateMessageService.isDuplicate(text)) {
                System.out.println("DUPLICATE MESSAGE SKIPPED");
                System.out.println("TEXT: " + text);
                return;
            }

            String messageToSend = """
                    🚨 Знайдено повідомлення

                    💬 %s
                    """.formatted(text);

            System.out.println("MATCHED KEYWORD: " + matchedKeyword);
            System.out.println("SOURCE: " + source.title());
            System.out.println("SOURCE CHAT_ID: " + chatId);
            System.out.println("TEXT: " + text);

            telegramSenderService.sendToChat(
                    appProperties.telegram().targetChannelId(),
                    messageToSend
            );

        } catch (Exception e) {
            System.out.println("Failed to handle TDLib update: " + e.getMessage());
        }
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