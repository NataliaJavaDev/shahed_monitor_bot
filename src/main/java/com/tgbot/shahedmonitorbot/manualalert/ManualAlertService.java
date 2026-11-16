package com.tgbot.shahedmonitorbot.manualalert;

import com.tgbot.shahedmonitorbot.alert.AlertDeliveryService;
import com.tgbot.shahedmonitorbot.alertapi.model.DangerLevel;
import com.tgbot.shahedmonitorbot.monitoring.MonitoringStateService;
import org.springframework.stereotype.Service;

@Service
public class ManualAlertService {

    private final AlertDeliveryService alertDeliveryService;
    private final ManualAlertMessageFormatter formatter;
    private final MonitoringStateService monitoringStateService;

    /**
     * Останній рівень тривоги, про який повідомили (з API або кнопками).
     * Потрібен, щоб при заміні RED <-> YELLOW надсилати повідомлення про зміну.
     */
    private DangerLevel currentLevel = DangerLevel.NONE;

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

    /** Синхронізація поточного рівня без відправки повідомлення (наприклад, при старті). */
    public synchronized void syncLevel(DangerLevel level) {
        currentLevel = level;
    }

    private synchronized void send(AlertType type) {
        alertDeliveryService.send(formatter.format(resolve(type)));
    }

    /**
     * Червона/жовта тривога після протилежного рівня перетворюється
     * на повідомлення про зміну рівня. Повтор того самого рівня
     * надсилається як звичайна тривога.
     */
    private AlertType resolve(AlertType type) {

        if (type == AlertType.RED_ALERT) {
            AlertType result = currentLevel == DangerLevel.YELLOW
                ? AlertType.YELLOW_TO_RED
                : AlertType.RED_ALERT;
            currentLevel = DangerLevel.RED;
            return result;
        }

        if (type == AlertType.YELLOW_ALERT) {
            AlertType result = currentLevel == DangerLevel.RED
                ? AlertType.RED_TO_YELLOW
                : AlertType.YELLOW_ALERT;
            currentLevel = DangerLevel.YELLOW;
            return result;
        }

        if (type == AlertType.ALL_CLEAR) {
            currentLevel = DangerLevel.NONE;
        }

        // HIGH_RISK рівень тривоги не змінює.
        return type;
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