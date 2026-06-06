package com.tgbot.shahedmonitorbot.alertapi.model;

import com.tgbot.shahedmonitorbot.manualalert.ManualAlertType;

import java.time.LocalDateTime;
import java.util.List;

public record ApiAlertStatus(
        ManualAlertType type,
        boolean districtAlertActive,
        List<String> activeDangerRegionNames,
        LocalDateTime checkedAt
) {
}