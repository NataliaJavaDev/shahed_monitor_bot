package com.tgbot.shahedmonitorbot.processing;

public record MessageAnalysis(
        MonitorMatch monitorMatch,
        MessageIntent intent,
        boolean duplicate,
        boolean contextUsed,
        String deduplicationKey
) {
}