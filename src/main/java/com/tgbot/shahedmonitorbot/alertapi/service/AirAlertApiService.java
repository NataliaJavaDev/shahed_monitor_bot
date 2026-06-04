package com.tgbot.shahedmonitorbot.alertapi.service;

import com.tgbot.shahedmonitorbot.alertapi.client.AirAlertApiClient;
import com.tgbot.shahedmonitorbot.alertapi.dto.RegionAlertDto;
import com.tgbot.shahedmonitorbot.config.AppProperties;
import com.tgbot.shahedmonitorbot.manualalert.ManualAlertType;

import java.util.Arrays;

import org.springframework.stereotype.Service;

@Service
public class AirAlertApiService {

    private final AirAlertApiClient client;
    private final AppProperties properties;

    public AirAlertApiService(
        AirAlertApiClient client,
        AppProperties properties
    ) {
        this.client = client;
        this.properties = properties;
    }

    public void checkAlerts() {

        RegionAlertDto[] alerts = client.fetchAlerts();

        ManualAlertType type = detectAlertType(alerts);

        System.out.println("API alert type: " + type);
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