package com.tgbot.shahedmonitorbot.processing;

import com.tgbot.shahedmonitorbot.config.AppProperties;
import com.tgbot.shahedmonitorbot.util.TextNormalizer;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ThreatDetectorService {

    private static final String THREAT_CATEGORY = "Глобальна загроза";

    private final AppProperties appProperties;

    public ThreatDetectorService(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public Optional<ThreatMatch> findThreat(String text) {
        if (text == null || text.isBlank()) {
            return Optional.empty();
        }

        String normalizedText = TextNormalizer.normalize(text);

        // TODO:
        // Тимчасова заглушка.
        // До реалізації окремого GLOBAL_FORECAST
        // не пропускаємо локальні повідомлення
        // виду "на <місто>", "над <місто>" тощо.
        if (containsLocalDirection(normalizedText)) {
            return Optional.empty();
        }

        return appProperties.monitor().messageIntents()
                .stream()
                .filter(intentConfig ->
                        MessageIntent.THREAT_DETECTED.name()
                                .equals(intentConfig.intent())
                )
                .flatMap(intentConfig -> intentConfig.aliases().stream())
                .filter(alias ->
                        normalizedText.contains(TextNormalizer.normalize(alias))
                )
                .findFirst()
                .map(alias -> new ThreatMatch(
                        alias,
                        THREAT_CATEGORY
                ));
    }

    private boolean containsLocalDirection(String text) {

        return text.contains(" на ")
                || text.contains(" над ")
                || text.contains(" повз ")
                || text.contains(" до ")
                || text.contains(" курс на ");
    }
}