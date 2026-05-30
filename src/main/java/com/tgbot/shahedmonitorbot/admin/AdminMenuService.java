package com.tgbot.shahedmonitorbot.admin;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.List;

@Service
public class AdminMenuService {

    public String mainMenuText() {
        return "⚙️ Адмін-панель\n\nОберіть дію:";
    }

    public InlineKeyboardMarkup mainMenuKeyboard() {
        InlineKeyboardButton keywordsButton = InlineKeyboardButton.builder()
                .text("🔑 Ключові слова")
                .callbackData("KEYWORDS_MENU")
                .build();

        InlineKeyboardButton alertButton = InlineKeyboardButton.builder()
                .text("🚨 Тривога")
                .callbackData("ALERT_MENU")
                .build();

        InlineKeyboardButton statusButton = InlineKeyboardButton.builder()
                .text("📊 Статус")
                .callbackData("STATUS")
                .build();

        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(
                        new InlineKeyboardRow(keywordsButton),
                        new InlineKeyboardRow(alertButton),
                        new InlineKeyboardRow(statusButton)
                ))
                .build();
    }

    public String keywordsMenuText() {
        return "🔑 Ключові слова\n\nОберіть дію:";
    }

    public InlineKeyboardMarkup keywordsMenuKeyboard() {
        InlineKeyboardButton showButton = InlineKeyboardButton.builder()
                .text("📋 Показати")
                .callbackData("SHOW_KEYWORDS")
                .build();

        InlineKeyboardButton addButton = InlineKeyboardButton.builder()
                .text("➕ Додати")
                .callbackData("ADD_KEYWORD")
                .build();

        InlineKeyboardButton removeButton = InlineKeyboardButton.builder()
                .text("➖ Видалити")
                .callbackData("REMOVE_KEYWORD")
                .build();

        InlineKeyboardButton backButton = InlineKeyboardButton.builder()
                .text("⬅️ Назад")
                .callbackData("MAIN_MENU")
                .build();

        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(
                        new InlineKeyboardRow(showButton),
                        new InlineKeyboardRow(addButton, removeButton),
                        new InlineKeyboardRow(backButton)
                ))
                .build();
    }

    public String alertMenuText() {
        return "🚨 Ручне керування тривогою\n\nОберіть тип повідомлення:";
    }

    public InlineKeyboardMarkup alertMenuKeyboard() {
        InlineKeyboardButton alertButton = InlineKeyboardButton.builder()
                .text("🚨 Тривога")
                .callbackData("MANUAL_ALERT")
                .build();

        InlineKeyboardButton highRiskButton = InlineKeyboardButton.builder()
                .text("⚠️ Підвищена небезпека")
                .callbackData("MANUAL_HIGH_RISK")
                .build();

        InlineKeyboardButton allClearButton = InlineKeyboardButton.builder()
                .text("✅ Відбій")
                .callbackData("MANUAL_ALL_CLEAR")
                .build();

        InlineKeyboardButton backButton = InlineKeyboardButton.builder()
                .text("⬅️ Назад")
                .callbackData("MAIN_MENU")
                .build();

        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(
                        new InlineKeyboardRow(alertButton),
                        new InlineKeyboardRow(highRiskButton),
                        new InlineKeyboardRow(allClearButton),
                        new InlineKeyboardRow(backButton)
                ))
                .build();
    }

    public ReplyKeyboardMarkup mainReplyKeyboard() {

    KeyboardRow row1 = new KeyboardRow();
    row1.add("🔑 Ключові слова");
    row1.add("🚨 Керування тривогами");

    KeyboardRow row2 = new KeyboardRow();
    row2.add("📊 Статус");
    row2.add("⚙️ Налаштування");

    return ReplyKeyboardMarkup.builder()
            .keyboard(List.of(
                    row1,
                    row2
            ))
            .resizeKeyboard(true)
            .build();
}

public ReplyKeyboardMarkup alertReplyKeyboard() {
    KeyboardRow row1 = new KeyboardRow();
    row1.add("🚨 Тривога");

    KeyboardRow row2 = new KeyboardRow();
    row2.add("⚠️ Підвищена небезпека");

    KeyboardRow row3 = new KeyboardRow();
    row3.add("✅ Відбій");

    KeyboardRow row4 = new KeyboardRow();
    row4.add("⬅️ Назад");

    return ReplyKeyboardMarkup.builder()
            .keyboard(List.of(row1, row2, row3, row4))
            .resizeKeyboard(true)
            .build();
}

public ReplyKeyboardMarkup keywordsReplyKeyboard() {
    KeyboardRow row1 = new KeyboardRow();
    row1.add("📋 Показати ключові слова");

    KeyboardRow row2 = new KeyboardRow();
    row2.add("➕ Додати ключове слово");
    row2.add("➖ Видалити ключове слово");

    KeyboardRow row3 = new KeyboardRow();
    row3.add("⬅️ Назад");

    return ReplyKeyboardMarkup.builder()
            .keyboard(List.of(row1, row2, row3))
            .resizeKeyboard(true)
            .build();
}
}