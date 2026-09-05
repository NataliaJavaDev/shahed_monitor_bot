package com.tgbot.shahedmonitorbot.processing;

public record MonitorMatch(
        String matchedTarget,
        String targetCategory,
        String direction,
        String matchedLocation,
        String locationCategory,
        MatchType matchType
) {
}