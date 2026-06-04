package com.tgbot.shahedmonitorbot.alertapi.dto;

import java.util.List;

public record RegionAlertDto(
        String regionId,
        String regionType,
        String regionName,
        List<ActiveAlertDto> activeAlerts
) {
}