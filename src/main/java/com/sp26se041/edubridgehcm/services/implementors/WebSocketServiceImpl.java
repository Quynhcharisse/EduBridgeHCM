package com.sp26se041.edubridgehcm.services.implementors;

import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.models.Account;
import com.sp26se041.edubridgehcm.models.ChatMessage;
import com.sp26se041.edubridgehcm.models.Conversation;
import com.sp26se041.edubridgehcm.models.PersonalityType;
import com.sp26se041.edubridgehcm.models.StudentProfile;
import com.sp26se041.edubridgehcm.repositories.AccountRepo;
import com.sp26se041.edubridgehcm.repositories.ChatMessageRepo;
import com.sp26se041.edubridgehcm.repositories.ConversationRepo;
import com.sp26se041.edubridgehcm.repositories.PersonalityTypeRepo;
import com.sp26se041.edubridgehcm.repositories.StudentInfoRepo;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.WebSocketService;
import com.sp26se041.edubridgehcm.utils.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
public class WebSocketServiceImpl implements WebSocketService {

    private final ConversationRepo conversationRepo;
    private final ChatMessageRepo chatMessageRepo;
    private final AccountRepo accountRepo;
    private final StudentInfoRepo studentInfoRepo;
    private final PersonalityTypeRepo personalityTypeRepo;

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

    @Override
    public ResponseEntity<ResponseObject> getChatHistory(String parentEmail, String counsellorEmail, int studentProfileId, Long cursorId) {
        Optional<Account> accParent = accountRepo.findByEmail(parentEmail);

        if (accParent.isEmpty()) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Parent not found or be deleted", null);
        }

        Optional<Account> accCounsellor = accountRepo.findByEmail(counsellorEmail);

        if (accCounsellor.isEmpty()) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Counsellor not found or be deleted", null);
        }

        Optional<StudentProfile> studentProfile = studentInfoRepo.findById(studentProfileId);

        if(studentProfile.isEmpty()) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Student profile not found or be deleted", null);
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
                    .studentProfile(studentProfile.get())
                    .status(Status.CONVERSATION_ACTIVE)
                    .build();

            conversation = conversationRepo.save(conversation);

        }
        return ResponseBuilder.build(HttpStatus.OK, "Success", buildHistoryMessages(conversation, studentProfile.get(), messages, hasMore, nextCursorId));
    }

    @Override
    public ResponseEntity<ResponseObject> markConversationAsRead(Long conversationId, String receiverEmail) {
        List<ChatMessage> unreadMessages = chatMessageRepo.findByConversationIdAndReceiverNameAndStatus(conversationId, receiverEmail, Status.MESSAGE_SENT);
        unreadMessages.forEach(msg -> msg.setStatus(Status.MESSAGE_READ));
        chatMessageRepo.saveAll(unreadMessages);
        return ResponseBuilder.build(HttpStatus.OK, "Marked as read", unreadMessages);
    }


    private Map<String, Object> buildHistoryMessages(Conversation conversation, StudentProfile childProfile, List<ChatMessage> messages, boolean hasMore, Long nextCursorId) {

        Map<String, Object> response = new HashMap<>();

        response.put("conversationId", conversation.getId());

        response.put("childName", childProfile.getStudentName());
        response.put("gender", childProfile.getGender());

        Optional<PersonalityType> personalityType =
                personalityTypeRepo.findByCode(childProfile.getPersonalityTypeName());

        response.put("personalityCode",
                personalityType.map(PersonalityType::getCode).orElse("N/A"));

        response.put("traits",
                personalityType.map(PersonalityType::getTraits).orElse(List.of()));

        response.put("favouriteJob", childProfile.getFavouriteJob());
        response.put("academicProfileMetadata", childProfile.getAcademicProfileMetadata());
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
