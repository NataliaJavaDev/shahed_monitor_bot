package com.tgbot.shahedmonitorbot.alertapi.service;

import com.tgbot.shahedmonitorbot.alertapi.client.AirAlertApiClient;
import com.tgbot.shahedmonitorbot.alertapi.dto.RegionAlertDto;
import com.tgbot.shahedmonitorbot.config.AppProperties;
import com.tgbot.shahedmonitorbot.manualalert.ManualAlertService;
import com.tgbot.shahedmonitorbot.manualalert.ManualAlertType;

import java.util.Arrays;

import org.springframework.stereotype.Service;

@Service
public class AirAlertApiService {

    private final AirAlertApiClient client;
    private final AppProperties properties;
    private final ManualAlertService manualAlertService;

    public AirAlertApiService(
        AirAlertApiClient client,
        AppProperties properties,
        ManualAlertService manualAlertService
    ) {
        this.client = client;
        this.properties = properties;
        this.manualAlertService = manualAlertService;
    }

    private ManualAlertType previousAlertType = ManualAlertType.ALL_CLEAR;

    public void checkAlerts() {
        try {
            RegionAlertDto[] alerts = client.fetchAlerts();

            ManualAlertType currentAlertType = detectAlertType(alerts);

            if (currentAlertType == previousAlertType) {
                System.out.println("API alert type unchanged: " + currentAlertType);
                return;
            }

            manualAlertService.sendAlert(currentAlertType);
            previousAlertType = currentAlertType;

        } catch (Exception e) {
            System.out.println("Alert API check failed: " + e.getMessage());
        }
    }

    private ManualAlertType detectAlertType(RegionAlertDto[] alerts) {

    boolean highRisk = Arrays.stream(alerts)
            .anyMatch(alert -> properties.alertApi()
                    .dangerRegionIds()
                    .contains(alert.regionId()));

    if (highRisk) {
        return ManualAlertType.HIGH_RISK;
    }

    boolean districtAlert = Arrays.stream(alerts)
            .anyMatch(alert -> properties.alertApi()
                    .alarmRegionId()
                    .equals(alert.regionId()));

    if (districtAlert) {
        return ManualAlertType.ALERT;
    }

    return ManualAlertType.ALL_CLEAR;
}
}