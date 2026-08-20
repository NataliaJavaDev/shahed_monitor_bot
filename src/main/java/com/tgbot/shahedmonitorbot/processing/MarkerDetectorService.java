package com.tgbot.shahedmonitorbot.processing;

import com.tgbot.shahedmonitorbot.util.TextNormalizer;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MarkerDetectorService {

    public Optional<String> findMatchedMarker(String text, List<String> markers) {

        if (text == null || text.isBlank()) {
            return Optional.empty();
        }

        String normalizedText = TextNormalizer.normalize(text);

        return markers.stream()
                .filter(marker ->
                    normalizedText.contains(TextNormalizer.normalize(marker))
                )
                .findFirst();
    }
}