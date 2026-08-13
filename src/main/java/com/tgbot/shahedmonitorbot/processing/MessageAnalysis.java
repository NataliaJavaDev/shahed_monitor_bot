package com.tgbot.shahedmonitorbot.processing;

public record MessageAnalysis(
        MonitorMatch monitorMatch,
        ThreatMatch threatMatch,
        GlobalThreatMatch globalThreatMatch,
        ForecastMatch forecastMatch,
        MessageIntent intent,
        boolean duplicate,
        boolean contextUsed,
        String deduplicationKey,
        String originalMessage
) {
}