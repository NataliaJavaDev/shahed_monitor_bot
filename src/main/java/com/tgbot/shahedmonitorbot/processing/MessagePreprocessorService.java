package com.tgbot.shahedmonitorbot.processing;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class MessagePreprocessorService {

    private static final int MAX_TEXT_LENGTH_FOR_LOCAL_ANALYSIS = 450;

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

        return String.join("\n", cleanedLines);
    }

    private boolean isCommonNoiseLine(String line) {
        String lower = line.toLowerCase();

        return lower.startsWith("http://")
                || lower.startsWith("https://")
                || lower.contains("підтримати канал")
                || lower.contains("підтримати")
                || lower.contains("донат")
                || lower.contains("monobank")
                || lower.contains("send.monobank")
                || lower.matches(".*\\b\\d{16}\\b.*");
    }
}