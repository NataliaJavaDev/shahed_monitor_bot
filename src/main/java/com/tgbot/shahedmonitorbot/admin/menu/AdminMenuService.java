package com.tgbot.shahedmonitorbot.admin.menu;

import com.tgbot.shahedmonitorbot.admin.enums.AdminButton;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.List;

@Service
public class AdminMenuService {

    public String mainMenuText() {
        return "⚙️ Адмін-панель\n\nОберіть дію:";
    }

    public ReplyKeyboardMarkup mainReplyKeyboard() {

        KeyboardRow row1 = new KeyboardRow();
        row1.add(AdminButton.KEYWORDS.text());
        row1.add(AdminButton.ALERTS.text());

        KeyboardRow row2 = new KeyboardRow();
        row2.add(AdminButton.STATUS.text());
        row2.add(AdminButton.SETTINGS.text());

        return ReplyKeyboardMarkup.builder()
                .keyboard(List.of(row1, row2))
                .resizeKeyboard(true)
                .build();
    }

    public ReplyKeyboardMarkup keywordsReplyKeyboard() {

        KeyboardRow row1 = new KeyboardRow();
        row1.add(AdminButton.TARGETS.text());
        row1.add(AdminButton.LOCATIONS.text());

        KeyboardRow row2 = new KeyboardRow();
        row2.add(AdminButton.DIRECTIONS.text());

        KeyboardRow row3 = new KeyboardRow();
        row2.add(AdminButton.BACK.text());

        return ReplyKeyboardMarkup.builder()
                .keyboard(List.of(row1, row2, row3))
                .resizeKeyboard(true)
                .build();
    }

    public ReplyKeyboardMarkup targetsReplyKeyboard() {

        KeyboardRow row1 = new KeyboardRow();
        row1.add(AdminButton.SHOW_TARGETS.text());

        KeyboardRow row2 = new KeyboardRow();
        row2.add(AdminButton.ADD_TARGET.text());
        row2.add(AdminButton.REMOVE_TARGET.text());

        KeyboardRow row3 = new KeyboardRow();
        row3.add(AdminButton.BACK.text());

        return ReplyKeyboardMarkup.builder()
                .keyboard(List.of(row1, row2, row3))
                .resizeKeyboard(true)
                .build();
    }

    public ReplyKeyboardMarkup locationsReplyKeyboard() {

        KeyboardRow row1 = new KeyboardRow();
        row1.add(AdminButton.SHOW_LOCATIONS.text());

        KeyboardRow row2 = new KeyboardRow();
        row2.add(AdminButton.ADD_LOCATION.text());
        row2.add(AdminButton.REMOVE_LOCATION.text());

        KeyboardRow row3 = new KeyboardRow();
        row3.add(AdminButton.BACK.text());

        return ReplyKeyboardMarkup.builder()
                .keyboard(List.of(row1, row2, row3))
                .resizeKeyboard(true)
                .build();
    }

    public ReplyKeyboardMarkup directionsReplyKeyboard() {

        KeyboardRow row1 = new KeyboardRow();
        row1.add(AdminButton.SHOW_DIRECTIONS.text());

        KeyboardRow row2 = new KeyboardRow();
        row2.add(AdminButton.ADD_DIRECTION.text());
        row2.add(AdminButton.REMOVE_DIRECTION.text());

        KeyboardRow row3 = new KeyboardRow();
        row3.add(AdminButton.BACK.text());

        return ReplyKeyboardMarkup.builder()
                .keyboard(List.of(row1, row2, row3))
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

    public ReplyKeyboardMarkup settingsReplyKeyboard() {

        KeyboardRow row1 = new KeyboardRow();
        row1.add(AdminButton.API_CONTROL.text());

        KeyboardRow row2 = new KeyboardRow();
        row2.add(AdminButton.SOURCES.text());

        KeyboardRow row3 = new KeyboardRow();
        row3.add(AdminButton.BACK.text());

        return ReplyKeyboardMarkup.builder()
                .keyboard(List.of(row1, row2, row3))
                .resizeKeyboard(true)
                .build();
    }

    public ReplyKeyboardMarkup sourcesReplyKeyboard() {

        KeyboardRow row1 = new KeyboardRow();
        row1.add(AdminButton.SHOW_SOURCES.text());

        KeyboardRow row2 = new KeyboardRow();
        row2.add(AdminButton.ADD_SOURCE.text());
        row2.add(AdminButton.REMOVE_SOURCE.text());

        KeyboardRow row3 = new KeyboardRow();
        row3.add(AdminButton.BACK.text());

        return ReplyKeyboardMarkup.builder()
                .keyboard(List.of(row1, row2, row3))
                .resizeKeyboard(true)
                .build();
    }
}