package com.tgbot.shahedmonitorbot.alertapi.scheduler;

import com.tgbot.shahedmonitorbot.alertapi.service.AirAlertApiService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AirAlertPollingScheduler {

    private final AirAlertApiService service;
    private static final Logger log =
            LoggerFactory.getLogger(AirAlertPollingScheduler.class);

    public AirAlertPollingScheduler(AirAlertApiService service) {
        this.service = service;
    }

    @Scheduled(fixedDelayString = "${app.alert-api.polling-delay-ms}")
    public void pollAlerts() {
        service.checkAlerts();
        log.info("Polling alerts...");
    }
}