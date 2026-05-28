package com.tgbot.shahedmonitorbot.sender;

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
            System.out.println("MESSAGE SENT");
        } catch (Exception e) {
            System.err.println("TELEGRAM SEND ERROR:");
            e.printStackTrace();
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