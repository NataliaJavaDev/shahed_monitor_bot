package com.tgbot.shahedmonitorbot.context;

import com.tgbot.shahedmonitorbot.processing.MonitorMatch;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EventContextService {

    private final Map<String, ContextEntry> eventContexts =
            new ConcurrentHashMap<>();

    private final Duration contextTtl;

    public EventContextService(
            @Value("${app.monitor.context-ttl:15m}")
            Duration contextTtl
    ) {
        this.contextTtl = contextTtl;
    }

    public void saveContext(String chatId, MonitorMatch match) {
        if (chatId == null || chatId.isBlank() || match == null) {
            return;
        }

        ContextEntry contextEntry = new ContextEntry(
                match,
                Instant.now()
        );

        eventContexts.put(chatId, contextEntry);
    }

    public Optional<MonitorMatch> getContext(String chatId) {
        if (chatId == null || chatId.isBlank()) {
            return Optional.empty();
        }

        ContextEntry contextEntry = eventContexts.get(chatId);

        if (contextEntry == null) {
            return Optional.empty();
        }

        if (isExpired(contextEntry)) {
            eventContexts.remove(chatId, contextEntry);
            return Optional.empty();
        }

        return Optional.of(contextEntry.match());
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

    private boolean isExpired(ContextEntry contextEntry) {
        Instant expiresAt = contextEntry.savedAt().plus(contextTtl);

        return !Instant.now().isBefore(expiresAt);
    }

    private record ContextEntry(
            MonitorMatch match,
            Instant savedAt
    ) {
    }
}