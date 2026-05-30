package com.tgbot.shahedmonitorbot.manualalert;

import com.tgbot.shahedmonitorbot.sender.TelegramSenderService;
import org.springframework.stereotype.Service;

@Service
public class ManualAlertService {

    private final TelegramSenderService senderService;
    private final ManualAlertMessageFormatter formatter;

    public ManualAlertService(
            TelegramSenderService senderService,
            ManualAlertMessageFormatter formatter
    ) {
        this.senderService = senderService;
        this.formatter = formatter;
    }

    public void sendAlert(ManualAlertType type) {
        senderService.send(formatter.format(type));
    }
}