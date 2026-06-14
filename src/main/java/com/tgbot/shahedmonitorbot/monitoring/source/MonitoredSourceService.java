package com.tgbot.shahedmonitorbot.monitoring.source;

import com.tgbot.shahedmonitorbot.config.AppProperties;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MonitoredSourceService {

    private final List<MonitoredSource> sources = new ArrayList<>();

    public MonitoredSourceService(AppProperties appProperties) {
        var configuredSources = appProperties.monitor().sources();

        if (configuredSources == null) {
            return;
        }

        configuredSources.forEach(source ->
                sources.add(new MonitoredSource(
                        source.chatId(),
                        source.title(),
                        Boolean.TRUE.equals(source.active())
                ))
        );
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