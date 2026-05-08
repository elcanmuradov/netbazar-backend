package com.swaply.chatservice.service;

import com.swaply.chatservice.service.EmailAsyncService;
import com.swaply.chatservice.client.UserClient;
import com.swaply.chatservice.dto.MessageDto;
import com.swaply.chatservice.dto.TicketAdminRequestDto;
import com.swaply.chatservice.dto.TicketRequestDto;
import com.swaply.chatservice.dto.TicketResponseDto;
import com.swaply.chatservice.dto.user.UserDto;
import com.swaply.chatservice.entity.Message;
import com.swaply.chatservice.entity.Ticket;
import com.swaply.chatservice.exception.NotFoundException;
import com.swaply.chatservice.repository.ChatRepository;
import com.swaply.chatservice.repository.TicketRepository;
import com.swaply.chatservice.utils.enums.MessageStatus;
import com.swaply.chatservice.utils.enums.MessageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
public class TicketService {
    private static final UUID SYSTEM_ADMIN_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    ;
    private final ChatService chatService;
    private final UserClient userClient;
    private final TicketRepository ticketRepository;
    private final ChatRepository chatRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final EmailAsyncService emailAsyncService;

    public void sendSupportMessageToAdmin(TicketRequestDto request) {
        UserDto receiverUser = userClient.getUserById(UUID.fromString(request.getUserId()), null).getData();
        if (receiverUser == null || receiverUser.getEmail() == null) {
            throw new NotFoundException("Sender user not found");
        }

        String message = String.format("""
                Mövzu : %s
                
                Şikayət : %s
                """, request.getTitle(), request.getUserReport());

        Ticket ticket = Ticket.builder()
            .userId(receiverUser.getId())
                .userName(receiverUser.getName())
                .userEmail(receiverUser.getEmail())
                .title(request.getTitle()).userReport(request.getUserReport())
                .reportTime(LocalDateTime.now().plusHours(4)).build();


        ticketRepository.save(ticket);
        messagingTemplate.convertAndSendToUser(receiverUser.getEmail(), "/queue/messages", toDto(ticket));
    }

    public void sendSupportReportMessageToUser(TicketAdminRequestDto request) {
        Ticket ticket = ticketRepository.findById(request.getTicketId()).orElseThrow(() -> new NotFoundException("Ticket not found"));

        String adminResponse = request.getAdminResponse() == null ? "" : request.getAdminResponse().trim();
        if (adminResponse.isBlank()) {
            throw new IllegalArgumentException("Admin response is empty");
        }


        Ticket ticketResponse = Ticket.builder()
                .id(ticket.getId())
            .userId(ticket.getUserId())
                .userName(ticket.getUserName())
                .userEmail(ticket.getUserEmail())
                .title(ticket.getTitle())
                .userReport(ticket.getUserReport())
            .adminResponse(adminResponse)
                .responseTime(LocalDateTime.now().plusHours(4))
                .status(MessageStatus.RESOLVED)
                .reportTime(ticket.getReportTime())
                .build();
        log.info("{}",ticketResponse.toString());
        emailAsyncService.sendTicketResponseAsync(toDto(ticketResponse));
        ticketRepository.save(ticketResponse);

        Message chatMessage = Message.builder()
            .senderId(SYSTEM_ADMIN_ID)
            .receiverId(ticketResponse.getUserId())
            .content(String.format("""
                🎧 Dəstək Komandasından cavab

                Mövzu: %s

                Cavab: %s

                Bu mesaj Netbazar komandası tərəfindən göndərilib.
                """, ticketResponse.getTitle(), ticketResponse.getAdminResponse()))
            .sentAt(LocalDateTime.now().plusHours(4))
            .messageType(MessageType.SYSTEM)
            .isRead(false)
            .isReported(false)
            .status(MessageStatus.NORMAL)
            .build();

        chatRepository.save(chatMessage);
        messagingTemplate.convertAndSendToUser(ticketResponse.getUserEmail(), "/queue/messages", toMessageDto(chatMessage));
    }

    public TicketResponseDto toDto(Ticket ticket) {
        return TicketResponseDto.builder()
                .id(ticket.getId())
                .userName(ticket.getUserName())
                .userEmail(ticket.getUserEmail())
                .title(ticket.getTitle())
                .userReport(ticket.getUserReport())
                .adminResponse(ticket.getAdminResponse())
                .responseTime(ticket.getResponseTime())
                .reportTime(ticket.getReportTime())
                .status(ticket.getStatus())
                .build();
    }

    private MessageDto toMessageDto(Message message) {
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

    public List<TicketResponseDto> getUserTickets(UUID currentUserId, String token) {
        UserDto user = userClient.getUserById(currentUserId, token).getData();
        List<TicketResponseDto> tickets = new ArrayList<>();
        ticketRepository.getTicketsByUserEmail(user.getEmail()).forEach(
                (ticket) -> tickets.add(toDto(ticket))
        );
        return tickets;
    }

    public List<TicketResponseDto> getPendingTickets(){
        List<TicketResponseDto> tickets = new ArrayList<>();
        ticketRepository.getTicketsByStatus(MessageStatus.PENDING).forEach(
                (ticket) -> tickets.add(toDto(ticket))
        );
        return tickets;
    }

    public List<TicketResponseDto> getResolvedTickets(){
        List<TicketResponseDto> tickets = new ArrayList<>();
        ticketRepository.getTicketsByStatus(MessageStatus.RESOLVED).forEach(
                (ticket) -> tickets.add(toDto(ticket))
        );
        return tickets;
    }

}
