package com.tgbot.shahedmonitorbot.alertapi.client;

import com.tgbot.shahedmonitorbot.alertapi.dto.RegionAlertDto;
import com.tgbot.shahedmonitorbot.config.AppProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AirAlertApiClient {

    private final RestClient restClient;
    private final AppProperties properties;

    public AirAlertApiClient(
            RestClient.Builder restClientBuilder,
            AppProperties properties
    ) {
        this.properties = properties;
        this.restClient = restClientBuilder
                .baseUrl(properties.alertApi().baseUrl())
                .defaultHeader("Authorization",
                properties.alertApi().apiKey())
                .build();

        System.out.println("API key exists: " + !properties.alertApi().apiKey().isBlank());
System.out.println("API key length: " + properties.alertApi().apiKey().length());
System.out.println("Base URL: " + properties.alertApi().baseUrl());
    }

    

//     public RegionAlertDto[] fetchAlerts() {
//         return this.restClient.get()
//                 .uri("/alerts")
//                 .retrieve()
//                 .body(RegionAlertDto[].class);
//     }

    public RegionAlertDto[] fetchAlerts() {
    try {
        return this.restClient.get()
                .uri("/alerts")
                .retrieve()
                .body(RegionAlertDto[].class);
    } catch (Exception e) {
        System.out.println("Alert API request failed: " + e.getMessage());
        throw e;
    }
}
}