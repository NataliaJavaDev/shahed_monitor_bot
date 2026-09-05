package com.tgbot.shahedmonitorbot.processing;

import com.tgbot.shahedmonitorbot.admin.dictionary.DictionaryStorage;
import com.tgbot.shahedmonitorbot.util.TextNormalizer;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class MessagePreprocessorService {

    private static final int MAX_TEXT_LENGTH_FOR_LOCAL_ANALYSIS = 350;
    private final DictionaryStorage storage;

    public MessagePreprocessorService(DictionaryStorage storage) {
        this.storage = storage;
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

        return TextNormalizer.normalize(String.join("\n", cleanedLines));
    }

    private boolean isCommonNoiseLine(String line) {

        String normalizedLine = TextNormalizer.normalize(line);

        return normalizedLine.startsWith("http://")
            || normalizedLine.startsWith("https://")
            || storage.get()
                .dictionaries()
                .noise()
                .stream()
                .map(TextNormalizer::normalize)
                .anyMatch(normalizedLine::contains)
            || normalizedLine.matches(".*\\b\\d{16}\\b.*");
    }
}