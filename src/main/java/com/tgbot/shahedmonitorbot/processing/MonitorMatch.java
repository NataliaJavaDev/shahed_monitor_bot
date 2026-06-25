package com.tgbot.shahedmonitorbot.processing;

public record MonitorMatch(
        String target,
        String targetCategory,
        String direction,
        String location
) {
}