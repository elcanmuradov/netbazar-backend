package com.swaply.chatservice.controller;

import com.swaply.chatservice.dto.*;
import com.swaply.chatservice.service.ChatService;
import com.swaply.chatservice.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;
    private final TicketService ticketService;

    @MessageMapping("/chat.send")
    public void chatEndpoints(@Payload MessageDto message, StompHeaderAccessor accessor) {
        chatService.sendMessageToUser(message, accessor);
    }

    @MessageMapping("/chat.read")
    public void markAsRead(@Payload MessageDto message, StompHeaderAccessor headerAccessor) {
        UUID currentUserId = null;
        String token = null;
        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
        if (sessionAttributes != null) {
            currentUserId = (UUID) sessionAttributes.get("userId");
            token = (String) sessionAttributes.get("token");
        }
        chatService.markMessageAsRead(message.getId(), currentUserId, token);
    }


    @GetMapping("/history/{otherUserId}")
    public ResponseEntity<List<MessageDto>> getHistory(@PathVariable UUID otherUserId, @RequestHeader("X-User-Id") UUID currentUserId) {
        return ResponseEntity.ok(chatService.getMessagesWithUser(currentUserId, otherUserId));
    }

    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationDto>> getConversations(@RequestHeader("X-User-Id") UUID currentUserId, @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(chatService.getConversations(currentUserId, token));
    }

    @PostMapping("/presence/ping")
    public void pingPresence(@RequestHeader("X-User-Id") UUID currentUserId) {
        chatService.markUserHeartbeat(currentUserId);
    }

    @PostMapping("/presence/offline")
    public void clearPresence(@RequestHeader("X-User-Id") UUID currentUserId) {
        chatService.clearUserHeartbeat(currentUserId);
    }



    @PutMapping("/{messageId}/report-message")
    public void reportMessage(@PathVariable String messageId) {
        chatService.reportMessage(messageId);
    }

    @GetMapping("/reported-messages")
    public ResponseEntity<ApiResponse<List<MessageDto>>> getReportedMessages() {
        return ResponseEntity.ok(ApiResponse.success(chatService.getReportedMessages()));
    }

    @PutMapping("/reported/ban")
    public void setBanned(@RequestParam String messageId) {
        chatService.setBanned(messageId);
    }

    @PutMapping("/reported/resolved")
    public void setResolved(@RequestParam String messageId) {
        chatService.setResolved(messageId);
    }

    @PostMapping("/system/product-deleted")
    public void sendProductDeletedNotice(@RequestBody ProductDeletionNoticeRequest request) {
        chatService.sendProductDeletedNotice(request.getReceiverId(), request.getProductTitle(), request.getReason());
    }

    @PostMapping("/system/order-placed")
    public void sendOrderPlacedNotice(@RequestBody OrderNoticeRequest request) {
        chatService.sendOrderPlacedNotice(request.getSellerId(), request.getBuyerName(), request.getProductTitle());
    }

    @PostMapping("/support/create-ticket")
    public void sendSupportMessageToAdmin(@RequestBody TicketRequestDto request) {
        ticketService.sendSupportMessageToAdmin(request);
    }

    @PostMapping({"/support/resolve-ticket", "/support/admin/resolve-ticket", "/chat/support/resolve-ticket"})
    public void sendSupportReportMessageToUser(@RequestBody TicketAdminRequestDto request) {
        ticketService.sendSupportReportMessageToUser(request);
    }

    @GetMapping("/support/my-tickets")
    public ResponseEntity<ApiResponse<List<TicketResponseDto>>> getUserTickets(@RequestHeader("X-User-Id") UUID currentUserId,
                                                                                @RequestHeader(value = "Authorization", required = false) String token) {
        return ResponseEntity.ok(ApiResponse.success(ticketService.getUserTickets(currentUserId, token)));
    }

    @GetMapping("/support/admin/get-resolved-tickets")
    public ResponseEntity<ApiResponse<List<TicketResponseDto>>> getResolvedTickets(){
        return ResponseEntity.ok(ApiResponse.success(ticketService.getResolvedTickets()));
    }

    @GetMapping("/support/admin/get-pending-tickets")
    public ResponseEntity<ApiResponse<List<TicketResponseDto>>> getPendingTickets(){
        return ResponseEntity.ok(ApiResponse.success(ticketService.getPendingTickets()));
    }


}
