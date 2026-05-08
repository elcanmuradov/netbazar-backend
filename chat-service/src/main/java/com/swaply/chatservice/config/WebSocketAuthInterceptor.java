package com.swaply.chatservice.config;

import com.swaply.chatservice.service.ChatService;
import com.swaply.chatservice.service.OnlineUserTracker;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class StompUser implements Principal {
    private final String name;
    public StompUser(String name) { this.name = name; }
    @Override public String getName() { return name; }
}

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final OnlineUserTracker onlineUserTracker;
    private final ChatService chatService;

    public WebSocketAuthInterceptor(OnlineUserTracker onlineUserTracker, @Lazy ChatService chatService) {
        this.onlineUserTracker = onlineUserTracker;
        this.chatService = chatService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            List<String> authorization = accessor.getNativeHeader("Authorization");
            if (authorization != null && !authorization.isEmpty()) {
                String token = authorization.get(0);
                if (token.startsWith("Bearer ")) {
                    token = token.substring(7);
                }
                try {
                    String[] chunks = token.split("\\.");
                    if (chunks.length > 1) {
                        Base64.Decoder decoder = Base64.getUrlDecoder();
                        String payload = new String(decoder.decode(chunks[1]));

                        // Extract userId from JSON (Basic regex parsing)
                        Matcher matcher = Pattern.compile("\"userId\"\\s*:\\s*\"([^\"]+)\"").matcher(payload);
                        if (matcher.find()) {
                            String userIdStr = matcher.group(1);
                            accessor.getSessionAttributes().put("userId", UUID.fromString(userIdStr));
                        }

                        // Extract sub (username/email) from JSON
                        matcher = Pattern.compile("\"sub\"\\s*:\\s*\"([^\"]+)\"").matcher(payload);
                        if (matcher.find()) {
                            String username = matcher.group(1);
                            accessor.getSessionAttributes().put("username", username);
                            accessor.setUser(new StompUser(username));
                        }

                        UUID userId = (UUID) accessor.getSessionAttributes().get("userId");
                        boolean becameOnline = onlineUserTracker.markOnline(userId);
                        if (becameOnline) {
                            chatService.notifyPresenceToContacts(userId, true, "Bearer " + token);
                        }
                        
                        // Store token for downstream Feign calls
                        accessor.getSessionAttributes().put("token", "Bearer " + token);
                    }
                } catch (Exception e) {
                    System.err.println("Token processing error: " + e.getMessage());
                }
            }
        } else if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
            UUID userId = accessor.getSessionAttributes() != null ? (UUID) accessor.getSessionAttributes().get("userId") : null;
            String token = accessor.getSessionAttributes() != null ? (String) accessor.getSessionAttributes().get("token") : null;
            boolean becameOffline = onlineUserTracker.markOffline(userId);
            if (becameOffline) {
                chatService.notifyPresenceToContacts(userId, false, token);
            }
        }
        return message;
    }
}
