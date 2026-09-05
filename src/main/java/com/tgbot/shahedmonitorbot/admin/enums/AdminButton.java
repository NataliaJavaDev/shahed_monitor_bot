package com.tgbot.shahedmonitorbot.admin.enums;

public enum AdminButton {

    KEYWORDS("🔑 Ключові слова"),
    TARGETS("🎯 Цілі"),
    TARG_ICON("🎯 ")
    LOCATIONS("📍 Локації"),
    LOCAT_ICON("📍 "),
    DIRECTIONS("🧭 Напрямки"),
    DIRECTIONS_TITLE("🧭 Напрямки моніторингу"),
    ATTENTION("⚠️ Увага"),
    GLOBAL_THREAT("🌐 Глобальні загрози"),
    FORECAST("🔮 Прогноз"),
    NOISE("✂️🔊 Шум"),
    VALUES("📋 Значення"),
    CATEGORY("Категорія: *"),

    SHOW_VALUES("📋 Показати значення"),
    ADD_VALUE("➕ Додати значення"),
    REMOVE_VALUE("➖ Видалити значення"),

    CATEGORY_ADD("➕ Додати категорію"),
    CATEGORY_REMOVE("➖ Видалити категорію"),

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

    ENABLE_MONITORING("✅ Увімкнути моніторинг"),
    DISABLE_MONITORING("⛔ Вимкнути моніторинг"),
    IGNORE_MONITORING("⛔ Ігнорувати"),
    
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