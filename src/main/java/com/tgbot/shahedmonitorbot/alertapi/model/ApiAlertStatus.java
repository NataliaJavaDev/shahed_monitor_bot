package com.tgbot.shahedmonitorbot.alertapi.model;

import java.time.LocalDateTime;

public record ApiAlertStatus(
        boolean districtAlertActive,
        boolean highRiskActive,
        LocalDateTime checkedAt
) {
}