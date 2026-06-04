package com.tgbot.shahedmonitorbot.alertapi.client;

import org.springframework.stereotype.Component;
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
                .defaultHeader("Authorization", properties.alertApi().apiKey())
                .build();
    }

    public String fetchAlerts() {
        return restClient.get()
                .uri("/alerts")
                .retrieve()
                .body(String.class);
    }
}