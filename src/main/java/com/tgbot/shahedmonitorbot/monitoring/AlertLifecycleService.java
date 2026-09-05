package com.tgbot.shahedmonitorbot.monitoring;

import com.tgbot.shahedmonitorbot.enums.MonitoringControlMode;
import org.springframework.stereotype.Service;

@Service
public class AlertLifecycleService {

    private final MonitoringStateService monitoringStateService;

    public AlertLifecycleService(
            MonitoringStateService monitoringStateService
    ) {
        this.monitoringStateService = monitoringStateService;
    }

    // Monitoring state

    public boolean isMonitoringEnabled() {
        return monitoringStateService.isMonitoringEnabled();
    }

    public boolean isAutoMode() {
        return monitoringStateService.isAutoMode();
    }

    public boolean isManualMode() {
        return monitoringStateService.isManualMode();
    }

    public MonitoringControlMode getControlMode() {
        return monitoringStateService.getControlMode();
    }

    public void enableMonitoringFromApi() {
        monitoringStateService.enableMonitoringFromApi();
    }

    public void disableMonitoringFromApi() {
        monitoringStateService.disableMonitoringFromApi();
    }

    public void enableMonitoringManually() {
        monitoringStateService.enableMonitoringManually();
    }

    public void disableMonitoringManually() {
        monitoringStateService.disableMonitoringManually();
    }

    // Info

    public String getMonitoringActivationSource() {
        return monitoringStateService.getMonitoringActivationSource();
    }
}