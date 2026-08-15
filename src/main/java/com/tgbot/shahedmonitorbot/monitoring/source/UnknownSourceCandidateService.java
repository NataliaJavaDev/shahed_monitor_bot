package com.tgbot.shahedmonitorbot.monitoring.source;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UnknownSourceCandidateService {

    private final Map<String, UnknownSourceCandidate> candidates = new ConcurrentHashMap<>();

    public void register(String chatId, String title, String text) {

        if (chatId == null || chatId.isBlank()) {
            return;
        }

        if (!chatId.startsWith("-")) {
            return;
        }

        Instant now = Instant.now();

        candidates.compute(chatId, (id, existing) -> {

            if (existing == null) {
                return new UnknownSourceCandidate(
                        id,
                        safeTitle(title),
                        safeText(text),
                        now,
                        now
                );
            }

            return new UnknownSourceCandidate(
                    existing.chatId(),
                    safeTitle(title),
                    safeText(text),
                    existing.firstSeenAt(),
                    now
            );
        });
    }

    public List<UnknownSourceCandidate> getAll() {
        return candidates.values().stream()
                .sorted(
                        (first, second) ->
                            second.lastSeenAt().compareTo(first.lastSeenAt())
                )
                .toList();
    }

    public UnknownSourceCandidate findByChatId(String chatId) {

        if (chatId == null || chatId.isBlank()) {
            return null;
        }

        return candidates.get(chatId);
    }

    public boolean contains(String chatId) {
        return candidates.containsKey(chatId);
    }

    public void remove(String chatId) {
        candidates.remove(chatId);
    }

    private String safeTitle(String title) {
        
        if (title == null || title.isBlank()) {
            return "Невідоме джерело";
        }

        return title.trim();
    }

    private String safeText(String text) {

        if (text == null || text.isBlank()) {
            return "[без тексту]";
        }

        return text.length() > 220
                ? text.substring(0, 220) + "..."
                : text;
    }
}