package com.tgbot.shahedmonitorbot.admin.menu;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import com.tgbot.shahedmonitorbot.admin.enums.AdminButton;

import java.util.List;

@Service
public class AdminMenuService {

    public String mainMenuText() {
        return "⚙️ Адмін-панель\n\nОберіть дію:";
    }

    public InlineKeyboardMarkup mainMenuKeyboard() {
        InlineKeyboardButton keywordsButton = InlineKeyboardButton.builder()
                .text(AdminButton.KEYWORDS.text())
                .callbackData("KEYWORDS_MENU")
                .build();

        InlineKeyboardButton alertButton = InlineKeyboardButton.builder()
                .text(AdminButton.ALERTS.text())
                .callbackData("ALERT_MENU")
                .build();

        InlineKeyboardButton statusButton = InlineKeyboardButton.builder()
                .text(AdminButton.STATUS.text())
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
                .text(AdminButton.SHOW_KEYWORDS.text())
                .callbackData("SHOW_KEYWORDS")
                .build();

        InlineKeyboardButton addButton = InlineKeyboardButton.builder()
                .text(AdminButton.ADD_KEYWORD.text())
                .callbackData("ADD_KEYWORD")
                .build();

        InlineKeyboardButton removeButton = InlineKeyboardButton.builder()
                .text(AdminButton.REMOVE_KEYWORD.text())
                .callbackData("REMOVE_KEYWORD")
                .build();

        InlineKeyboardButton backButton = InlineKeyboardButton.builder()
                .text(AdminButton.BACK.text())
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
                .text(AdminButton.ALERT.text())
                .callbackData("MANUAL_ALERT")
                .build();

        InlineKeyboardButton highRiskButton = InlineKeyboardButton.builder()
                .text(AdminButton.HIGH_RISK.text())
                .callbackData("MANUAL_HIGH_RISK")
                .build();

        InlineKeyboardButton allClearButton = InlineKeyboardButton.builder()
                .text(AdminButton.ALL_CLEAR.text())
                .callbackData("MANUAL_ALL_CLEAR")
                .build();

        InlineKeyboardButton backButton = InlineKeyboardButton.builder()
                .text(AdminButton.BACK.text())
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
    row1.add(AdminButton.KEYWORDS.text());
    row1.add(AdminButton.ALERTS.text());

    KeyboardRow row2 = new KeyboardRow();
    row2.add(AdminButton.STATUS.text());
    row2.add(AdminButton.SETTINGS.text());

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
    row1.add(AdminButton.ALERT.text());

    KeyboardRow row2 = new KeyboardRow();
    row2.add(AdminButton.HIGH_RISK.text());

    KeyboardRow row3 = new KeyboardRow();
    row3.add(AdminButton.ALL_CLEAR.text());

    KeyboardRow row4 = new KeyboardRow();
    row4.add(AdminButton.BACK.text());

    return ReplyKeyboardMarkup.builder()
            .keyboard(List.of(row1, row2, row3, row4))
            .resizeKeyboard(true)
            .build();
}

public ReplyKeyboardMarkup keywordsReplyKeyboard() {
    KeyboardRow row1 = new KeyboardRow();
    row1.add(AdminButton.SHOW_KEYWORDS.text());

    KeyboardRow row2 = new KeyboardRow();
    row2.add(AdminButton.ADD_KEYWORD.text());
    row2.add(AdminButton.REMOVE_KEYWORD.text());

    KeyboardRow row3 = new KeyboardRow();
    row3.add(AdminButton.BACK.text());

    return ReplyKeyboardMarkup.builder()
            .keyboard(List.of(row1, row2, row3))
            .resizeKeyboard(true)
            .build();
}

public ReplyKeyboardMarkup settingsReplyKeyboard() {
    KeyboardRow row1 = new KeyboardRow();
    row1.add(AdminButton.API_CONTROL.text());

    KeyboardRow row2 = new KeyboardRow();
    row2.add(AdminButton.BACK.text());

    return ReplyKeyboardMarkup.builder()
            .keyboard(List.of(row1, row2))
            .resizeKeyboard(true)
            .build();
}

public ReplyKeyboardMarkup statusReplyKeyboard() {
    KeyboardRow row1 = new KeyboardRow();
    row1.add(AdminButton.BOT_STATUS.text());
    row1.add(AdminButton.ALERT_STATUS.text());

    KeyboardRow row2 = new KeyboardRow();
    row2.add(AdminButton.BACK.text());

    return ReplyKeyboardMarkup.builder()
            .keyboard(List.of(row1, row2))
            .resizeKeyboard(true)
            .build();
}

}