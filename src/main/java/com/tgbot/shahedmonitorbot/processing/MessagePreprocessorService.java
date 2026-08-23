package com.tgbot.shahedmonitorbot.processing;

import org.springframework.stereotype.Service;

import com.tgbot.shahedmonitorbot.config.AppProperties;
import com.tgbot.shahedmonitorbot.util.TextNormalizer;

import java.util.Arrays;
import java.util.List;

@Service
public class MessagePreprocessorService {

    private static final int MAX_TEXT_LENGTH_FOR_LOCAL_ANALYSIS = 350;

    private final AppProperties appProperties;

    public MessagePreprocessorService(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public PreprocessedMessage preprocess(String text) {

        if (text == null || text.isBlank()) {
            return new PreprocessedMessage(null, true);
        }

        String cleanedText = cleanupCommonNoise(text);

        if (cleanedText.isBlank()) {
            return new PreprocessedMessage(null, true);
        }

        boolean tooLongForLocalAnalysis = cleanedText.length() > MAX_TEXT_LENGTH_FOR_LOCAL_ANALYSIS;

        return new PreprocessedMessage(cleanedText, tooLongForLocalAnalysis);
    }

    private String cleanupCommonNoise(String text) {

        List<String> cleanedLines = Arrays.stream(text.split("\\R"))
            .map(String::trim)
            .filter(line -> !line.isBlank())
            .filter(line -> !isCommonNoiseLine(line))
            .toList();

        return TextNormalizer.normalize(
            String.join("\n", cleanedLines)
        );
    }

    private boolean isCommonNoiseLine(String line) {
        
        String normalizedLine = TextNormalizer.normalize(line);

        return normalizedLine.startsWith("http://")
            || normalizedLine.startsWith("https://")
            || appProperties.monitor().noiseMarkers()
                .stream()
                .map(TextNormalizer::normalize)
                .anyMatch(normalizedLine::contains)
            || normalizedLine.matches(".*\\b\\d{16}\\b.*");
    }
}