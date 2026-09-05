package com.tgbot.shahedmonitorbot.processing;

public record PreprocessedMessage(
        String cleanedText,
        boolean tooLongForLocalAnalysis
) {
}