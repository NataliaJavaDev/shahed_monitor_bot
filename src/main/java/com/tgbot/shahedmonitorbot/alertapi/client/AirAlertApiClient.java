package com.tgbot.shahedmonitorbot.alertapi.client;

import com.tgbot.shahedmonitorbot.alertapi.dto.RegionAlertDto;
import com.tgbot.shahedmonitorbot.config.AppProperties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AirAlertApiClient {

    private final RestClient restClient;
    private final AppProperties properties;

    private static final Logger log =
            LoggerFactory.getLogger(AirAlertApiClient.class);

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

        log.info("Alert API client initialized. Base URL: {}, API key exists: {}", 
                properties.alertApi().baseUrl(),
                !properties.alertApi().apiKey().isBlank());
    }

    public RegionAlertDto[] fetchAlerts() {

        try {
            return this.restClient.get()
                    .uri("/alerts")
                    .retrieve()
                    .body(RegionAlertDto[].class);
        } catch (Exception e) {
            log.error("Alert API request failed: ", e);
            throw e;
        }
    }
}