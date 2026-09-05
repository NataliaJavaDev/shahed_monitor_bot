package com.tgbot.shahedmonitorbot.admin.enums;

public enum AdminCommand {

    START("/start"),
    ADMIN("/admin");

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