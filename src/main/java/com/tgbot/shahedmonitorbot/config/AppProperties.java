package com.tgbot.shahedmonitorbot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
    Telegram telegram,
    Monitor monitor,
    AlertApi alertApi,
    Tdlib tdlib,
    Dictionary dictionary
) {

    public record Telegram(
        String botToken,
        String targetChannelId,
        String debugChannelId
    ) {}

    public record Monitor(
        Integer duplicateTtlMinutes
    ) {
    }

    public record AlertApi(
        String baseUrl,
        String apiKey,
        String alarmRegionId,
        String highRiskRegionId,
        Long pollingDelayMs
    ) {}

    public record Tdlib(
        Integer apiId,
        String apiHash,
        String phoneNumber,
        String databaseDirectory,
        String filesDirectory,
        String libraryPath,
        String authCode
    ) {}

    public record Dictionary(
        String file
    ) {}
}