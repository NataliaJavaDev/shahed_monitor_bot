package com.tgbot.shahedmonitorbot.admin.enums;

public enum AdminButton {

    KEYWORDS("🔑 Ключові слова"),
    ALERTS("🚨 Керування тривогами"),
    STATUS("📊 Статус"),
    SETTINGS("⚙️ Налаштування"),

    BOT_STATUS("🤖 Статус бота"),
    ALERT_STATUS("📡 Статус тривоги"),

    SHOW_KEYWORDS("📋 Показати ключові слова"),
    ADD_KEYWORD("➕ Додати ключове слово"),
    REMOVE_KEYWORD("➖ Видалити ключове слово"),

    ALERT("🚨 Тривога"),
    HIGH_RISK("⚠️ Підвищена небезпека"),
    ALL_CLEAR("✅ Відбій"),

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