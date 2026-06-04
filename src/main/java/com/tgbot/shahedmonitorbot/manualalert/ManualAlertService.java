package com.tgbot.shahedmonitorbot.manualalert;

import com.tgbot.shahedmonitorbot.alert.AlertDeliveryService;
import org.springframework.stereotype.Service;

@Service
public class ManualAlertService {

    private final AlertDeliveryService alertDeliveryService;
    private final ManualAlertMessageFormatter formatter;

    public ManualAlertService(
            AlertDeliveryService alertDeliveryService,
            ManualAlertMessageFormatter formatter
    ) {
        this.alertDeliveryService = alertDeliveryService;
        this.formatter = formatter;
    }

    public void sendAlert(ManualAlertType type) {
        alertDeliveryService.send(formatter.format(type));
    }
}