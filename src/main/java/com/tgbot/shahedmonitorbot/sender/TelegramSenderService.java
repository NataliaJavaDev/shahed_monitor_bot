package com.tgbot.shahedmonitorbot.sender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tgbot.shahedmonitorbot.config.AppProperties;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

@Service
public class TelegramSenderService {

    private final TelegramClient telegramClient;
    private final AppProperties properties;

    private static final Logger log =
            LoggerFactory.getLogger(TelegramSenderService.class);

    public TelegramSenderService(
            TelegramClient telegramClient,
            AppProperties properties
    ) {
        this.telegramClient = telegramClient;
        this.properties = properties;
    }

    public void send(String text) {
        sendToChat(properties.telegram().targetChannelId(), text);
    }

    public void sendToChat(String chatId, String text) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .build();

        execute(message);
    }

    public void sendToChatWithKeyboard(
            String chatId,
            String text,
            InlineKeyboardMarkup keyboard
    ) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(keyboard)
                .build();

        execute(message);
    }

    private void execute(SendMessage message) {

        try {
            telegramClient.execute(message);
            log.info("Telegram message sent to chat {}", message.getChatId());
        } catch (Exception e) {
            log.error("Telegram send error", e);
        }
    }

    public void sendToChatWithReplyKeyboard(
        String chatId,
        String text,
        ReplyKeyboardMarkup keyboard
) {

    SendMessage message = SendMessage.builder()
            .chatId(chatId)
            .text(text)
            .replyMarkup(keyboard)
            .build();

    execute(message);
}
}