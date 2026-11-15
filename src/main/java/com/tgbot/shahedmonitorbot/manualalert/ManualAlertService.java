package com.tgbot.shahedmonitorbot.manualalert;

import com.tgbot.shahedmonitorbot.alert.AlertDeliveryService;
import com.tgbot.shahedmonitorbot.monitoring.MonitoringStateService;
import org.springframework.stereotype.Service;

@Service
public class ManualAlertService {

    private final AlertDeliveryService alertDeliveryService;
    private final ManualAlertMessageFormatter formatter;
    private final MonitoringStateService monitoringStateService;

    public ManualAlertService(
            AlertDeliveryService alertDeliveryService,
            ManualAlertMessageFormatter formatter,
            MonitoringStateService monitoringStateService
    ) {
        this.alertDeliveryService = alertDeliveryService;
        this.formatter = formatter;
        this.monitoringStateService = monitoringStateService;
    }

    public void sendAlert(AlertType type) {

        updateMonitoringFromManual(type);
        send(type);
    }

    public void sendApiAlert(AlertType type) {

        send(type);
    }

    private void send(AlertType type) {

        alertDeliveryService.send(formatter.format(type));
    }

    private void updateMonitoringFromManual(AlertType type) {

        if (type == AlertType.RED_ALERT || type == AlertType.YELLOW_ALERT) {
            monitoringStateService.enableMonitoringManually();
            return;
        }

        if (type == AlertType.ALL_CLEAR) {
            monitoringStateService.disableMonitoringManually();
        }
    }

    public void sendDebug(String text) {

        // alertDeliveryService.send("DEBUG\n\n" + text);
    }
}