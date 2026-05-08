package com.swaply.chatservice.service;

import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class OnlineUserTracker {

    private static final long HEARTBEAT_TTL_MS = 90_000L;

    private final ConcurrentHashMap<UUID, AtomicInteger> onlineUsers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Long> heartbeatUntil = new ConcurrentHashMap<>();

    public boolean markOnline(UUID userId) {
        if (userId == null) {
            return false;
        }
        boolean wasOnline = isOnline(userId);

        onlineUsers.compute(userId, (key, count) -> {
            if (count == null) {
                return new AtomicInteger(1);
            }
            count.incrementAndGet();
            return count;
        });

        return !wasOnline && isOnline(userId);
    }

    public boolean markOffline(UUID userId) {
        if (userId == null) {
            return false;
        }
        boolean wasOnline = isOnline(userId);

        onlineUsers.computeIfPresent(userId, (key, count) -> {
            if (count.decrementAndGet() <= 0) {
                return null;
            }
            return count;
        });

        return wasOnline && !isOnline(userId);
    }

    public boolean markHeartbeat(UUID userId) {
        if (userId == null) {
            return false;
        }

        boolean wasOnline = isOnline(userId);
        heartbeatUntil.put(userId, System.currentTimeMillis() + HEARTBEAT_TTL_MS);
        return !wasOnline && isOnline(userId);
    }

    public boolean clearHeartbeat(UUID userId) {
        if (userId == null) {
            return false;
        }

        boolean wasOnline = isOnline(userId);
        heartbeatUntil.remove(userId);
        return wasOnline && !isOnline(userId);
    }

    public boolean isOnline(UUID userId) {
        if (userId == null) {
            return false;
        }

        AtomicInteger sessions = onlineUsers.get(userId);
        if (sessions != null && sessions.get() > 0) {
            return true;
        }

        Long heartbeatExpiry = heartbeatUntil.get(userId);
        if (heartbeatExpiry == null) {
            return false;
        }

        if (heartbeatExpiry <= System.currentTimeMillis()) {
            heartbeatUntil.remove(userId, heartbeatExpiry);
            return false;
        }

        return true;
    }
}
