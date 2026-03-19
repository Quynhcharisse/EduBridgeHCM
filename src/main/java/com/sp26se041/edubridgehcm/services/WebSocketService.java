package com.sp26se041.edubridgehcm.services;

import com.sp26se041.edubridgehcm.models.ChatMessage;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import org.springframework.http.ResponseEntity;

public interface WebSocketService {
    String createChatMessage(ChatMessage chatMessage);
    ResponseEntity<ResponseObject> getChatHistory(String parentEmail, String counsellorEmail, Long cursorId);
    ResponseEntity<ResponseObject> markConversationAsRead(Long conversationId, String receiverEmail);
}
