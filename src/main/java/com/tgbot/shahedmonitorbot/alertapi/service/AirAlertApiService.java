package com.tgbot.shahedmonitorbot.alertapi.service;

import com.tgbot.shahedmonitorbot.alertapi.client.AirAlertApiClient;
import com.tgbot.shahedmonitorbot.alertapi.dto.RegionAlertDto;
import com.tgbot.shahedmonitorbot.alertapi.model.ApiAlertStatus;
import com.tgbot.shahedmonitorbot.config.AppProperties;
import com.tgbot.shahedmonitorbot.manualalert.ManualAlertService;
import com.tgbot.shahedmonitorbot.manualalert.ManualAlertType;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

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

    public void checkAlerts() {

        try {
            RegionAlertDto[] alerts = client.fetchAlerts();

            ApiAlertStatus currentStatus = detectAlertStatus(alerts);

            if (currentStatus.type() == lastStatus.type()) {
                System.out.println("API alert status unchanged: " + currentStatus);
                lastStatus = currentStatus;
                return;
            }

            System.out.println("API alert status changed: " + lastStatus.type() + " -> " + currentStatus.type());

            lastStatus = currentStatus;

        } catch (Exception e) {
            System.out.println("Alert API check failed: " + e.getMessage());
        }
    }

    private ApiAlertStatus detectAlertStatus(RegionAlertDto[] alerts) {

        List<String> activeDangerRegions = Arrays.stream(alerts)
                .filter(alert -> properties.alertApi()
                        .dangerRegionIds()
                        .contains(alert.regionId()))
                .map(RegionAlertDto::regionName)
                .toList();

        boolean districtAlertActive = Arrays.stream(alerts)
                .anyMatch(alert -> properties.alertApi()
                        .alarmRegionId()
                        .equals(alert.regionId()));

        ManualAlertType type;

        if (!activeDangerRegions.isEmpty()) {
            type = ManualAlertType.HIGH_RISK;
        } else if (districtAlertActive) {
            type = ManualAlertType.ALERT;
        } else {
            type = ManualAlertType.ALL_CLEAR;
        }

        return new ApiAlertStatus(
                type,
                districtAlertActive,
                activeDangerRegions,
                LocalDateTime.now()
        );
    }

    public ApiAlertStatus getLastStatus() {
        return lastStatus;
    }

    private ApiAlertStatus lastStatus = new ApiAlertStatus(
        ManualAlertType.ALL_CLEAR,
        false,
        List.of(),
        LocalDateTime.now()
    );
}