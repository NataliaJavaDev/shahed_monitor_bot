package com.tgbot.shahedmonitorbot.tdlib.history;

import java.time.LocalDateTime;

public record TdHistoryMessage(

        long messageId,
        LocalDateTime dateTime,
        String text
) {
}