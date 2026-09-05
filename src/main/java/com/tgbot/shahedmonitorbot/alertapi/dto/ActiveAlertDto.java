package com.tgbot.shahedmonitorbot.alertapi.dto;

public record ActiveAlertDto(
        String regionId,
        String regionType,
        String type
) {
}