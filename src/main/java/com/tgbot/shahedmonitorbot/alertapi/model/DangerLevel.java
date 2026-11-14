package com.tgbot.shahedmonitorbot.alertapi.model;

/**
 * Порядок констант важливий: більший ordinal = вищий рівень.
 */
public enum DangerLevel {
    NONE,
    YELLOW,
    RED;

    /**
     * "Yellow" -> YELLOW. "Red" та будь-яке невідоме або порожнє значення -> RED
     * (безпечніше показати вищий рівень, ніж пропустити тривогу).
     */
    public static DangerLevel fromApi(String value) {
        if (value != null && value.trim().equalsIgnoreCase("yellow")) {
            return YELLOW;
        }
        return RED;
    }
}