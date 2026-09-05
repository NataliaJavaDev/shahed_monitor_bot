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
        "⚠️ Категорію не знайдено"
    ),

    CATEGORIES_NOT_FOUND(
        "⚠️ Категорій не знайдено"
    ),

    VALUE_NOT_FOUND(
        "📋⚠️ Значення не знайдено"
    ),

    SOURCE_NOT_FOUND(
        "⚠️ Джерело не знайдено"
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

    UNKNOWN_CATEGORY(
        "❌ Не вдалося визначити категорію"
    ),

    VALUE_CAN_NOT_BE_EMPTY(
        "⚠️ Значення не може бути порожнім"
    ),

    LIST_IS_EMPTY(
        "⚠️ Список значень порожній"
    ),

    CATEGORY_IS_CREATED(
        "✅ Категорію «%s» успішно створено"
    ),

    CATEGORY_IS_REMOVED(
        "✅ Категорію успішно видалено"
    ),

    CATEGORY_ALREADY_EXISTS(
        "⚠️ Така категорія вже існує"
    ),

    VALUE_ALREADY_EXISTS(
        "⚠️ Таке значення вже існує"
    ),

    STATE_IS_ALREADY_ACTUAL(
        "⚠️ Стан вже актуальний"
    ),

    ENTER_NEW_VALUE(
        "Введіть нове значення:"
    ),

    REMOVE_VALUE(
        "Введіть значення, яке потрібно видалити:"
    ),

    VALUE_IS_ADDED(
        "✅ Значення «%s» успішно додано"
    ),

    VALUE_IS_REMOVED(
        "✅ Значення «%s» успішно видалено"
    ),

    ENTER_NEW_VALUE_FOR_CATEGORY(
        "Введіть нове значення для категорії «%s»:"
    ),

    REMOVE_VALUE_FOR_CATEGORY(
        "Введіть значення, яке потрібно видалити з категорії «%s»:"
    ),

    ENTER_NEW_CATEGORY(
        "Введіть назву нової категорії:"
    ),

    CHUSE_CATEGORY_TO_DELETE(
        "➖ Оберіть категорію, яку потрібно видалити:"
    ),

    ADMIN_MENU(
        "⚙️ Адмін-панель\n\nОберіть дію:"
    ),

    KEYWORDS_MENU(
        "🔑 Ключові слова\n\nОберіть розділ:"
    ),

    DIRECTIONS_MENU(
        "🧭 Напрямки\n\nОберіть дію:"
    ),

    ATTENTION_MENU(
        "⚠️ Увага\n\nОберіть дію:"
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

    MONITORING_ENABLED(
        "✅ Моніторинг увімкнено"
    ),

    MONITORING_DISABLED(
        "⛔ Моніторинг вимкнено"
    ),

    SOURCE_IGNORED(
        "⛔ Джерело ігнорується"
    ),

    ACTIVE_SOURCES_NOT_FOUND(
        "📡⚠️ Активних джерел не знайдено"
    ),

    NEW_SOURCES_NOT_FOUND(
        "📡🆕 Нових джерел не знайдено"
    ),

    IGNORED_SOURCES_NOT_FOUND(
        "📡⛔ Ігнорованих джерел не знайдено"
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