package com.tgbot.shahedmonitorbot.admin.enums;

public enum AdminButton {

    TARGETS("🎯 Цілі"),
    SHOW_TARGETS("📋 Показати цілі"),
    ADD_TARGET("➕ Додати ціль"),
    REMOVE_TARGET("➖ Видалити ціль"),

    KEYWORDS("🔑 Ключові слова"),
    LOCATIONS("📍 Локації"),
    SHOW_LOCATIONS("📋 Показати локації"),
    ADD_LOCATION("➕ Додати локацію"),
    REMOVE_LOCATION("➖ Видалити локацію"),

    DIRECTIONS("🧭 Напрямки"),
    SHOW_DIRECTIONS("📋 Показати напрямки"),
    ADD_DIRECTION("➕ Додати напрямок"),
    REMOVE_DIRECTION("➖ Видалити напрямок"),

    ALERTS("🚨 Керування тривогами"),
    ALERT("🚨 Тривога"),
    HIGH_RISK("⚠️ Підвищена небезпека"),
    ALL_CLEAR("✅ Відбій"),

    STATUS("📊 Статус"),
    BOT_STATUS("🤖 Статус бота"),
    ALERT_STATUS("📡 Статус тривоги"),

    SETTINGS("⚙️ Налаштування"),
    API_CONTROL("🔌 API-керування"),

    SOURCES("📡 Джерела моніторингу"),
    SHOW_SOURCES("📋 Показати джерела"),
    ADD_SOURCE("➕ Додати джерело"),
    REMOVE_SOURCE("➖ Видалити джерело"),

    BACK("⬅️ Назад");

    private final String text;

    AdminButton(String text) {
        this.text = text;
    }

    public String text() {
        return text;
    }

    public boolean matches(String input) {
        return text.equals(input);
    }

    public static AdminButton fromText(String text) {

    for (AdminButton button : values()) {
        if (button.text.equals(text)) {
            return button;
        }
    }

    return null;
    }
}