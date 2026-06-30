package com.tgbot.shahedmonitorbot.processing;

import com.tgbot.shahedmonitorbot.config.AppProperties;
import com.tgbot.shahedmonitorbot.util.TextNormalizer;
import org.springframework.stereotype.Service;

@Service
public class MessageIntentDetectorService {

    private final AppProperties appProperties;

    public MessageIntentDetectorService(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public MessageIntent detect(String text) {
        if (text == null || text.isBlank()) {
            return MessageIntent.UNKNOWN;
        }

        String normalizedText = TextNormalizer.normalize(text);

        return appProperties.monitor().messageIntents()
                .stream()
                .filter(intentConfig -> matchesAnyAlias(normalizedText, intentConfig))
                .map(AppProperties.MessageIntentConfig::intent)
                .map(this::toMessageIntent)
                .findFirst()
                .orElse(MessageIntent.UNKNOWN);
    }

    private MessageIntent toMessageIntent(String value) {
        if (value == null || value.isBlank()) {
            return MessageIntent.UNKNOWN;
        }

        try {
            return MessageIntent.valueOf(value);
        } catch (IllegalArgumentException e) {
            return MessageIntent.UNKNOWN;
        }
    }

    private boolean matchesAnyAlias(
            String normalizedText,
            AppProperties.MessageIntentConfig intentConfig
    ) {
        if (intentConfig.aliases() == null || intentConfig.aliases().isEmpty()) {
            return false;
        }

        return intentConfig.aliases()
                .stream()
                .map(TextNormalizer::normalize)
                .anyMatch(normalizedText::contains);
    }
}