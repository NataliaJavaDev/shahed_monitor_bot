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

    public void sendAlert(ManualAlertType type) {

        alertDeliveryService.send("DEBUG: sendAlert()");

        updateMonitoringFromManual(type);

        sendWithReason(type);
    }

    public void sendApiAlert(ManualAlertType type) {

        alertDeliveryService.send("DEBUG: sendApiAlert()");

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

        try {

            AlertReason reason = alertReasonAnalyzerService.analyze();

            alertDeliveryService.send(
                    "DEBUG\n" + reason
            );

            send(type, reason);

        } catch (Exception e) {

            alertDeliveryService.send("""
            EXCEPTION

            %s
            """.formatted(e));

                send(type, null);
        }
    }

    public void sendDebug(String text) {
        alertDeliveryService.send(
                "DEBUG\n\n" + text
        );
    }
}