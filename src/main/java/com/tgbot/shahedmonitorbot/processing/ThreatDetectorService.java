package com.tgbot.shahedmonitorbot.processing;

import com.tgbot.shahedmonitorbot.admin.dictionary.DictionaryStorage;
import com.tgbot.shahedmonitorbot.admin.dictionary.DictionaryIntent;
import com.tgbot.shahedmonitorbot.util.TextNormalizer;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ThreatDetectorService {

    private static final String THREAT_CATEGORY = "Глобальна загроза";
    private final DictionaryStorage storage;

    public ThreatDetectorService(DictionaryStorage storage) {
        this.storage = storage;
    }

    public Optional<ThreatMatch> findThreat(String text) {

        if (text == null || text.isBlank()) {
            return Optional.empty();
        }

        String normalizedText = TextNormalizer.normalize(text);

        // TODO:
        // Тимчасово не пропускаємо локальні повідомлення
        // до THREAT_DETECTED.
        // Після реалізації повноцінної логіки GLOBAL_FORECAST
        // цей фільтр потрібно переглянути.
        if (containsLocalDirection(normalizedText)) {
            return Optional.empty();
        }

        return storage.get()
            .dictionaries()
            .messageIntents()
            .stream()
            .filter(intent -> MessageIntent.THREAT_DETECTED.name().equals(intent.intent()))
            .flatMap(intent -> intent.aliases().stream())
            .filter(alias -> normalizedText.contains( TextNormalizer.normalize(alias)))
            .findFirst()
            .map(alias -> new ThreatMatch(alias, THREAT_CATEGORY));
    }

    private boolean containsLocalDirection(String text) {

        return text.contains(" на ")
            || text.contains(" над ")
            || text.contains(" повз ")
            || text.contains(" до ")
            || text.contains(" курс на ");
    }
}