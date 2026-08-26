package com.tgbot.shahedmonitorbot.admin.service;

import com.tgbot.shahedmonitorbot.admin.enums.DictionaryAction;
import com.tgbot.shahedmonitorbot.admin.enums.DictionaryType;
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
        return sessions.computeIfAbsent(userId, id -> new AdminSession());
    }

    public DictionaryType getDictionaryType(Long userId) {
        return getSession(userId).getDictionaryType();
    }
    
    public void setDictionaryType(Long userId, DictionaryType dictionaryType) {
        getSession(userId).setDictionaryType(dictionaryType);
    }
    
    public DictionaryAction getDictionaryAction(Long userId) {
        return getSession(userId).getDictionaryAction();
    }
    
    public void setDictionaryAction(Long userId, DictionaryAction dictionaryAction) {
        getSession(userId).setDictionaryAction(dictionaryAction);
    }

    public String getSelectedCategory(Long userId) {
	    return getSession(userId).getSelectedCategory();
    }

    public void setSelectedCategory(Long userId, String category) {
	    getSession(userId).setSelectedCategory(category);
    }
}