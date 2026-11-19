package com.tgbot.shahedmonitorbot.alertapi.model;

import java.time.LocalDateTime;

/**
 * Стан самих опитувань API: коли востаннє вдалося, коли була помилка і яка.
 */
public record ApiCheckHealth(
    LocalDateTime lastSuccessAt,
    LocalDateTime lastErrorAt,
    String lastError,
    int consecutiveFailures
) {

    public static ApiCheckHealth initial() {
        return new ApiCheckHealth(null, null, null, 0);
    }

    public ApiCheckHealth success(LocalDateTime now) {
        return new ApiCheckHealth(now, lastErrorAt, lastError, 0);
    }

    public ApiCheckHealth failure(LocalDateTime now, String message) {
        return new ApiCheckHealth(lastSuccessAt, now, message, consecutiveFailures + 1);
    }
}