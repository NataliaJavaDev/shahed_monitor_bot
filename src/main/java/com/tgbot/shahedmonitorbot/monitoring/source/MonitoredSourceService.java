package com.tgbot.shahedmonitorbot.monitoring.source;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MonitoredSourceService {

    private final List<MonitoredSource> sources = new ArrayList<>();

    public MonitoredSourceService() {

        sources.add(new MonitoredSource(
                "-5519048152",
                "Тест джерело моніторингу",
                true
        ));

        sources.add(new MonitoredSource(
                "-5539045370",
                "Тест БЦ новини",
                true
        ));
    }

    public List<MonitoredSource> getAllSources() {
        return List.copyOf(sources);
    }

    public MonitoredSource findByChatId(String chatId) {
        return sources.stream()
                .filter(source -> source.chatId().equals(chatId))
                .findFirst()
                .orElse(null);
    }

    public boolean isMonitored(String chatId) {
        return sources.stream()
                .anyMatch(source ->
                        source.active()
                        && source.chatId().equals(chatId));
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