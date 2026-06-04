package com.tgbot.shahedmonitorbot.alertapi.service;

import com.tgbot.shahedmonitorbot.alertapi.client.AirAlertApiClient;
import com.tgbot.shahedmonitorbot.alertapi.dto.RegionAlertDto;

import java.util.Arrays;

import org.springframework.stereotype.Service;

@Service
public class AirAlertApiService {

    private final AirAlertApiClient client;

    public AirAlertApiService(AirAlertApiClient client) {
        this.client = client;
    }

    public void checkAlerts() {

        RegionAlertDto[] alerts = client.fetchAlerts();

        boolean districtAlert = Arrays.stream(alerts)
            .anyMatch(alert -> "73".equals(alert.regionId()));

        System.out.println(
                "Bila Tserkva district alert: " + districtAlert
        );
    }
}