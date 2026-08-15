package com.tgbot.shahedmonitorbot.util;

public class TextNormalizer {

    public static String normalize(String text) {
        
        if (text == null) {
            return "";
        }

        return text
                .toLowerCase()
                .replace("ґ", "г")
                .replaceAll("[\\n\\r\\t]+", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}