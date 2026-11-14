package com.tgbot.shahedmonitorbot.alertapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ActiveAlertDto(
        String regionId,
        String regionType,
        String type,
        List<ActiveAlertLevelDto> activeAlertLevels
) {
}