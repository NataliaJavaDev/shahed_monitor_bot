package com.tgbot.shahedmonitorbot.processing;

import com.tgbot.shahedmonitorbot.admin.dictionary.DictionaryIntent;
import com.tgbot.shahedmonitorbot.admin.dictionary.DictionaryStorage;
import com.tgbot.shahedmonitorbot.admin.service.AttentionWordAdminService;
import com.tgbot.shahedmonitorbot.util.TextNormalizer;
import org.springframework.stereotype.Service;

@Service
public class MessageIntentDetectorService {

    private final DictionaryStorage storage;
    private final AttentionWordAdminService attentionWordAdminService;

    public MessageIntentDetectorService(
        DictionaryStorage storage,
        AttentionWordAdminService attentionWordAdminService
    ) {
        this.storage = storage;
        this.attentionWordAdminService = attentionWordAdminService;
    }

    public MessageIntent detect(String text) {

        if (text == null || text.isBlank()) {
            return MessageIntent.UNKNOWN;
        }

        if (attentionWordAdminService.findAttentionWord(text) != null) {
            return MessageIntent.ATTENTION;
        }

        String normalizedText = TextNormalizer.normalize(text);

        return storage.get()
            .dictionaries()
            .messageIntents()
            .stream()
            .filter(intent -> matchesAnyAlias(normalizedText, intent))
            .map(DictionaryIntent::intent)
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

    private boolean matchesAnyAlias(String normalizedText, DictionaryIntent intent) {

        if (intent.aliases() == null || intent.aliases().isEmpty()) {
            return false;
        }

        return intent.aliases()
            .stream()
            .map(TextNormalizer::normalize)
            .anyMatch(normalizedText::contains);
    }
}