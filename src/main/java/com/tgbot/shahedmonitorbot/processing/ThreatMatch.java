package com.tgbot.shahedmonitorbot.processing;

public record ThreatMatch(
        String matchedThreat,
        String threatCategory
) {
}