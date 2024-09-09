package com.Identity.Identity.service;

import com.Identity.Identity.SessionData;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SessionService {
    private final Map<UUID, SessionData> sessionStore = new HashMap<>();

    public void addSession(UUID sessionId, Integer userId, List<Integer> roleId) {
        sessionStore.put(sessionId, new SessionData(userId, roleId, new Date()));
    }

    public boolean isSessionValid(UUID sessionId) {
        System.out.println("qqqqq" + sessionId);
        return sessionStore.containsKey(sessionId);
    }

    public void invalidateSession(UUID sessionId) {
        sessionStore.remove(sessionId);
    }
}

