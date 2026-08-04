package com.tgbot.shahedmonitorbot.monitoring;

import com.tgbot.shahedmonitorbot.monitoring.history.ThreatHistoryBootstrapService;
import org.springframework.stereotype.Service;

@Service
public class AlertLifecycleService {

    private final MonitoringStateService monitoringStateService;
    private final ThreatHistoryBootstrapService threatHistoryBootstrapService;

    public AlertLifecycleService(
            MonitoringStateService monitoringStateService,
            ThreatHistoryBootstrapService threatHistoryBootstrapService
    ) {
        this.monitoringStateService = monitoringStateService;
        this.threatHistoryBootstrapService = threatHistoryBootstrapService;
    }

    /*
     * ----------------------------
     * API control
     * ----------------------------
     */

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

    /*
     * ----------------------------
     * Monitoring state
     * ----------------------------
     */

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

        boolean wasEnabled =
                monitoringStateService.isMonitoringEnabled();

        monitoringStateService.enableMonitoringFromApi();

        if (!wasEnabled
                && monitoringStateService.isMonitoringEnabled()) {

            threatHistoryBootstrapService.initialize();
        }
    }

    public void disableMonitoringFromApi() {

        boolean wasEnabled =
                monitoringStateService.isMonitoringEnabled();

        monitoringStateService.disableMonitoringFromApi();

        if (wasEnabled
                && !monitoringStateService.isMonitoringEnabled()) {

            threatHistoryBootstrapService.clear();
        }
    }

    public void enableMonitoringManually() {

        boolean wasEnabled =
                monitoringStateService.isMonitoringEnabled();

        monitoringStateService.enableMonitoringManually();

        if (!wasEnabled
                && monitoringStateService.isMonitoringEnabled()) {

            threatHistoryBootstrapService.initialize();
        }
    }

    public void disableMonitoringManually() {

        boolean wasEnabled =
                monitoringStateService.isMonitoringEnabled();

        monitoringStateService.disableMonitoringManually();

        if (wasEnabled
                && !monitoringStateService.isMonitoringEnabled()) {

            threatHistoryBootstrapService.clear();
        }
    }

    /*
     * ----------------------------
     * Info
     * ----------------------------
     */

    public String getApiControlStatus() {
        return monitoringStateService.getApiControlStatus();
    }

    public String getMonitoringActivationSource() {
        return monitoringStateService.getMonitoringActivationSource();
    }
}