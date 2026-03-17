package com.sp26se041.edubridgehcm.repositories;

import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.models.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepo extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByConversationIdAndReceiverNameAndStatus(Long conversationId, String receiverName, Status status);

    List<ChatMessage> findTop20ByConversationIdOrderByTimestampDesc(Long conversationId);

    List<ChatMessage> findTop20ByConversationIdAndIdLessThanOrderByIdDesc(Long conversationId, Long cursorId);

    // Lấy last message của 1 conversation
    ChatMessage findTopByConversationIdOrderByTimestampDesc(Long conversationId);

    // Đếm unread message
    Long countByConversationIdAndReceiverNameAndStatusNot(
            Long conversationId,
            String receiverName,
            Status status
    );
}
