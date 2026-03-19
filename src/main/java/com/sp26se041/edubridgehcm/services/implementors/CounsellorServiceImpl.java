package com.sp26se041.edubridgehcm.services.implementors;

import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.models.ChatMessage;
import com.sp26se041.edubridgehcm.models.Conversation;
import com.sp26se041.edubridgehcm.repositories.ChatMessageRepo;
import com.sp26se041.edubridgehcm.repositories.ConversationRepo;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.CounsellorService;
import com.sp26se041.edubridgehcm.utils.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CounsellorServiceImpl implements CounsellorService {

    private final ConversationRepo conversationRepo;
    private final ChatMessageRepo chatMessageRepo;

    @Override
    public ResponseEntity<ResponseObject> getConversations(Long cursorId) {
        try {
            String email = SecurityContextHolder
                    .getContext()
                    .getAuthentication()
                    .getName();
            List<Conversation> conversations;
            if (cursorId == null) {
                conversations = conversationRepo
                        .findTop20ByCounsellorEmailOrderByUpdatedDateDesc(email);

            } else {
                conversations = conversationRepo
                        .findTop20ByCounsellorEmailAndIdLessThanOrderByUpdatedDateDesc(email, cursorId);
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
}
