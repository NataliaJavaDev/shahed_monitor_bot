package com.tgbot.shahedmonitorbot.monitoring.source;

import java.time.Instant;

public record UnknownSourceCandidate(
        String chatId,
        String title,
        String lastText,
        Instant firstSeenAt,
        Instant lastSeenAt
) {
}