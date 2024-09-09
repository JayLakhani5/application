package com.Identity.Identity.service;

import com.Identity.Identity.SessionData;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SessionService {
    private final Map<String, SessionData> sessionStore = new HashMap<>();

    public void addSession(String sessionId, Integer userId, List<Integer> roleId) {
        sessionStore.put(sessionId, new SessionData(userId, roleId, new Date()));
    }

    public boolean isSessionValid(String sessionId) {
        System.out.println("qqqqq" + sessionId);
        return sessionStore.containsKey(sessionId);
    }

    public void invalidateSession(String sessionId) {
        sessionStore.remove(sessionId);
    }
}

