package com.tgbot.shahedmonitorbot.monitoring.source;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatInfoService {

    private final Map<String, String> titles = new ConcurrentHashMap<>();

    public void saveTitle(String chatId, String title) {
        
        if (chatId == null || chatId.isBlank()) {
            return;
        }

        if (title == null || title.isBlank()) {
            return;
        }

        titles.put(chatId, title);
    }

    public String getTitle(String chatId) {
        return titles.getOrDefault(chatId, "Невідоме джерело");
    }
}