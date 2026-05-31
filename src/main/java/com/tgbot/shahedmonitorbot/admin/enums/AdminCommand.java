package com.tgbot.shahedmonitorbot.admin.enums;

public enum AdminCommand {

    START("/start"),
    ADMIN("/admin"),
    KEYWORDS("/keywords"),
    ADD_KEYWORD("/add_keyword"),
    REMOVE_KEYWORD("/remove_keyword");

    private final String value;

    AdminCommand(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public boolean matches(String input) {
        return value.equals(input);
    }

    public static AdminCommand fromText(String text) {
        for (AdminCommand command : values()) {
            if (command.matches(text)) {
                return command;
            }
        }
        return null;
    }
}