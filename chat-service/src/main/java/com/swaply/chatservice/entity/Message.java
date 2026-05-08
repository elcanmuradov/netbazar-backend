package com.swaply.chatservice.entity;

import com.mongodb.lang.Nullable;
import com.swaply.chatservice.utils.enums.MessageStatus;
import com.swaply.chatservice.utils.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;


@Data
@Builder
@Document(collection = "messages")
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    @Id
    private String id;
    private UUID senderId;
    private UUID receiverId;
    private String content;
    private UUID productId;
    private LocalDateTime sentAt;

    private MessageType messageType;

    @Builder.Default
    private Boolean isRead = false;

    @Builder.Default
    private Boolean isReported = false;
    @Builder.Default
    @Nullable
    private LocalDateTime reportedAt = null;
    @Builder.Default
    private MessageStatus status = MessageStatus.NORMAL;


}
