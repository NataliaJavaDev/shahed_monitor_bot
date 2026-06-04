package com.tgbot.shahedmonitorbot.alert;

import com.tgbot.shahedmonitorbot.sender.TelegramSenderService;
import org.springframework.stereotype.Service;

@Service
public class AlertDeliveryService {

    private final TelegramSenderService senderService;

    public AlertDeliveryService(TelegramSenderService senderService) {
        this.senderService = senderService;
    }

    public void send(String message) {
        senderService.send(message);
    }
}