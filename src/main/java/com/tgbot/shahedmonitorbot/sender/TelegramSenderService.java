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
    System.out.println("SENDING TO TELEGRAM");
    System.out.println("TARGET = " + properties.telegram().targetChannelId());
    System.out.println("TEXT = " + text);

    SendMessage message = SendMessage.builder()
            .chatId(properties.telegram().targetChannelId())
            .text(text)
            .build();

        try {
            telegramClient.execute(message);
            System.out.println("MESSAGE SENT");
        } catch (Exception e) {
            System.err.println("TELEGRAM SEND ERROR:");
            e.printStackTrace();
        }
    }

    public void sendToChat(String chatId, String text) {
    SendMessage message = SendMessage.builder()
            .chatId(chatId)
            .text(text)
            .build();

        try {
            telegramClient.execute(message);
        } catch (Exception e) {
            System.err.println("TELEGRAM SEND ERROR:");
            e.printStackTrace();
        }
    }
}