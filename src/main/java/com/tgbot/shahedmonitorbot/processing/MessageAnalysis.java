package com.tgbot.shahedmonitorbot.processing;

public record MessageAnalysis(
        MonitorMatch monitorMatch,
        boolean duplicate,
        boolean contextUsed,
        String deduplicationKey
) {
}