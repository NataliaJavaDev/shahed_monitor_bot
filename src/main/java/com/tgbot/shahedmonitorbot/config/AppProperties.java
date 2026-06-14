package com.tgbot.shahedmonitorbot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        Telegram telegram,
        Monitor monitor,
        AlertApi alertApi,
        Tdlib tdlib
) {

    public record Telegram(
        String botToken,
        String targetChannelId
    ) {}

    public record Monitor(
        List<String> keywords,
        List<Source> sources
    ) {}

    public record Source(
        String chatId,
        String title,
        Boolean active
    ) {}

    public record AlertApi(
        String baseUrl,
        String apiKey,
        String alarmRegionId,
        List<String> dangerRegionIds,
        Long pollingDelayMs
    ) {}

    public record Tdlib(
            Integer apiId,
            String apiHash,
            String phoneNumber,
            String databaseDirectory,
            String filesDirectory
    ) {}
}