package com.tgbot.shahedmonitorbot.processing;

public record ContextResolution(
        MonitorMatch match,
        boolean contextUsed
) {
}