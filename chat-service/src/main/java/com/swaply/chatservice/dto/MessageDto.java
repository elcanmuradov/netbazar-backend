package com.swaply.chatservice.dto;

import com.swaply.chatservice.utils.enums.MessageStatus;
import com.swaply.chatservice.utils.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDto {
    private String id;
    private UUID senderId;
    private UUID receiverId;
    private String content;
    private UUID productId;
    private LocalDateTime sentAt;
    private MessageType messageType;

    @Builder.Default
    private boolean isRead = false;

    @Builder.Default
    private LocalDateTime reportedAt = null;

    @Builder.Default
    private boolean isReported = false;

    @Builder.Default
    private MessageStatus status = MessageStatus.NORMAL;
}
