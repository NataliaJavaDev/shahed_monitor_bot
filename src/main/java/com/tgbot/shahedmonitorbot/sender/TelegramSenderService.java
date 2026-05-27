package com.tgbot.shahedmonitorbot.sender;

import com.tgbot.shahedmonitorbot.config.AppProperties;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Service
public class TelegramSenderService {

    private final TelegramClient telegramClient;
    private final AppProperties properties;

    public TelegramSenderService(TelegramClient telegramClient, AppProperties properties) {
        this.telegramClient = telegramClient;
        this.properties = properties;
    }

    public void send(String text) {
        SendMessage message = SendMessage.builder()
                .chatId(properties.telegram().targetChatId())
                .text(text)
                .build();

        try {
            telegramClient.execute(message);
        } catch (Exception e) {
            throw new RuntimeException("Не вдалося відправити повідомлення в Telegram", e);
        }
    }
}