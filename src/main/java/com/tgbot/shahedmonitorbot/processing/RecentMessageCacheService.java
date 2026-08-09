package com.tgbot.shahedmonitorbot.processing;

import java.time.LocalDateTime;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.time.Duration;

import org.springframework.stereotype.Service;

import com.tgbot.shahedmonitorbot.tdlib.history.TdHistoryMessage;

@Service
public class RecentMessageCacheService {
    private final Map<String, Deque<TdHistoryMessage>> cache =
        new ConcurrentHashMap<>();

    public void add(
        String chatId,
        TdHistoryMessage message
    ) {
        Deque<TdHistoryMessage> messages =
        cache.computeIfAbsent(
                chatId,
                id -> new ConcurrentLinkedDeque<>()
        );

        messages.addLast(message);

        cleanup(messages);
    }

    private void cleanup(
            Deque<TdHistoryMessage> messages
    ) {
        LocalDateTime limit = LocalDateTime.now().minusHours(2);

        while (!messages.isEmpty()
                && messages.peekFirst()
                        .dateTime()
                        .isBefore(limit)) {

            messages.removeFirst();
        }
    }

    public Map<String, List<TdHistoryMessage>> getHistory(
            Duration lookback
    ) {

        LocalDateTime limit =
                LocalDateTime.now().minus(lookback);

        Map<String, List<TdHistoryMessage>> result =
                new HashMap<>();

        cache.forEach((chatId, messages) -> {

            List<TdHistoryMessage> recentMessages =
                    messages.stream()
                            .filter(message ->
                                    message.dateTime().isAfter(limit)
                            )
                            .toList();

            result.put(chatId, recentMessages);
        
            if (!recentMessages.isEmpty()) {
                result.put(chatId, recentMessages);
            }
        });

        return result;
    }
}
