package com.tgbot.shahedmonitorbot.admin.enums;

public enum AdminMessage { 

    WELCOME_MESSAGE(
            "Вітаю) \n\n" +
            "Я моніторинговий чат бот) \n\n" +
            "Щоб перейти у режим адміна натисніть " + AdminCommand.ADMIN.value()
    ),

    NO_ACCESS(
            "У вас немає доступу до адмін-команд."
    ),

    UNKNOWN_COMMAND(
            "Невідома команда. Напишіть /admin"
    ),

    ADD_KEYWORD_REQUEST(
            "Надішліть ключове слово, яке потрібно додати."
    ),

    REMOVE_KEYWORD_REQUEST(
            "Надішліть ключове слово, яке потрібно видалити."
    ),

    EMPTY_KEYWORDS(
            "Список ключових слів порожній."
    ),

    ALERT_SENT(
            "✅ Сповіщення про тривогу відправлено."
    ),

    HIGH_RISK_SENT(
            "✅ Сповіщення про підвищену небезпеку відправлено."
    ),

    ALL_CLEAR_SENT(
            "✅ Сповіщення про відбій відправлено."
    ),
    
    COMING_SOON(
        "Цей розділ ще знаходиться у стадії розробки, очікуйте його найближчим часом."
    ),
    KEYWORD_ADDED("Ключове слово додано: %s"),
    KEYWORD_REMOVED("Ключове слово видалено: %s"),
    KEYWORD_NOT_FOUND("Ключове слово не знайдено."),
    KEYWORD_ADD_FAILED("Не вдалося додати ключове слово. Можливо, воно вже існує."),
    SHOW_KEYWORDS(
        "Поточні ключові слова: \n\n %s"
    ),
    ALERT_MENU_TITLE(
        "🚨 Керування тривогами\n\nОберіть тип сповіщення:"
    ),
    KEYWORDS_MENU_TITLE(
        "🔑 Ключові слова\n\nОберіть дію:"
    );

    private final String text;

    AdminMessage(String text) {
        this.text = text;
    }

    public String text() {
        return text;
    }

    public String format(Object... args) {
        return text.formatted(args);
    }
}