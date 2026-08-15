package com.tgbot.shahedmonitorbot.monitoring;

import org.springframework.stereotype.Service;

@Service
public class AlertLifecycleService {

    private final MonitoringStateService monitoringStateService;

    public AlertLifecycleService(
        MonitoringStateService monitoringStateService
    ) {
        this.monitoringStateService = monitoringStateService;
    }

    //  API control

    public boolean isApiControlEnabled() {
        return monitoringStateService.isApiControlEnabled();
    }

    public void enableApiControl() {
        monitoringStateService.enableApiControl();
    }

    public void disableApiControl() {
        monitoringStateService.disableApiControl();
    }

    public void toggleApiControl() {
        monitoringStateService.toggleApiControl();
    }

    //  Monitoring state

    public boolean isMonitoringEnabled() {
        return monitoringStateService.isMonitoringEnabled();
    }

    public boolean isApiMonitoringEnabled() {
        return monitoringStateService.isApiMonitoringEnabled();
    }

    public boolean isManualMonitoringEnabled() {
        return monitoringStateService.isManualMonitoringEnabled();
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


    //  Info

    public String getApiControlStatus() {
        return monitoringStateService.getApiControlStatus();
    }

    public String getMonitoringActivationSource() {
        return monitoringStateService.getMonitoringActivationSource();
    }
}