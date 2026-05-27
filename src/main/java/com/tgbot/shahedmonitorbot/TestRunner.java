package com.tgbot.shahedmonitorbot;

import com.tgbot.shahedmonitorbot.processing.AlertProcessingService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class TestRunner implements CommandLineRunner {

    private final AlertProcessingService alertProcessingService;

    public TestRunner(AlertProcessingService alertProcessingService) {
        this.alertProcessingService = alertProcessingService;
    }

    @Override
    public void run(String... args) {
        System.out.println("TEST RUNNER STARTED");

        alertProcessingService.process(
                "TEST",
                "Шахед курсом на Білу Церкву"
        );

        System.out.println("TEST RUNNER FINISHED");
    }
}