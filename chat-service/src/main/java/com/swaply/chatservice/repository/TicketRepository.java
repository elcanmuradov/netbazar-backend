package com.swaply.chatservice.repository;

import com.swaply.chatservice.entity.Ticket;
import com.swaply.chatservice.utils.enums.MessageStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface TicketRepository  extends MongoRepository<Ticket, String> {
    List<Ticket> getTicketsByAdminResponse(String adminResponse);

    List<Ticket> getTicketsByUserEmail(String email);

    List<Ticket> getTicketsByStatus(MessageStatus status);

    Ticket getTicketsById(String id);
}
