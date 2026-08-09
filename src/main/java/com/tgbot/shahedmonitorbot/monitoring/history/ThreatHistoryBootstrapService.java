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

        analyzerService.analyze()
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
    }

    public void clear() {

        // Поки що нічого.
        // Пізніше тут очищатимемо HistoricalThreatState.
    }
}