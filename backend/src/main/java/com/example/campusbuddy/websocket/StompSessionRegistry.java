package com.example.campusbuddy.websocket;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class StompSessionRegistry {
    private static final Set<Long> ONLINE_USERS = ConcurrentHashMap.newKeySet();

    @EventListener
    public void handleSessionConnect(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Long userId = getUserId(accessor.getUser());
        if (userId != null) {
            ONLINE_USERS.add(userId);
        }
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Long userId = getUserId(accessor.getUser());
        if (userId != null) {
            ONLINE_USERS.remove(userId);
        }
    }

    public static boolean isUserOnline(Long userId) {
        return userId != null && ONLINE_USERS.contains(userId);
    }

    public static int getOnlineUserCount() {
        return ONLINE_USERS.size();
    }

    private static Long getUserId(Principal principal) {
        if (principal == null) {
            return null;
        }
        try {
            return Long.parseLong(principal.getName());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
