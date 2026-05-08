package com.swaply.userservice.dto.admin.message;

import com.swaply.userservice.utils.enums.MessageStatus;
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
public class ReportMessageDto {
    private String id;
    private UUID senderId;
    private String content;
    private LocalDateTime sentAt;

    @Builder.Default
    private MessageStatus status = MessageStatus.PENDING;

    @Builder.Default
    private LocalDateTime reportedAt = LocalDateTime.now();


    @Builder.Default
    private String user = "null";
}
