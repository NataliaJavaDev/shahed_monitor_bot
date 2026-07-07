package com.tgbot.shahedmonitorbot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.tgbot.shahedmonitorbot.config.AppProperties.AlertApi;
import com.tgbot.shahedmonitorbot.config.AppProperties.Tdlib;

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
        List<TargetCategory> targetCategories,
        List<String> directions,
        List<LocationCategory> locationCategories,
        List<MessageIntentConfig> messageIntents,
        List<AttentionWordConfig> attentionWords,
        List<Source> sources,
        List<String> ignoredChatIds
    ) {
    }

    public record TargetCategory(
        String category,
        List<String> aliases
    ) {}

    public record LocationCategory(
        String category,
        List<String> aliases
    ) {}

    public record MessageIntentConfig(
        String intent,
        List<String> aliases
    ) {}

    public record AttentionWordConfig(
        String category,
        List<String> aliases
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
            String filesDirectory,
            String libraryPath,
            String authCode
    ) {}
}