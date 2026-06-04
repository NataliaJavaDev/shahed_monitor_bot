package com.tgbot.shahedmonitorbot.alertapi.scheduler;

import com.tgbot.shahedmonitorbot.alertapi.service.AirAlertApiService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AirAlertPollingScheduler {

    private final AirAlertApiService service;

    public AirAlertPollingScheduler(AirAlertApiService service) {
        this.service = service;
    }

    @Scheduled(fixedDelayString = "${alert-api.polling-delay-ms}")
    public void pollAlerts() {
        service.checkAlerts();
    }
}