package com.tgbot.shahedmonitorbot.processing;

public record MessageAnalysis(
        MonitorMatch monitorMatch,
        ThreatMatch threatMatch,
        MessageIntent intent,
        boolean duplicate,
        boolean contextUsed,
        String deduplicationKey
) {
}