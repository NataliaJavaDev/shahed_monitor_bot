package com.tgbot.shahedmonitorbot.admin.enums;

public enum AdminCommand {

    START("/start"),
    ADMIN("/admin"),
    TARGETS("/targets"),
    ADD_TARGET("/add_target"),
    REMOVE_TARGET("/remove_target"),
    LOCATIONS("/locations"),
    ADD_LOCATION("/add_location"),
    REMOVE_LOCATION("/remove_location");

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