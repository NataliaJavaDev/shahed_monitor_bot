package com.tgbot.shahedmonitorbot.monitoring;

import org.springframework.stereotype.Service;

@Service
public class MonitoringStateService {

    private boolean apiControlEnabled = true;
    private boolean monitoringEnabled = false;

    public boolean isApiControlEnabled() {
        return apiControlEnabled;
    }

    public boolean isMonitoringEnabled() {
        return monitoringEnabled;
    }

    public void enableApiControl() {
        apiControlEnabled = true;
    }

    public void disableApiControl() {
        apiControlEnabled = false;
    }

    public void enableMonitoring() {
        monitoringEnabled = true;
    }

    public void disableMonitoring() {
        monitoringEnabled = false;
    }

    public void toggleApiControl() {
        apiControlEnabled = !apiControlEnabled;
    }

    public String getApiControlStatus() {
        return apiControlEnabled ? "УВІМКНЕНО" : "ВИМКНЕНО";
    }
}