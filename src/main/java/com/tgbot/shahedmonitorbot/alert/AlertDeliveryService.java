package com.tgbot.shahedmonitorbot.alert;

import com.tgbot.shahedmonitorbot.config.AppProperties;
import com.tgbot.shahedmonitorbot.sender.TelegramSenderService;
import org.springframework.stereotype.Service;

@Service
public class AlertDeliveryService {

    private final TelegramSenderService senderService;
    private final AppProperties properties;

    public AlertDeliveryService(TelegramSenderService senderService, AppProperties properties) {
        this.senderService = senderService;
        this.properties = properties;
    }

    public void send(String message) {
        senderService.sendToChat(properties.telegram().targetChannelId(), message);
        senderService.sendToChat(properties.telegram().debugChannelId(), message);
    }
}