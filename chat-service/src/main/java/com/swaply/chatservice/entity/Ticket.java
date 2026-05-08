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
@Document(collection = "tickets")
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {
    @Id
    private String id;

    private UUID userId;

    private String userName;

    private String userEmail;

    private String title;

    private String userReport;

    private String adminResponse;

    private LocalDateTime responseTime;

    @Builder.Default
    private LocalDateTime reportTime =  LocalDateTime.now();

    @Builder.Default
    private MessageStatus status = MessageStatus.PENDING;




}
