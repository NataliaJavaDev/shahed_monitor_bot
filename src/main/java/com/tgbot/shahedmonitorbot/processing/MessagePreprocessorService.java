package com.tgbot.shahedmonitorbot.processing;

import org.springframework.stereotype.Service;
import com.tgbot.shahedmonitorbot.util.TextNormalizer;

import java.util.Arrays;
import java.util.List;

@Service
public class MessagePreprocessorService {

    private static final int MAX_TEXT_LENGTH_FOR_LOCAL_ANALYSIS = 350;

    private static final List<String> COMMON_NOISE_MARKERS = List.of(
        "підтримати канал",
            "підтримати",
            "донат",
            "monobank",
            "send.monobank",
            "номер картки",
            "картки банки",
            "посилання на банку",
            "підтримати збір",
            "підтримайте збір",
            "підтримайте, будь ласка",
            "підтримайте будь ласка",
            "дякую за ваші фото",
            "хто бажає надіслати своє",
            "ставте ❤️",
            "ставте ❤",
            "антистрес",
            "чому",
            "чому тривога",
            "небо_без_тривог",
            "надішліть фото",
            "надіслати фото",
            "підтримати канал",
            "чому тривога"

    );

    public PreprocessedMessage preprocess(String text) {
        if (text == null || text.isBlank()) {
            return new PreprocessedMessage(null, true);
        }

        String cleanedText = cleanupCommonNoise(text);

        if (cleanedText.isBlank()) {
            return new PreprocessedMessage(null, true);
        }

        boolean tooLongForLocalAnalysis =
                cleanedText.length() > MAX_TEXT_LENGTH_FOR_LOCAL_ANALYSIS;

        return new PreprocessedMessage(
                cleanedText,
                tooLongForLocalAnalysis
        );
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
                || COMMON_NOISE_MARKERS.stream().anyMatch(normalizedLine::contains)
                || normalizedLine.matches(".*\\b\\d{16}\\b.*");
    }
}