package com.swaply.chatservice.service;

import com.swaply.chatservice.client.MediaClient;
import com.swaply.chatservice.client.UserClient;
import com.swaply.chatservice.dto.ConversationDto;
import com.swaply.chatservice.dto.MessageDto;
import com.swaply.chatservice.dto.user.UserDto;
import com.swaply.chatservice.entity.Message;
import com.swaply.chatservice.exception.NotFoundException;
import com.swaply.chatservice.repository.ChatRepository;
import com.swaply.chatservice.utils.enums.MessageStatus;
import com.swaply.chatservice.utils.enums.MessageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {
    private static final UUID SYSTEM_ADMIN_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private final UserClient userClient;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatRepository chatRepository;
    private final MediaClient mediaClient;
    private final OnlineUserTracker onlineUserTracker;

    public void sendMessageToUser(MessageDto incomingMessage, StompHeaderAccessor accessor) {
        Principal principal = accessor.getUser();
        if (principal == null) {
            throw new NotFoundException("User not authenticated");
        }
        String senderUsername = principal.getName();
        UUID senderId = (UUID) accessor.getSessionAttributes().get("userId");

        Optional.ofNullable(senderId).orElseThrow(() -> new NotFoundException("SenderId  is null."));
        Optional.ofNullable(senderUsername).orElseThrow(() -> new NotFoundException("SenderUsername is null."));

        incomingMessage.setSenderId(senderId);
        incomingMessage.setSentAt(LocalDateTime.now().plusHours(4));
        incomingMessage.setRead(false);

        UUID receiverId = Optional.ofNullable(incomingMessage.getReceiverId()).orElseThrow(() -> new NotFoundException("ReceiverId is null."));
        String token = (String) accessor.getSessionAttributes().get("token");

        // Removed role-based chat restriction to allow all users to talk to each other

        UserDto receiverUser = userClient.getUserById(receiverId, token).getData();

        if (receiverUser == null) {
            throw new IllegalArgumentException("Alıcı  tapılmadı: " + receiverId);
        }

        saveMessage(incomingMessage);

        messagingTemplate.convertAndSendToUser(
                receiverUser.getEmail(),
                "/queue/messages",
                incomingMessage
        );

        // Send ack to sender with persisted message id/state for delivery status UI.
        messagingTemplate.convertAndSendToUser(
            senderUsername,
            "/queue/messages",
            incomingMessage
        );
        log.info("Message sent to {}", incomingMessage.getSenderId());
    }

    private boolean isMarketRole(String roleText) {
        return roleText != null && roleText.toUpperCase().contains("MARKET");
    }

    public void saveMessage(MessageDto incomingMessage) {
        log.info("Saving message to user: {}", incomingMessage.getSenderId());
        Message message = Message.builder()
                .senderId(incomingMessage.getSenderId())
                .receiverId(incomingMessage.getReceiverId())
                .content(incomingMessage.getContent())
                .productId(incomingMessage.getProductId())
                .sentAt(incomingMessage.getSentAt())
                .messageType(incomingMessage.getMessageType())
                .isRead(incomingMessage.isRead())
                .build();
        chatRepository.save(message);
        incomingMessage.setId(message.getId());
        log.info("Saved message: {}", incomingMessage);
    }

    public List<MessageDto> getMessagesWithUser(UUID userId, UUID otherUserId) {
        log.info("Getting messages with user {} and other user {}", userId, otherUserId);
        List<Message> messages = chatRepository.findBySenderIdAndReceiverIdOrReceiverIdAndSenderIdOrderBySentAtAsc(userId, otherUserId, userId, otherUserId);
        return messages.stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<ConversationDto> getConversations(UUID userId, String token) {
        log.info("Getting conversations with info for user {}", userId);

        List<Message> messages = chatRepository.findConversationsByUserId(userId).stream()
            .sorted(Comparator.comparing(Message::getSentAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
            .toList();

        return messages.stream()
                .map(msg -> msg.getSenderId().equals(userId) ? msg.getReceiverId() : msg.getSenderId())
                .distinct()
                .map(otherUserId -> {
                    String name = "İstifadəçi " + otherUserId.toString().substring(0, 5);
                    if (SYSTEM_ADMIN_ID.equals(otherUserId)) {
                        name = "Swaply Admin";
                    }
                    try {
                        UserDto userDto = userClient.getUserById(otherUserId, token).getData();
                        if (userDto != null && userDto.getName() != null) {
                            name = userDto.getName();
                        }
                    } catch (Exception e) {
                        log.warn("Failed to fetch user name for {}: {}", otherUserId, e.getMessage());
                    }

                    // Find last message for this conversation
                    Optional<Message> lastMsgOpt = messages.stream()
                            .filter(m -> m.getSenderId().equals(otherUserId) || m.getReceiverId().equals(otherUserId))
                            .findFirst();

                        int unreadCount = chatRepository.countBySenderIdAndReceiverIdAndIsReadFalse(otherUserId, userId);

                    return ConversationDto.builder()
                            .userId(otherUserId)
                            .name(name)
                            .lastMessage(lastMsgOpt.map(Message::getContent).orElse(""))
                            .lastMessageTime(lastMsgOpt.map(Message::getSentAt).orElse(null))
                            .unreadCount(unreadCount)
                            .isOnline(onlineUserTracker.isOnline(otherUserId))
                            .build();
                })
                .collect(Collectors.toList());
    }

    public void reportMessage(String messageId) {
        Message message = chatRepository.findById(messageId).orElseThrow(() -> new NotFoundException("MessageId: " + messageId));
        message.setIsReported(true);
        message.setReportedAt(LocalDateTime.now());
        message.setStatus(MessageStatus.PENDING);
        log.info("Reported message: {}", message);
        chatRepository.save(message);
    }


    public List<MessageDto> getReportedMessages() {
        List<Message> messages = chatRepository.findMessagesByIsReported(true);
        List<MessageDto> dtos = new ArrayList<>();
        messages.forEach(message -> {
            dtos.add(toDto(message));
        });

        return dtos;
    }

    public void setBanned(String messageId) {
        Message message = chatRepository.findById(messageId).orElseThrow(() -> new NotFoundException("MessageId: " + messageId));
        message.setStatus(MessageStatus.BANNED);
        chatRepository.save(message);
    }

    public void setResolved(String messageId) {
        Message message = chatRepository.findById(messageId).orElseThrow(() -> new NotFoundException("MessageId: " + messageId));
        message.setStatus(MessageStatus.RESOLVED);
        chatRepository.save(message);
    }

    public void sendProductDeletedNotice(UUID receiverId, String productTitle, String reason) {
        UserDto receiverUser = userClient.getUserById(receiverId, null).getData();
        if (receiverUser == null || receiverUser.getEmail() == null) {
            throw new NotFoundException("Receiver user not found");
        }

        String safeReason = (reason == null || reason.isBlank()) ? "Qayda pozuntusu" : reason.trim();
        String safeProductTitle = (productTitle == null || productTitle.isBlank()) ? "Bu" : productTitle.trim();
        String noticeContent = String.format("""
                ⚠️ Vacib Xəbərdarlıq !
                
                Gələcəkdə qayda pozuntuları təkrarlanarsa, hesabınız müvəqqəti və ya daimi olaraq məhdudlaşdırıla bilər.
                
                Məhsul Adı: %s
                
                Qayda pozuntusu : %s
                
                Bu emaili Swaply komandası göndərdi.
                Bu, avtomatik göndərilən bildiriş mesajıdır.
                
                """, safeProductTitle, safeReason);

        Message notice = Message.builder()
                .senderId(SYSTEM_ADMIN_ID)
                .receiverId(receiverId)
                .content(noticeContent)
                .sentAt(LocalDateTime.now())
                .messageType(MessageType.SYSTEM)
                .isRead(false)
                .isReported(false)
                .status(MessageStatus.NORMAL)
                .build();

        chatRepository.save(notice);
        messagingTemplate.convertAndSendToUser(receiverUser.getEmail(), "/queue/messages", toDto(notice));
    }

    public void sendOrderPlacedNotice(UUID sellerId, String buyerName, String productTitle) {
        UserDto sellerUser = userClient.getUserById(sellerId, null).getData();
        if (sellerUser == null || sellerUser.getEmail() == null) {
            log.warn("Seller user not found for notification: {}", sellerId);
            return;
        }

        String noticeContent = String.format("🎉 Yeni sifariş! %s tərəfindən '%s' məhsulu üçün sifariş yaradıldı.", buyerName, productTitle);

        Message notice = Message.builder()
                .senderId(SYSTEM_ADMIN_ID)
                .receiverId(sellerId)
                .content(noticeContent)
                .sentAt(LocalDateTime.now())
                .messageType(MessageType.SYSTEM)
                .isRead(false)
                .isReported(false)
                .status(MessageStatus.NORMAL)
                .build();

        chatRepository.save(notice);
        messagingTemplate.convertAndSendToUser(sellerUser.getEmail(), "/queue/messages", toDto(notice));
        
        // Also send a specific notification event for dashboard refresh
        messagingTemplate.convertAndSendToUser(
            sellerUser.getEmail(),
            "/queue/notifications",
            Map.of(
                "type", "NEW_ORDER",
                "message", noticeContent,
                "timestamp", LocalDateTime.now().toString()
            )
        );
    }


    public void markMessageAsRead(String messageId, UUID currentUserId, String token) {
        if (messageId == null || currentUserId == null) {
            return;
        }

        Optional<Message> messageOpt = chatRepository.findByIdAndReceiverId(messageId, currentUserId);
        if (messageOpt.isEmpty()) {
            return;
        }

        Message message = messageOpt.get();
        if (Boolean.TRUE.equals(message.getIsRead())) {
            return;
        }

        message.setIsRead(true);
        chatRepository.save(message);

        try {
            UserDto sender = userClient.getUserById(message.getSenderId(), token).getData();
            if (sender != null && sender.getEmail() != null) {
                messagingTemplate.convertAndSendToUser(
                        sender.getEmail(),
                        "/queue/read-receipts",
                        Map.of(
                                "messageId", messageId,
                                "readerId", currentUserId.toString(),
                                "read", true
                        )
                );
            }
        } catch (Exception e) {
            log.warn("Failed to publish read receipt for message {}: {}", messageId, e.getMessage());
        }
    }

    public void markUserHeartbeat(UUID userId) {
        if (onlineUserTracker.markHeartbeat(userId)) {
            notifyPresenceToContacts(userId, true, null);
        }
    }

    public void clearUserHeartbeat(UUID userId) {
        if (onlineUserTracker.clearHeartbeat(userId)) {
            notifyPresenceToContacts(userId, false, null);
        }
    }

    public void notifyPresenceToContacts(UUID changedUserId, boolean isOnline, String token) {
        if (changedUserId == null) {
            return;
        }

        List<Message> relatedMessages = chatRepository.findConversationsByUserId(changedUserId);
        Set<UUID> contactIds = new HashSet<>();

        for (Message message : relatedMessages) {
            UUID other = message.getSenderId().equals(changedUserId) ? message.getReceiverId() : message.getSenderId();
            if (other != null && !other.equals(changedUserId)) {
                contactIds.add(other);
            }
        }

        for (UUID contactId : contactIds) {
            try {
                UserDto contact = userClient.getUserById(contactId, token).getData();
                if (contact != null && contact.getEmail() != null) {
                    messagingTemplate.convertAndSendToUser(
                            contact.getEmail(),
                            "/queue/presence",
                            Map.of(
                                    "userId", changedUserId.toString(),
                                    "isOnline", isOnline
                            )
                    );
                }
            } catch (Exception e) {
                log.warn("Failed to notify presence to contact {}: {}", contactId, e.getMessage());
            }
        }
    }

    private MessageDto toDto(Message message) {
        return MessageDto.builder()
                .id(message.getId())
                .senderId(message.getSenderId())
                .receiverId(message.getReceiverId())
                .content(message.getContent())
                .productId(message.getProductId())
                .sentAt(message.getSentAt())
                .messageType(message.getMessageType())
                .isRead(message.getIsRead())
                .isReported(message.getIsReported())
                .reportedAt(message.getReportedAt())
                .status(message.getStatus())
                .build();
    }
}
