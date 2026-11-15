package com.tgbot.shahedmonitorbot.alertapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RegionAlertDto(
        String regionId,
        String regionType,
        String regionName,
        List<ActiveAlertDto> activeAlerts
) {
}