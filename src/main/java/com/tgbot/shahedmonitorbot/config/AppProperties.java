package com.tgbot.shahedmonitorbot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        Telegram telegram,
        Monitor monitor,
        AlertApi alertApi
) {

    public record Telegram(
            String botToken,
            String targetChannelId
    ) {}

    public record Monitor(
            List<String> keywords
    ) {}

public record AlertApi(
        String baseUrl,
        String apiKey,
        String alarmRegionId,
        List<String> dangerRegionIds,
        Long pollingDelayMs
) {}
}