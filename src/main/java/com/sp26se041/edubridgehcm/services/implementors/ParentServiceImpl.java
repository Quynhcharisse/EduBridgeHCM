package com.sp26se041.edubridgehcm.services.implementors;

import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.models.*;
import com.sp26se041.edubridgehcm.repositories.*;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.ParentService;
import com.sp26se041.edubridgehcm.utils.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ParentServiceImpl implements ParentService {

    private final ChatMessageRepo chatMessageRepo;
    private final ConversationRepo conversationRepo;
    private final AccountRepo accountRepo;

    @Override
    public ResponseEntity<ResponseObject> getHistoryMessages(String parentEmail, String counsellorEmail, Long cursorId) {

        Optional<Account> accParent = accountRepo.findByEmail(parentEmail);

        if (accParent.isEmpty()) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Parent not found or be deleted", null);
        }

        Optional<Account> accCounsellor = accountRepo.findByEmail(counsellorEmail);

        if (accCounsellor.isEmpty()) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Counsellor not found or be deleted", null);
        }
        Optional<Conversation> existingConversation =
                conversationRepo.findByParentEmailAndCounsellorEmail(parentEmail, counsellorEmail);

        Conversation conversation;
        List<ChatMessage> messages = new ArrayList<>();

        boolean hasMore = false;
        Long nextCursorId = null;

        if (existingConversation.isPresent()) {
            conversation = existingConversation.get();
            if (cursorId == null) {
                messages = chatMessageRepo.findTop20ByConversationIdOrderByTimestampDesc(existingConversation.get().getId());
            } else {
                messages = chatMessageRepo.findTop20ByConversationIdAndIdLessThanOrderByIdDesc(existingConversation.get().getId(), cursorId);
                hasMore = messages.size() == 20;
                nextCursorId = messages.isEmpty() ? null : messages.get(messages.size() - 1).getId();
            }
        } else {
            conversation = Conversation.builder()
                    .parentEmail(parentEmail)
                    .counsellorEmail(counsellorEmail)
                    .status(Status.CONVERSATION_ACTIVE)
                    .build();

            conversation = conversationRepo.save(conversation);
        }


        return ResponseBuilder.build(HttpStatus.OK, "Success", buildHistoryMessages(conversation ,messages, hasMore, nextCursorId));

    }

    @Override
    public ResponseEntity<ResponseObject> markConversationAsRead(Long conversationId, String receiverEmail) {
        List<ChatMessage> unreadMessages = chatMessageRepo.findByConversationIdAndReceiverNameAndStatus(conversationId, receiverEmail, Status.MESSAGE_SENT);
        unreadMessages.forEach(msg -> msg.setStatus(Status.MESSAGE_READ));
        chatMessageRepo.saveAll(unreadMessages);
        return ResponseBuilder.build(HttpStatus.OK, "Marked as read", unreadMessages);
    }

    @Override
    public String createChatMessage(ChatMessage chatMessage) {
        Optional<Conversation> conversation = conversationRepo.findById(chatMessage.getConversationId());
        chatMessage.setStatus(Status.MESSAGE_SENT);
        if(conversation.isEmpty()) {
           return "Please refresh page and try again later";
        }
        chatMessageRepo.save(chatMessage);
        return "";
    }

    private Map<String, Object> buildHistoryMessages(Conversation conversation, List<ChatMessage> messages, boolean hasMore, Long nextCursorId) {

        Map<String, Object> response = new HashMap<>();

        response.put("conversationId", conversation.getId());
        response.put("messages", buildMessages(messages));
        response.put("hasMore", hasMore);
        response.put("nextCursorId", nextCursorId);

        return response;
    }
    private List<Map<String, Object>> buildMessages(List<ChatMessage> messages) {

        if (messages == null || messages.isEmpty()) {
            return Collections.emptyList();
        }

        return messages.stream()
                .sorted(Comparator.comparing(ChatMessage::getTimestamp))
                .map(this::buildMessage)
                .toList();
    }

    private Map<String, Object> buildMessage(ChatMessage message) {
        Map<String, Object> msg = new HashMap<>();
        msg.put("id", message.getId());
        msg.put("senderName", message.getSenderName());
        msg.put("receiverName", message.getReceiverName());
        msg.put("message", message.getMessage());
        msg.put("timestamp", message.getTimestamp());
        msg.put("status", message.getStatus());
        return msg;
    }

}
