package com.tgbot.shahedmonitorbot.alertapi.model;

import java.time.LocalDateTime;
import java.util.List;

public record ApiAlertStatus(
        boolean districtAlertActive,
        boolean highRiskActive,
        List<String> activeDangerRegionNames,
        LocalDateTime checkedAt
) {

    public ApiAlertStatus {
        activeDangerRegionNames = activeDangerRegionNames == null
                ? List.of()
                : List.copyOf(activeDangerRegionNames);
    }
}