package com.tgbot.shahedmonitorbot.monitoring.source;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MonitoredSourceService {

    private final List<MonitoredSource> sources = new ArrayList<>();

    public List<MonitoredSource> getAllSources() {
        return List.copyOf(sources);
    }

    public boolean addSource(String chatId, String title) {
        boolean exists = sources.stream()
                .anyMatch(source -> source.chatId().equals(chatId));

        if (exists) {
            return false;
        }

        sources.add(new MonitoredSource(chatId, title, true));
        return true;
    }

    public boolean removeSource(String chatId) {
        return sources.removeIf(source -> source.chatId().equals(chatId));
    }
}