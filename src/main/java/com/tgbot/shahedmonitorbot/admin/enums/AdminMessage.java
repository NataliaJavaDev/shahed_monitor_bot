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

    ADD_TARGET_REQUEST(
        "Надішліть ціль, яку потрібно додати."
    ),

    REMOVE_TARGET_REQUEST(
        "Надішліть ціль, яку потрібно видалити."
    ),

    EMPTY_TARGETS(
        "Список цілей порожній."
    ),

    TARGET_ADDED(
        "Ціль додано: %s"
    ),

    TARGET_REMOVED(
        "Ціль видалено: %s"
    ),

    TARGET_NOT_FOUND(
        "Ціль не знайдено."
    ),

    TARGET_ADD_FAILED(
        "Не вдалося додати ціль. Можливо, вона вже існує."
    ),

    SHOW_TARGETS(
        "Поточні цілі:\n\n%s"
    ),

    TARGETS_MENU_TITLE(
        "🎯 Цілі\n\nОберіть дію:"
    ),

    ADD_LOCATION_REQUEST(
        "Надішліть локацію, яку потрібно додати."
    ),

    REMOVE_LOCATION_REQUEST(
        "Надішліть локацію, яку потрібно видалити."
    ),

    EMPTY_LOCATIONS(
        "Список локацій порожній."
    ),

    LOCATION_ADDED(
        "Локацію додано: %s"
    ),

    LOCATION_REMOVED(
        "Локацію видалено: %s"
    ),

    LOCATION_NOT_FOUND(
        "Локацію не знайдено."
    ),

    LOCATION_ADD_FAILED(
        "Не вдалося додати локацію. Можливо, вона вже існує."
    ),

    SHOW_LOCATIONS(
        "Поточні локації:\n\n%s"
    ),

    LOCATIONS_MENU_TITLE(
        "📍 Локації\n\nОберіть дію:"
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

    ALERT_MENU_TITLE(
        "🚨 Керування тривогами\n\nОберіть тип сповіщення:"
    ),

    COMING_SOON(
        "Цей розділ ще знаходиться у стадії розробки, очікуйте його найближчим часом."
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