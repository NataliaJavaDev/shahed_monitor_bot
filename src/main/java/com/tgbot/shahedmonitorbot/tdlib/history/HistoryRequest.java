package com.tgbot.shahedmonitorbot.tdlib.history;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class HistoryRequest {

    private final String requestId;
    private final String chatId;
    private final LocalDateTime from;
    private long oldestMessageId;

    private final CompletableFuture<List<TdHistoryMessage>> future =
            new CompletableFuture<>();

    private final List<TdHistoryMessage> messages =
            new ArrayList<>();

    public HistoryRequest(
            String requestId,
            String chatId,
            LocalDateTime from
    ) {
        this.requestId = requestId;
        this.chatId = chatId;
        this.from = from;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getChatId() {
        return chatId;
    }

    public long getOldestMessageId() {
        return oldestMessageId;
    }

    public void setOldestMessageId(long oldestMessageId) {
        this.oldestMessageId = oldestMessageId;
    }

    public LocalDateTime getFrom() {
        return from;
    }

    public CompletableFuture<List<TdHistoryMessage>> getFuture() {
        return future;
    }

    public List<TdHistoryMessage> getMessages() {
        return messages;
    }
}