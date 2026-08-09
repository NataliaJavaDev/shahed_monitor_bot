package com.tgbot.shahedmonitorbot.monitoring.history;

import org.springframework.stereotype.Service;

@Service
public class ThreatHistoryBootstrapService {

    private final AlertReasonAnalyzerService analyzerService;

    public ThreatHistoryBootstrapService(
            AlertReasonAnalyzerService analyzerService
    ) {
        this.analyzerService = analyzerService;
    }

    public void initialize() {

        try {
            analyzerService.analyze();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}