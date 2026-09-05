package com.tgbot.shahedmonitorbot.admin.enums;

public enum AdminButton {

    KEYWORDS("🔑 Ключові слова"),


    SHOW_VALUES("📋 Показати значення"),
    ADD_VALUE("➕ Додати значення"),
    REMOVE_VALUE("➖ Видалити значення"),

    CATEGORY_ADD("➕ Додати категорію"),
    CATEGORY_REMOVE("➖ Видалити категорію"),



    TARGETS("🎯 Цілі"),
    SHOW_TARGETS("📋 Показати цілі"),
    ADD_TARGET("➕ Додати ціль"),
    REMOVE_TARGET("➖ Видалити ціль"),

    

    LOCATIONS("📍 Локації"),
    SHOW_LOCATIONS("📋 Показати локації"),
    ADD_LOCATION("➕ Додати локацію"),
    REMOVE_LOCATION("➖ Видалити локацію"),

    DIRECTIONS("🧭 Напрямки"),
    SHOW_DIRECTIONS("📋 Показати напрямки"),
    ADD_DIRECTION("➕ Додати напрямок"),
    REMOVE_DIRECTION("➖ Видалити напрямок"),

    ATTENTION("Увага"),
    SHOW_ATTENTIONS("📋 Показати слова"),
    ADD_ATTENTION("➕ Додати слово"),
    REMOVE_ATTENTION("➖ Видалити слово"),

    GLOBAL_THREAT("Глобальні загрози"),
    SHOW_GLOBAL_THREATS("📋 Показати слова"),
    ADD_GLOBAL_THREATS("➕ Додати слово"),
    REMOVE_GLOBAL_THREAT("➖ Видалити слово"),

    FORECAST("Прогноз"),
    SHOW_FORECASTS("📋 Показати слова"),
    ADD_FORECAST("➕ Додати слово"),
    REMOVE_FORECAST("➖ Видалити слово"),

    NOISE("Шум"),
    SHOW_NOISES("📋 Показати слова"),
    ADD_NOISE("➕ Додати слово"),
    REMOVE_NOISE("➖ Видалити слово"),

    MESSAGE_INTENTS("MESSAGE_INTENTS"),
    SHOW_MESSAGE_INTENTS("📋 Показати слова"),
    ADD_MESSAGE_INTENTS("➕ Додати слово"),
    REMOVE_MESSAGE_INTENTS("➖ Видалити слово"),

    ALERTS("🚨 Керування тривогами"),
    ALERT("🚨 Тривога"),
    HIGH_RISK("⚠️ Підвищена небезпека"),
    ALL_CLEAR("✅ Відбій"),

    STATUS("📊 Статус"),
    BOT_STATUS("🤖 Статус бота"),
    ALERT_STATUS("🚨 Статус тривоги"),

    SOURCES("📡 Джерела моніторингу"),

    ACTIVE_SOURCES("✅ Активні джерела"),
    NEW_SOURCES("🆕 Нові джерела"),
    IGNORED_SOURCES("⛔ Ігноровані джерела"),

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