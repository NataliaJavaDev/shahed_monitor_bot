package com.tgbot.shahedmonitorbot.admin.enums;

public enum AdminCommand {
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

    public boolean startsWith(String input) {
        return input.startsWith(value + " ");
    }
}