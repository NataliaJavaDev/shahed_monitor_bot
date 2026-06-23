package com.tgbot.shahedmonitorbot.processing;

public record MonitorMatch(
        String target,
        String direction,
        String location
) {
}