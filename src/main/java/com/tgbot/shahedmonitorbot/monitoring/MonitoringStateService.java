package com.tgbot.shahedmonitorbot.monitoring;

import com.tgbot.shahedmonitorbot.enums.MonitoringControlMode;
import org.springframework.stereotype.Service;

@Service
public class MonitoringStateService {

    private volatile MonitoringControlMode controlMode = MonitoringControlMode.AUTO;
    private volatile boolean monitoringEnabled = false;

    /**
     * Фінальний стан активного моніторингу.
     */
    public boolean isMonitoringEnabled() {
        return monitoringEnabled;
    }

    public MonitoringControlMode getControlMode() {
        return controlMode;
    }

    public boolean isAutoMode() {
        return controlMode == MonitoringControlMode.AUTO;
    }

    public boolean isManualMode() {
        return controlMode == MonitoringControlMode.MANUAL;
    }

    /**
     * API може керувати моніторингом тільки в AUTO.
     */
    public void enableMonitoringFromApi() {

        if (!isAutoMode()) {
            return;
        }

        monitoringEnabled = true;
    }

    /**
     * API може керувати моніторингом тільки в AUTO.
     */
    public void disableMonitoringFromApi() {

        if (!isAutoMode()) {
            return;
        }

        monitoringEnabled = false;
    }

    /**
     * Ручне ввімкнення переводить систему в MANUAL.
     */
    public void enableMonitoringManually() {

        controlMode = MonitoringControlMode.MANUAL;
        monitoringEnabled = true;
    }

    /**
     * Ручне вимкнення завжди повертає систему в AUTO.
     */
    public void disableMonitoringManually() {

        monitoringEnabled = false;
        controlMode = MonitoringControlMode.AUTO;
    }

    public String getMonitoringActivationSource() {

        if (!monitoringEnabled) {
            return "неактивний";
        }

        return switch (controlMode) {
            case AUTO -> "API";
            case MANUAL -> "ручне керування";
        };
    }
}