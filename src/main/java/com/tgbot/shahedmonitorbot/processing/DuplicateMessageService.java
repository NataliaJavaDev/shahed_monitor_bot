package com.tgbot.shahedmonitorbot.processing;

import com.tgbot.shahedmonitorbot.util.TextNormalizer;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class DuplicateMessageService {

    private final Set<String> processedMessages = new HashSet<>();

    public boolean isDuplicate(String text) {
        String normalizedText = TextNormalizer.normalize(text);

        if (normalizedText.isBlank()) {
            return true;
        }

        return !processedMessages.add(normalizedText);
    }
}