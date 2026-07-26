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

    /*
     * Викликається тільки після натискання кнопок
     * ручного керування в адмінці.
     */
    public void sendAlert(ManualAlertType type) {

        updateMonitoringFromManual(type);

        alertDeliveryService.send(
                formatter.format(type)
        );
    }

    /*
     * Викликається AirAlertApiService.
     *
     * Не змінює ручний аварійний стан.
     */
    public void sendApiAlert(ManualAlertType type) {
        alertDeliveryService.send(
                formatter.format(type)
        );
    }

    private void updateMonitoringFromManual(
            ManualAlertType type
    ) {

        if (type == ManualAlertType.ALERT) {
            monitoringStateService
                    .enableMonitoringManually();
            return;
        }

        if (type == ManualAlertType.ALL_CLEAR) {
            monitoringStateService
                    .disableMonitoringManually();
        }

        /*
         * HIGH_RISK не впливає на моніторинг.
         */
    }
}