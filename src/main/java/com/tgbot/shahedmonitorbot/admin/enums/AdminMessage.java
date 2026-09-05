package com.tgbot.shahedmonitorbot.admin.enums;

public enum AdminMessage {

    WELCOME_MESSAGE(
        "Вітаю) \n\n" +
        "Я моніторинговий чат бот) \n\n" +
        "Щоб перейти у режим адміна натисніть " + AdminCommand.ADMIN.value()
    ),

    NO_ACCESS(
        "Немає доступу"
    ),

    NO_CORRECT_ACTION(
        "Некоректна дія"
    ),

    NO_CORRECT_CATEGORY(
        "Некоректна категорія"
    ),

    NO_CORRECT_VOCAB(
        "Некоректний словник"
    ),

    CATEGORY_NOT_FOUND(
        "Категорію не знайдено"
    ),

    CATEGORIES_NOT_FOUND(
        "Категорій не знайдено"
    ),

    UNKNOWN_COMMAND(
        "Невідома команда. Напишіть " + AdminCommand.ADMIN.value()
    ),

    UNKNOWN_ACTION(
        "Невідома дія"
    ),

    UNKNOWN_ACTION_VOCAB(
        "❌ Не вдалося визначити операцію зі словником. Спробуйте ще раз"
    ),

    VALUE_CAN_NOT_BE_EMPTY(
        "⚠️ Значення не може бути порожнім"
    ),

    CATEGORY_ALREADY_EXISTS(
        "⚠️ Така категорія вже існує"
    ),

    KEYWORDS_MENU(
        "🔑 Ключові слова\n\nОберіть розділ:"
    ),

    DIRECTIONS_MENU(
        "🧭 Напрямки\n\nОберіть дію:"
    ),

    ATTENTION_MENU(
        "⚠️ Attention words\n\nОберіть дію:"
    ),

    ENTER_NEW_VALUE(
        "Введіть нове значення:"
    ),

    REMOVE_VALUE(
        "Введіть значення, яке потрібно видалити:"
    ),

    GLOBAL_THREATS_MENU(
        "🌐 Глобальні загрози\n\nОберіть дію:"
    ),

    FORECAST_MENU(
        "🔮 Прогноз\n\nОберіть дію:"
    ),

    NOISE_MENU(
        "✂️🔊 Шум\n\nОберіть дію:"
    ),

    STATUS_MENU(
        "📊 Статус\n\nОберіть дію:"
    ),

    SOURCES_MENU(
        "📡 Джерела моніторингу\n\nОберіть дію:"
    ),

    ALERT_MENU_TITLE(
        "🚨 Керування тривогами\n\nОберіть тип сповіщення:"
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