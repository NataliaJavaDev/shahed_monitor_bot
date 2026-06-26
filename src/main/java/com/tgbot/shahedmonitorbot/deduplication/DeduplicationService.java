package com.tgbot.shahedmonitorbot.deduplication;

import com.tgbot.shahedmonitorbot.processing.MonitorMatch;
import com.tgbot.shahedmonitorbot.util.TextNormalizer;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DeduplicationService {

    private static final Duration TTL = Duration.ofHours(1);

    private final Map<String, Instant> seenEvents = new ConcurrentHashMap<>();

    public boolean isDuplicate(MonitorMatch match) {
        String key = buildKey(match);

        if (key.isBlank()) {
            return true;
        }

        cleanupExpired();

        if (seenEvents.containsKey(key)) {
            return true;
        }

        seenEvents.put(key, Instant.now());
        return false;
    }

    private String buildKey(MonitorMatch match) {
        String targetCategory = normalizeOrFallback(match.targetCategory(), "NO_TARGET");
        String location = normalizeOrFallback(match.locationCategory(), "NO_LOCATION");

        return targetCategory + "::" + location;
    }

    private String normalizeOrFallback(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }

        return TextNormalizer.normalize(value);
    }

    private void cleanupExpired() {
        Instant threshold = Instant.now().minus(TTL);

        seenEvents.entrySet()
                .removeIf(entry -> entry.getValue().isBefore(threshold));
    }
}