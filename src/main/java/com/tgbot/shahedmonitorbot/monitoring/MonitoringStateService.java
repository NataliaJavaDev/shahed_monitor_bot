package com.tgbot.shahedmonitorbot.monitoring;

import org.springframework.stereotype.Service;

@Service
public class MonitoringStateService {

    private volatile boolean apiControlEnabled = true;

    /*
     * Стан, встановлений API тривог.
     */
    private volatile boolean apiMonitoringEnabled = false;

    /*
     * Аварійний ручний стан.
     */
    private volatile boolean manualMonitoringEnabled = false;

    public boolean isApiControlEnabled() {
        return apiControlEnabled;
    }

    /*
     * Фінальний стан активного моніторингу.
     */
    public boolean isMonitoringEnabled() {
        return apiMonitoringEnabled || manualMonitoringEnabled;
    }

    public boolean isApiMonitoringEnabled() {
        return apiMonitoringEnabled;
    }

    public boolean isManualMonitoringEnabled() {
        return manualMonitoringEnabled;
    }

    public void enableApiControl() {
        apiControlEnabled = true;
    }

    public void disableApiControl() {
        apiControlEnabled = false;

        /*
         * Після вимкнення API-керування старий API-стан
         * не повинен утримувати моніторинг увімкненим.
         */
        apiMonitoringEnabled = false;
    }

    public void toggleApiControl() {
        if (apiControlEnabled) {
            disableApiControl();
        } else {
            enableApiControl();
        }
    }

    public void enableMonitoringFromApi() {
        if (!apiControlEnabled) {
            return;
        }

        apiMonitoringEnabled = true;
    }

    public void disableMonitoringFromApi() {
        if (!apiControlEnabled) {
            return;
        }

        apiMonitoringEnabled = false;
    }

    public void enableMonitoringManually() {
        manualMonitoringEnabled = true;
    }

    public void disableMonitoringManually() {
        manualMonitoringEnabled = false;
    }

    public String getApiControlStatus() {
        return apiControlEnabled
                ? "УВІМКНЕНО"
                : "ВИМКНЕНО";
    }

    public String getMonitoringActivationSource() {

        if (apiMonitoringEnabled && manualMonitoringEnabled) {
            return "API + ручне керування";
        }

        if (manualMonitoringEnabled) {
            return "ручне керування";
        }

        if (apiMonitoringEnabled) {
            return "API";
        }

        return "неактивний";
    }
}