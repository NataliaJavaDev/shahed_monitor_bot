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

    public synchronized List<MonitoredSource> getAllSources() {
        return List.copyOf(sources);
    }

    public synchronized List<MonitoredSource> getActiveSources() {
        return sources.stream()
                .filter(MonitoredSource::active)
                .toList();
    }

    public synchronized List<MonitoredSource> getIgnoredSources() {
        return sources.stream()
                .filter(source -> !source.active())
                .toList();
    }

    public synchronized MonitoredSource findByChatId(String chatId) {
        if (chatId == null || chatId.isBlank()) {
            return null;
        }

        return sources.stream()
                .filter(source -> source.chatId().equals(chatId))
                .findFirst()
                .orElse(null);
    }

    public synchronized boolean isKnown(String chatId) {
        return findByChatId(chatId) != null;
    }

    public synchronized boolean isMonitored(String chatId) {
        MonitoredSource source = findByChatId(chatId);
        return source != null && source.active();
    }

    public synchronized boolean addSource(
            String chatId,
            String title,
            boolean active
    ) {
        if (chatId == null || chatId.isBlank()) {
            return false;
        }

        if (findByChatId(chatId) != null) {
            return false;
        }

        sources.add(new MonitoredSource(
                chatId,
                safeTitle(title),
                active
        ));

        return true;
    }

    public synchronized boolean addSource(
            String chatId,
            String title
    ) {
        return addSource(chatId, title, true);
    }

    public synchronized boolean addActiveSource(
            String chatId,
            String title
    ) {
        return addSource(chatId, title, true);
    }

    public synchronized boolean addIgnoredSource(
            String chatId,
            String title
    ) {
        return addSource(chatId, title, false);
    }

    public synchronized boolean enableSource(String chatId) {
        return changeActiveState(chatId, true);
    }

    public synchronized boolean ignoreSource(String chatId) {
        return changeActiveState(chatId, false);
    }

    private boolean changeActiveState(
            String chatId,
            boolean active
    ) {
        for (int index = 0; index < sources.size(); index++) {
            MonitoredSource current = sources.get(index);

            if (!current.chatId().equals(chatId)) {
                continue;
            }

            if (current.active() == active) {
                return false;
            }

            sources.set(
                    index,
                    new MonitoredSource(
                            current.chatId(),
                            current.title(),
                            active
                    )
            );

            return true;
        }

        return false;
    }

    private String safeTitle(String title) {
        if (title == null || title.isBlank()) {
            return "Невідоме джерело";
        }

        return title.trim();
    }
}