package com.tgbot.shahedmonitorbot.manualalert;

import com.tgbot.shahedmonitorbot.alert.AlertDeliveryService;
import com.tgbot.shahedmonitorbot.monitoring.MonitoringStateService;
import com.tgbot.shahedmonitorbot.monitoring.history.AlertReasonAnalyzerService;
import com.tgbot.shahedmonitorbot.monitoring.reason.AlertReason;
import org.springframework.stereotype.Service;

@Service
public class ManualAlertService {

    private final AlertDeliveryService alertDeliveryService;
    private final ManualAlertMessageFormatter formatter;
    private final MonitoringStateService monitoringStateService;
    private final AlertReasonAnalyzerService alertReasonAnalyzerService;

    public ManualAlertService(
            AlertDeliveryService alertDeliveryService,
            ManualAlertMessageFormatter formatter,
            MonitoringStateService monitoringStateService,
            AlertReasonAnalyzerService alertReasonAnalyzerService
    ) {
        this.alertDeliveryService = alertDeliveryService;
        this.formatter = formatter;
        this.monitoringStateService = monitoringStateService;
        this.alertReasonAnalyzerService = alertReasonAnalyzerService;
    }

    /*
     * Викликається тільки після натискання кнопок
     * ручного керування в адмінці.
     */
    public void sendAlert(ManualAlertType type) {

        updateMonitoringFromManual(type);

        sendWithReason(type);
    }

    public void sendApiAlert(ManualAlertType type) {

        sendWithReason(type);
    }

    private void send(
            ManualAlertType type,
            AlertReason reason
    ) {
        alertDeliveryService.send(
                formatter.format(type, reason)
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
    }

    private void sendWithReason(
            ManualAlertType type
    ) {

        if (type != ManualAlertType.ALERT) {
            send(type, null);
            return;
        }

        alertReasonAnalyzerService
                .analyze()
                .thenAccept(reason -> {send(type, reason);})
                .exceptionally(ex -> {

                    send(type, null);

                    return null;
                });
    }
}