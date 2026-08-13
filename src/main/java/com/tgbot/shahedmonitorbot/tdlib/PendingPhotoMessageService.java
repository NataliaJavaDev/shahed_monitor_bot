package com.tgbot.shahedmonitorbot.tdlib;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PendingPhotoMessageService {

    private final Map<Integer, PendingPhotoMessage> pendingMessages =
            new ConcurrentHashMap<>();

    public void save(
            int fileId,
            PendingPhotoMessage message
    ) {
        pendingMessages.put(fileId, message);
    }

    public PendingPhotoMessage get(int fileId) {
        return pendingMessages.get(fileId);
    }

    public PendingPhotoMessage remove(int fileId) {
        return pendingMessages.remove(fileId);
    }
}