package com.tgbot.shahedmonitorbot.monitoring.source;

public record MonitoredSource(
        String chatId,
        String title,
        boolean active
) {
}