package com.swaply.chatservice.repository;


import com.swaply.chatservice.entity.Message;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatRepository extends MongoRepository<Message, String> {
    List<Message> findBySenderIdAndReceiverIdOrReceiverIdAndSenderIdOrderBySentAtAsc(UUID sender1, UUID receiver1, UUID sender2, UUID receiver2);

    @Query("{ $or: [ { senderId: ?0 }, { receiverId: ?0 } ] }")
    List<Message> findConversationsByUserId(UUID userId);

    Optional<Message> findByIdAndReceiverId(String id, UUID receiverId);

    int countBySenderIdAndReceiverIdAndIsReadFalse(UUID senderId, UUID receiverId);

    Optional<Message> findById(String id);

    List<Message> findMessagesByIsReported(Boolean isReported);
}
