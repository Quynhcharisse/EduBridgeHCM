package com.sp26se041.edubridgehcm.services.implementors;

import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.models.Account;
import com.sp26se041.edubridgehcm.models.ChatMessage;
import com.sp26se041.edubridgehcm.models.Conversation;
import com.sp26se041.edubridgehcm.models.Counsellor;
import com.sp26se041.edubridgehcm.models.Parent;
import com.sp26se041.edubridgehcm.models.PersonalityType;
import com.sp26se041.edubridgehcm.models.StudentProfile;
import com.sp26se041.edubridgehcm.repositories.AccountRepo;
import com.sp26se041.edubridgehcm.repositories.ChatMessageRepo;
import com.sp26se041.edubridgehcm.repositories.ConversationRepo;
import com.sp26se041.edubridgehcm.repositories.CounsellorRepo;
import com.sp26se041.edubridgehcm.repositories.PersonalityTypeRepo;
import com.sp26se041.edubridgehcm.repositories.StudentInfoRepo;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.CounsellorService;
import com.sp26se041.edubridgehcm.utils.AccountRestrictionUtil;
import com.sp26se041.edubridgehcm.utils.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CounsellorServiceImpl implements CounsellorService {

    private final ConversationRepo conversationRepo;

    private final ChatMessageRepo chatMessageRepo;

    private final AccountRepo accountRepo;
    private final StudentInfoRepo studentInfoRepo;
    private final PersonalityTypeRepo personalityTypeRepo;

    @Override
    public ResponseEntity<ResponseObject> getConversations(String status, Long cursorId) {
        try {

            String email = SecurityContextHolder
                    .getContext()
                    .getAuthentication()
                    .getName();

            List<Conversation> conversations;

            Optional<Account> counsellorAcc = accountRepo.findByEmail(email);

            if(status.equalsIgnoreCase("active")) {
                if (cursorId == null) {
                    conversations = conversationRepo
                            .findTop20ByCounsellorEmailAndStatusAndStudentProfileIsNotNullOrderByIdDesc(email, Status.CONVERSATION_ACTIVE);
                } else {
                    conversations = conversationRepo
                            .findTop20ByCounsellorEmailAndStatusAndIdLessThanAndStudentProfileIsNotNullOrderByIdDesc(email, Status.CONVERSATION_PENDING, cursorId);
                }
                List<Map<String, Object>> items = buildConversationList(conversations, email);

                boolean hasMore = conversations.size() == 20;
                Long nextCursorId = hasMore
                        ? conversations.get(conversations.size() - 1).getId()
                        : null;

                Map<String, Object> result = new HashMap<>();
                result.put("items", items);
                result.put("hasMore", hasMore);
                result.put("nextCursorId", nextCursorId);

                return ResponseBuilder.build(
                        HttpStatus.OK,
                        "Get conversations with status " + status + " successfully",
                        result
                );
            } else if (status.equalsIgnoreCase("pending")) {
                if (cursorId == null) {
                    conversations = conversationRepo
                            .findTop20ByCampusIdAndStatusAndStudentProfileIsNotNullOrderByIdDesc(counsellorAcc.get().getCampus().getId(), Status.CONVERSATION_PENDING);
                } else {
                    conversations = conversationRepo
                            .findTop20ByCampusIdAndStatusAndIdLessThanAndStudentProfileIsNotNullOrderByIdDesc(counsellorAcc.get().getCampus().getId(), Status.CONVERSATION_PENDING, cursorId);
                }
                List<Map<String, Object>> items = buildConversationList(conversations, email);

                boolean hasMore = conversations.size() == 20;
                Long nextCursorId = hasMore
                        ? conversations.get(conversations.size() - 1).getId()
                        : null;

                Map<String, Object> result = new HashMap<>();
                result.put("items", items);
                result.put("hasMore", hasMore);
                result.put("nextCursorId", nextCursorId);

                return ResponseBuilder.build(
                        HttpStatus.OK,
                        "Get conversations with status " + status + " successfully",
                        result
                );
            }
                return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Invalid status", null);

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

                    String avatarUrl = accountRepo.findByEmail(otherUser)
                            .map(Account::getParent)
                            .map(Parent::getAvatar)
                            .orElse("N/A");

                    map.put("avatarUrl", avatarUrl);
                    map.put("studentProfileId", conversation.getStudentProfile().getId());
                    map.put("studentName", conversation.getStudentProfile().getStudentName());

                    return map;

                })
                .toList();
    }
    @Override
    public ResponseEntity<ResponseObject> getChatHistory(String parentEmail, String counsellorEmail, int studentProfileId, Long cursorId) {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Your account is restricted", null);
        }

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
                conversationRepo.findByParentEmailAndCampusIdAndStudentProfile(parentEmail, accCounsellor.get().getCampus().getId(), studentProfile.get());

        Conversation conversation;
        List<ChatMessage> messages;

        boolean hasMore = false;
        Long nextCursorId = null;

        if (existingConversation.isPresent()) {

            conversation = existingConversation.get();

            if(conversation.getCounsellorEmail().equals("N/A")) {
                conversation.setStatus(Status.CONVERSATION_ACTIVE);
                conversation.setCounsellorEmail(counsellorEmail);
                conversationRepo.save(conversation);
            }

            if (cursorId == null) {
                messages = chatMessageRepo.findTop20ByConversationIdOrderByTimestampDesc(existingConversation.get().getId());
            } else {
                messages = chatMessageRepo.findTop20ByConversationIdAndIdLessThanOrderByIdDesc(existingConversation.get().getId(), cursorId);
                hasMore = messages.size() == 20;
                nextCursorId = messages.isEmpty() ? null : messages.get(messages.size() - 1).getId();
            }
        } else {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Conversation not found or be deleted or not active", null);
        }
        return ResponseBuilder.build(HttpStatus.OK, "Success", buildHistoryMessages(conversation, studentProfile.get(), messages, hasMore, nextCursorId));
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
