package com.tgbot.shahedmonitorbot.alertapi.model;

import java.time.LocalDateTime;

public record ApiAlertStatus(
        DangerLevel districtLevel,
        boolean highRiskActive,
        LocalDateTime checkedAt
) {

    /** Для сумісності зі старим кодом: тривога є, якщо рівень не NONE. */
    public boolean districtAlertActive() {
        return districtLevel != DangerLevel.NONE;
    }
}
