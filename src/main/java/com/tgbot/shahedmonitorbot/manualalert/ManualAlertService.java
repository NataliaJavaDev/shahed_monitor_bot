package com.tgbot.shahedmonitorbot.manualalert;

import com.tgbot.shahedmonitorbot.monitoring.MonitoringStateService;
import com.tgbot.shahedmonitorbot.alert.AlertDeliveryService;
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

    private void updateMonitoringFromManual(ManualAlertType type) {

        if (monitoringStateService.isApiControlEnabled()) {
            return;
        }

        if (type == ManualAlertType.ALERT) {
            monitoringStateService.enableMonitoring();
        }

        if (type == ManualAlertType.ALL_CLEAR) {
            monitoringStateService.disableMonitoring();
        }
    }

    public void sendAlert(ManualAlertType type) {
        updateMonitoringFromManual(type);
        alertDeliveryService.send(formatter.format(type));
    }
}