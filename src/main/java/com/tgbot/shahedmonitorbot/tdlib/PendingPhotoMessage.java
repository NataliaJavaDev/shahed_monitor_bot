package com.tgbot.shahedmonitorbot.tdlib;

import com.tgbot.shahedmonitorbot.processing.MessageAnalysis;

public record PendingPhotoMessage(
        String chatId,
        String sourceTitle,
        String originalText,
        MessageAnalysis analysis
) {
}