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

        return buildKeyboard(List.of(row1, row2));
    }

    public ReplyKeyboardMarkup keywordsReplyKeyboard() {

        KeyboardRow row1 = new KeyboardRow();
        row1.add(AdminButton.TARGETS.text());
        row1.add(AdminButton.LOCATIONS.text());

        KeyboardRow row2 = new KeyboardRow();
        row2.add(AdminButton.DIRECTIONS.text());
        row2.add(AdminButton.ATTENTION.text());
        row2.add(AdminButton.NOISE.text());

        KeyboardRow row3 = new KeyboardRow();
        row3.add(AdminButton.GLOBAL_THREAT.text());
        row3.add(AdminButton.FORECAST.text());

        KeyboardRow row4 = new KeyboardRow();
        row4.add(AdminButton.MESSAGE_INTENTS.text());
        row4.add(AdminButton.BACK.text());

        return buildKeyboard(List.of(row1, row2, row3, row4));
    }

    public ReplyKeyboardMarkup targetsReplyKeyboard() {

        KeyboardRow row1 = new KeyboardRow();
        row1.add(AdminButton.SHOW_TARGETS.text());

        KeyboardRow row2 = new KeyboardRow();
        row2.add(AdminButton.ADD_TARGET.text());
        row2.add(AdminButton.REMOVE_TARGET.text());

        KeyboardRow row3 = new KeyboardRow();
        row3.add(AdminButton.BACK.text());

        return buildKeyboard(List.of(row1, row2, row3));
    }

    public ReplyKeyboardMarkup crudReplyKeyboard() {

        KeyboardRow row1 = new KeyboardRow();
        row1.add(AdminButton.SHOW_ATTENTIONS.text());

        KeyboardRow row2 = new KeyboardRow();
        row2.add(AdminButton.ADD_ATTENTION.text());
        row2.add(AdminButton.REMOVE_ATTENTION.text());

        KeyboardRow row3 = new KeyboardRow();
        row3.add(AdminButton.BACK.text());

        return buildKeyboard(List.of(row1, row2, row3));
    }

    public ReplyKeyboardMarkup locationsReplyKeyboard() {

        KeyboardRow row1 = new KeyboardRow();
        row1.add(AdminButton.SHOW_LOCATIONS.text());

        KeyboardRow row2 = new KeyboardRow();
        row2.add(AdminButton.ADD_LOCATION.text());
        row2.add(AdminButton.REMOVE_LOCATION.text());

        KeyboardRow row3 = new KeyboardRow();
        row3.add(AdminButton.BACK.text());

        return buildKeyboard(List.of(row1, row2, row3));
    }

    public ReplyKeyboardMarkup directionsReplyKeyboard() {

        KeyboardRow row1 = new KeyboardRow();
        row1.add(AdminButton.SHOW_DIRECTIONS.text());

        KeyboardRow row2 = new KeyboardRow();
        row2.add(AdminButton.ADD_DIRECTION.text());
        row2.add(AdminButton.REMOVE_DIRECTION.text());

        KeyboardRow row3 = new KeyboardRow();
        row3.add(AdminButton.BACK.text());

        return buildKeyboard(List.of(row1, row2, row3));
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

        return buildKeyboard(List.of(row1, row2, row3, row4));
    }

    public ReplyKeyboardMarkup statusReplyKeyboard() {

        KeyboardRow row1 = new KeyboardRow();
        row1.add(AdminButton.BOT_STATUS.text());
        row1.add(AdminButton.ALERT_STATUS.text());

        KeyboardRow row2 = new KeyboardRow();
        row2.add(AdminButton.BACK.text());

        return buildKeyboard(List.of(row1, row2));
    }

    public ReplyKeyboardMarkup settingsReplyKeyboard() {

        KeyboardRow row1 = new KeyboardRow();
        row1.add(AdminButton.SOURCES.text());

        KeyboardRow row2 = new KeyboardRow();
        row2.add(AdminButton.BACK.text());

        return buildKeyboard(List.of(row1, row2));
    }

    public ReplyKeyboardMarkup sourcesReplyKeyboard() {

        KeyboardRow row1 = new KeyboardRow();
        row1.add(AdminButton.ACTIVE_SOURCES.text());

        KeyboardRow row2 = new KeyboardRow();
        row2.add(AdminButton.NEW_SOURCES.text());

        KeyboardRow row3 = new KeyboardRow();
        row3.add(AdminButton.IGNORED_SOURCES.text());

        KeyboardRow row4 = new KeyboardRow();
        row4.add(AdminButton.BACK.text());

        return buildKeyboard(List.of(row1, row2, row3, row4));
    }

    private ReplyKeyboardMarkup buildKeyboard(List<KeyboardRow> rows) {
        return ReplyKeyboardMarkup.builder()
            .keyboard(rows)
            .resizeKeyboard(true)
            .build();
    }
}