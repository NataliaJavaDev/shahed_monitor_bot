package com.tgbot.shahedmonitorbot.processing;

public enum MatchType {

    TARGET_AND_LOCATION("🎯 Ціль + Локація"),

    LOCATION_ONLY("📍 Лише локація"),

    DIRECTION_AND_LOCATION("🧭 Напрямок + Локація");

    private final String displayName;

    MatchType(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}