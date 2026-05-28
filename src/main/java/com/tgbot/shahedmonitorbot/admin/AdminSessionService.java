package com.tgbot.shahedmonitorbot.admin;

import com.tgbot.shahedmonitorbot.model.admin.AdminSessionState;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AdminSessionService {

    private final Map<Long, AdminSessionState> states = new ConcurrentHashMap<>();

    public AdminSessionState getState(Long userId) {
        return states.getOrDefault(userId, AdminSessionState.IDLE);
    }

    public void setState(Long userId, AdminSessionState state) {
        states.put(userId, state);
    }

    public void reset(Long userId) {
        states.put(userId, AdminSessionState.IDLE);
    }
}