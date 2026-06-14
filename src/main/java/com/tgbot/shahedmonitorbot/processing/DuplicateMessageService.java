package com.tgbot.shahedmonitorbot.processing;

import com.tgbot.shahedmonitorbot.util.TextNormalizer;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DuplicateMessageService {

    private static final Duration TTL = Duration.ofHours(1);

    private final Map<String, Instant> processedMessages =
            new ConcurrentHashMap<>();

    public boolean isDuplicate(String text) {

        String normalizedText = TextNormalizer.normalize(text);

        if (normalizedText.isBlank()) {
            return true;
        }

        cleanupExpired();

        Instant existing = processedMessages.get(normalizedText);

        if (existing != null) {
            return true;
        }

        processedMessages.put(normalizedText, Instant.now());

        return false;
    }

    private void cleanupExpired() {

        Instant threshold = Instant.now().minus(TTL);

        processedMessages.entrySet()
                .removeIf(entry ->
                        entry.getValue().isBefore(threshold));
    }
}