package com.tgbot.shahedmonitorbot.alertapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ActiveAlertLevelDto(
        String alertLevel
) {
}