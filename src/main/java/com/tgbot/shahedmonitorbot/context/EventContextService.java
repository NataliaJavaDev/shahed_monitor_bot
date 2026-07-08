package com.tgbot.shahedmonitorbot.context;

import com.tgbot.shahedmonitorbot.processing.MonitorMatch;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EventContextService {

    private final Map<String, MonitorMatch> eventContexts =
            new ConcurrentHashMap<>();

    public void saveContext(String chatId, MonitorMatch match) {
        if (chatId == null || chatId.isBlank() || match == null) {
            return;
        }

        eventContexts.put(chatId, match);
    }

    public Optional<MonitorMatch> getContext(String chatId) {
        if (chatId == null || chatId.isBlank()) {
            return Optional.empty();
        }

        return Optional.ofNullable(eventContexts.get(chatId));
    }

    public void clearContext(String chatId) {
        if (chatId == null || chatId.isBlank()) {
            return;
        }

        eventContexts.remove(chatId);
    }

    public void clearAll() {
        eventContexts.clear();
    }
}