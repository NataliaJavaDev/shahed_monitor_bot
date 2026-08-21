package com.tgbot.shahedmonitorbot.alertapi.service;

import com.tgbot.shahedmonitorbot.alertapi.client.AirAlertApiClient;
import com.tgbot.shahedmonitorbot.alertapi.dto.RegionAlertDto;
import com.tgbot.shahedmonitorbot.alertapi.model.ApiAlertStatus;
import com.tgbot.shahedmonitorbot.config.AppProperties;
import com.tgbot.shahedmonitorbot.manualalert.ManualAlertService;
import com.tgbot.shahedmonitorbot.manualalert.ManualAlertType;
import com.tgbot.shahedmonitorbot.monitoring.AlertLifecycleService;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;

@Service
public class AirAlertApiService {

    private static final ZoneId KYIV_ZONE = ZoneId.of("Europe/Kyiv");

    private final AirAlertApiClient client;
    private final AppProperties properties;
    private final ManualAlertService manualAlertService;
    private final AlertLifecycleService alertLifecycleService;

    private ApiAlertStatus lastStatus = new ApiAlertStatus(false,
        false,
        LocalDateTime.now(KYIV_ZONE));

    public AirAlertApiService(
        AirAlertApiClient client,
        AppProperties properties,
        ManualAlertService manualAlertService,
        AlertLifecycleService alertLifecycleService
    ) {
        this.client = client;
        this.properties = properties;
        this.manualAlertService = manualAlertService;
        this.alertLifecycleService = alertLifecycleService;
    }

    public void checkAlerts() {
        
        try {

            RegionAlertDto[] alerts = client.fetchAlerts();

            if (alerts == null) {
                alerts = new RegionAlertDto[0];
            }

            ApiAlertStatus currentStatus = detectAlertStatus(alerts);

            processDistrictAlertChange(lastStatus, currentStatus);
            processHighRiskChange(lastStatus, currentStatus);

            if (hasStatusChanged(lastStatus, currentStatus)) {
                System.out.println(
                        "API alert status changed: "
                                + formatStatusForLog(lastStatus)
                                + " -> "
                                + formatStatusForLog(currentStatus)
                );
            } else {
                System.out.println(
                        "API alert status unchanged: "
                                + formatStatusForLog(currentStatus)
                );
            }

            lastStatus = currentStatus;

        } catch (Exception e) {
            System.out.println("Alert API check failed: " + e.getMessage());
        }
    }

    private void processDistrictAlertChange(ApiAlertStatus previousStatus, ApiAlertStatus currentStatus) {
    
        boolean wasActive = previousStatus.districtAlertActive();
        boolean isActive = currentStatus.districtAlertActive();
    
        if (wasActive == isActive) {
            return;
        }
    
        if (isActive) {
    
            updateMonitoringFromApi(ManualAlertType.ALERT);
            manualAlertService.sendApiAlert(ManualAlertType.ALERT);
    
            return;
        }
    
        updateMonitoringFromApi(ManualAlertType.ALL_CLEAR);
        manualAlertService.sendApiAlert(ManualAlertType.ALL_CLEAR);
    }

    private void processHighRiskChange(ApiAlertStatus previousStatus, ApiAlertStatus currentStatus) {

        boolean wasActive = previousStatus.highRiskActive();
        boolean isActive = currentStatus.highRiskActive();

        /*
         * HIGH_RISK надсилається тільки тоді,
         * коли активується громада з high-risk-region-id,
         * тобто 711.
         */
        if (!wasActive && isActive) {
            manualAlertService.sendApiAlert(ManualAlertType.HIGH_RISK);
        }
    }

    private void updateMonitoringFromApi(ManualAlertType type) {

        if (type == ManualAlertType.ALERT) {
            alertLifecycleService.enableMonitoringFromApi();
            return;
        }

        if (type == ManualAlertType.ALL_CLEAR) {
            alertLifecycleService.disableMonitoringFromApi();
        }
    }

    private ApiAlertStatus detectAlertStatus(RegionAlertDto[] alerts) {

        boolean districtAlertActive = Arrays.stream(alerts)
            .anyMatch(alert ->
                properties.alertApi()
                    .alarmRegionId()
                    .equals(alert.regionId())
            );

        boolean highRiskActive = Arrays.stream(alerts)
            .anyMatch(alert ->
                properties.alertApi()
                    .highRiskRegionId()
                    .equals(alert.regionId())
            );

        return new ApiAlertStatus(
                districtAlertActive,
                highRiskActive,
                LocalDateTime.now(KYIV_ZONE)
        );
    }

    private boolean hasStatusChanged(ApiAlertStatus previousStatus, ApiAlertStatus currentStatus) {
        return previousStatus.districtAlertActive()
                != currentStatus.districtAlertActive()
                || previousStatus.highRiskActive()
                != currentStatus.highRiskActive();
    }

    private String formatStatusForLog(ApiAlertStatus status) {

        return "districtAlertActive="
                + status.districtAlertActive()
                + ", highRiskActive="
                + status.highRiskActive();
    }

    public ApiAlertStatus getLastStatus() {
        return lastStatus;
    }
}