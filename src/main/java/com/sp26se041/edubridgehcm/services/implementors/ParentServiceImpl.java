package com.sp26se041.edubridgehcm.services.implementors;

import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.models.Account;
import com.sp26se041.edubridgehcm.models.ChatMessage;
import com.sp26se041.edubridgehcm.models.Conversation;
import com.sp26se041.edubridgehcm.repositories.AccountRepo;
import com.sp26se041.edubridgehcm.repositories.ChatMessageRepo;
import com.sp26se041.edubridgehcm.repositories.ConversationRepo;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.ParentService;
import com.sp26se041.edubridgehcm.utils.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
                    .createdDate(LocalDateTime.now())
                    .updatedDate(LocalDateTime.now())
                    .status(Status.CONVERSATION_ACTIVE)
                    .build();

            conversation = conversationRepo.save(conversation);

        }
        return ResponseBuilder.build(HttpStatus.OK, "Success", buildHistoryMessages(conversation, messages, hasMore, nextCursorId));

    }

    @Override
    public ResponseEntity<ResponseObject> markConversationAsRead(Long conversationId, String receiverEmail) {
        List<ChatMessage> unreadMessages = chatMessageRepo.findByConversationIdAndReceiverNameAndStatus(conversationId, receiverEmail, Status.MESSAGE_SENT);
        unreadMessages.forEach(msg -> msg.setStatus(Status.MESSAGE_READ));
        chatMessageRepo.saveAll(unreadMessages);
        return ResponseBuilder.build(HttpStatus.OK, "Marked as read", unreadMessages);
    }

    @Override
    public  ResponseEntity<ResponseObject> getConversations(Long cursorId) {
        try {
            String email = SecurityContextHolder
                    .getContext()
                    .getAuthentication()
                    .getName();

            Authentication authentication = SecurityContextHolder
                    .getContext()
                    .getAuthentication();

            List<String> roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();

            boolean isParent = roles.contains("ROLE_PARENT");
            boolean isCounsellor = roles.contains("ROLE_COUNSELLOR");

            List<Conversation> conversations = new ArrayList<>();

            if (cursorId == null) {
                if (isParent) {
                    conversations = conversationRepo
                            .findTop20ByParentEmailOrderByUpdatedDateDesc(email);
                } else if (isCounsellor) {
                    conversations = conversationRepo
                            .findTop20ByCounsellorEmailOrderByUpdatedDateDesc(email);
                }
            } else {
                if (isParent) {
                    conversations = conversationRepo
                            .findTop20ByParentEmailAndIdLessThanOrderByUpdatedDateDesc(email, cursorId);
                } else if (isCounsellor) {
                    conversations = conversationRepo
                            .findTop20ByCounsellorEmailAndIdLessThanOrderByUpdatedDateDesc(email, cursorId);
                }
            }
            List<Map<String, Object>> items = buildConversationList(conversations, email);

            boolean hasMore = conversations.size() == 20;
            Long nextCursorId = hasMore && !conversations.isEmpty()
                    ? conversations.get(conversations.size() - 1).getId()
                    : null;

            Map<String, Object> result = new HashMap<>();
            result.put("items", items);
            result.put("hasMore", hasMore);
            result.put("nextCursorId", nextCursorId);

            return ResponseBuilder.build(
                    HttpStatus.OK,
                    "Get conversations successfully",
                    result
            );

        } catch (Exception e) {
            return ResponseBuilder.build(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed: " + e.getMessage(),
                    null
            );
        }
    }

    @Override
    public String createChatMessage(ChatMessage chatMessage) {
        Optional<Conversation> conversation = conversationRepo.findById(chatMessage.getConversationId());
        if (conversation.isEmpty()) {
            return "Please refresh page and try again later";
        }
        chatMessage.setStatus(Status.MESSAGE_SENT);
        conversation.get().setUpdatedDate(LocalDateTime.now());
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

    private List<Map<String, Object>> buildConversationList(
            List<Conversation> conversations,
            String email
    ) {

        // 👉 tránh null pointer
        if (conversations == null || conversations.isEmpty()) {
            return List.of(); // hoặc return new ArrayList<>();
        }

        return conversations.stream()
                .map(conversation -> {

                    ChatMessage lastMessage = chatMessageRepo
                            .findTopByConversationIdOrderByTimestampDesc(conversation.getId());

                    Long unreadCount = chatMessageRepo
                            .countByConversationIdAndReceiverNameAndStatusNot(
                                    conversation.getId(),
                                    email,
                                    Status.MESSAGE_READ
                            );

                    Map<String, Object> map = new HashMap<>();
                    map.put("conversationId", conversation.getId());
                    map.put("lastMessage", lastMessage != null ? lastMessage.getMessage() : null);
                    map.put("updatedAt", conversation.getUpdatedDate());
                    map.put("unreadCount", unreadCount != null ? unreadCount : 0L);
                    String otherUser = conversation.getParentEmail().equals(email)
                            ? conversation.getCounsellorEmail()
                            : conversation.getParentEmail();

                    map.put("otherUser", otherUser);
                    return map;
                })
                .toList();
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
