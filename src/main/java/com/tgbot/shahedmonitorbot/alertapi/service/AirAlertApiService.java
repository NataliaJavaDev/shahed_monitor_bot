package com.tgbot.shahedmonitorbot.alertapi.service;

import com.tgbot.shahedmonitorbot.alertapi.client.AirAlertApiClient;
import org.springframework.stereotype.Service;

@Service
public class AirAlertApiService {

    private final AirAlertApiClient client;

    public AirAlertApiService(AirAlertApiClient client) {
        this.client = client;
    }

    public void checkAlerts() {
        String response = client.fetchAlerts();
        System.out.println(response);
    }
}