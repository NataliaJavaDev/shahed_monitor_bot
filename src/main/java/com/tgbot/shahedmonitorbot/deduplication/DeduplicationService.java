package com.tgbot.shahedmonitorbot.deduplication;

import com.tgbot.shahedmonitorbot.util.TextNormalizer;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DeduplicationService {

    private final Map<String, Instant> seenMessages = new ConcurrentHashMap<>();

    public boolean isDuplicate(String text) {
        String normalizedText = TextNormalizer.normalize(text);
        String key = Integer.toHexString(normalizedText.hashCode());

        if (seenMessages.containsKey(key)) {
            return true;
        }

        seenMessages.put(key, Instant.now());
        return false;
    }
}