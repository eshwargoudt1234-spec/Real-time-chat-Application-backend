package com.chatapp.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Remembers which WebSocket sessions belong to which user.
 * A user can have several tabs open -> several sessions. They are "online" while at least one is open.
 */
@Service
public class PresenceService {

    private final Map<String, String> sessionToUser = new ConcurrentHashMap<>();   // sessionId -> username

    /** @return true if this is the user's FIRST open session (user just came online) */
    public synchronized boolean userConnected(String sessionId, String username) {
        boolean firstSession = !isOnline(username);
        sessionToUser.put(sessionId, username);
        return firstSession;
    }

    /** @return the username if that was their LAST session (user is now offline), otherwise null */
    public synchronized String userDisconnected(String sessionId) {
        String username = sessionToUser.remove(sessionId);
        if (username != null && !isOnline(username)) {
            return username;
        }
        return null;
    }

    public boolean isOnline(String username) {
        return sessionToUser.containsValue(username);
    }
}
