package com.tgbot.shahedmonitorbot.monitoring.reason;

import java.util.Set;

public record AlertReasonItem(

    String category,
    Set<String> threats
) {
}