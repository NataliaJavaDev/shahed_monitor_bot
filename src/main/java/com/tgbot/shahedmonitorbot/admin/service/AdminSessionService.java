package com.tgbot.shahedmonitorbot.admin.service;

import com.tgbot.shahedmonitorbot.model.admin.AdminSession;
import com.tgbot.shahedmonitorbot.model.admin.AdminSessionState;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AdminSessionService {

    private final Map<Long, AdminSession> sessions = new ConcurrentHashMap<>();

    public AdminSessionState getState(Long userId) {
        return getSession(userId).getState();
    }

    public void setState(Long userId, AdminSessionState state) {
        getSession(userId).setState(state);
    }

    public String getPendingSourceId(Long userId) {
        return getSession(userId).getPendingSourceId();
    }

    public void setPendingSourceId(Long userId, String sourceId) {
        getSession(userId).setPendingSourceId(sourceId);
    }

    public void reset(Long userId) {
        sessions.remove(userId);
    }

    private AdminSession getSession(Long userId) {
        return sessions.computeIfAbsent(
                userId,
                id -> new AdminSession()
        );
    }
}