package com.tgbot.shahedmonitorbot.filter;

import com.tgbot.shahedmonitorbot.config.AppProperties;
import com.tgbot.shahedmonitorbot.util.TextNormalizer;
import org.springframework.stereotype.Service;

@Service
public class KeywordMatcherService {

    private final AppProperties properties;

    public KeywordMatcherService(AppProperties properties) {
        this.properties = properties;
    }

    public boolean isRelevant(String text) {
        String normalizedText = TextNormalizer.normalize(text);

        return properties.monitor().keywords().stream()
                .map(TextNormalizer::normalize)
                .anyMatch(normalizedText::contains);
    }
}