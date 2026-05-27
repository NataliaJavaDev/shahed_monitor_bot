package com.tgbot.shahedmonitorbot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        Telegram telegram,
        Monitor monitor
) {
    public record Telegram(
            String botToken,
            String targetChatId
    ) {}

    public record Monitor(
            List<String> keywords
    ) {}
}