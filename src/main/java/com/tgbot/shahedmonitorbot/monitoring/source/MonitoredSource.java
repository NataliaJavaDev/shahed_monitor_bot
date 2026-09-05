package com.tgbot.shahedmonitorbot.monitoring.source;

import com.tgbot.shahedmonitorbot.admin.enums.SourceStatus;

public record MonitoredSource(
    String chatId,
    String title,
    SourceStatus status
) {
}