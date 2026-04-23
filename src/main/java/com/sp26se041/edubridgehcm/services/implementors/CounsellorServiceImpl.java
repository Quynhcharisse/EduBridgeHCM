package com.sp26se041.edubridgehcm.services.implementors;

import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.enums.SubjectType;
import com.sp26se041.edubridgehcm.models.Account;
import com.sp26se041.edubridgehcm.models.ChatMessage;
import com.sp26se041.edubridgehcm.models.Conversation;
import com.sp26se041.edubridgehcm.models.Counsellor;
import com.sp26se041.edubridgehcm.models.Parent;
import com.sp26se041.edubridgehcm.models.PersonalityType;
import com.sp26se041.edubridgehcm.models.StudentProfile;
import com.sp26se041.edubridgehcm.models.Subject;
import com.sp26se041.edubridgehcm.repositories.AccountRepo;
import com.sp26se041.edubridgehcm.repositories.ChatMessageRepo;
import com.sp26se041.edubridgehcm.repositories.ConversationRepo;
import com.sp26se041.edubridgehcm.repositories.CounsellorRepo;
import com.sp26se041.edubridgehcm.repositories.PersonalityTypeRepo;
import com.sp26se041.edubridgehcm.repositories.StudentInfoRepo;
import com.sp26se041.edubridgehcm.repositories.SubjectRepo;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.CounsellorService;
import com.sp26se041.edubridgehcm.utils.AccountRestrictionUtil;
import com.sp26se041.edubridgehcm.utils.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CounsellorServiceImpl implements CounsellorService {

    private final ConversationRepo conversationRepo;

    private final ChatMessageRepo chatMessageRepo;

    private final AccountRepo accountRepo;

    private final StudentInfoRepo studentInfoRepo;

    private final PersonalityTypeRepo personalityTypeRepo;

    private final CounsellorRepo counsellorRepo;

    private final SubjectRepo subjectRepo;

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
                        "",
                        result
                );
            } else if (status.equalsIgnoreCase("pending")) {

                Counsellor counsellor = counsellorRepo.findByAccountId(counsellorAcc.get().getId());

                if (cursorId == null) {
                    conversations = conversationRepo
                            .findTop20ByCampusIdAndStatusAndStudentProfileIsNotNullOrderByIdDesc(counsellor.getCampus().getId(), Status.CONVERSATION_PENDING);
                } else {
                    conversations = conversationRepo
                            .findTop20ByCampusIdAndStatusAndIdLessThanAndStudentProfileIsNotNullOrderByIdDesc(counsellor
                                    .getCampus().getId(), Status.CONVERSATION_PENDING, cursorId);
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
                        "",
                        result
                );
            }
                return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Trạng thái gửi đi không hợp lệ", null);

        } catch (Exception e) {
            return ResponseBuilder.build(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Thất bại: " + e.getMessage(),
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

                    Long unreadCount;

                    if (conversation.getStatus().getValue().equals(Status.CONVERSATION_PENDING.getValue())) {

                        unreadCount = chatMessageRepo.countByConversationIdAndStatusNot(conversation.getId(), Status.MESSAGE_READ);

                    } else {

                         unreadCount = chatMessageRepo
                                .countByConversationIdAndReceiverNameAndStatusNot(
                                        conversation.getId(),
                                        email,
                                        Status.MESSAGE_READ
                                );
                    }

                    String otherUser = conversation.getParentEmail().equals(email)
                            ? conversation.getCounsellorEmail()
                            : conversation.getParentEmail();
                    String avatarUrl = accountRepo.findByEmail(otherUser)
                            .map(Account::getParent)
                            .map(Parent::getAvatar)
                            .orElse("N/A");

                    Map<String, Object> map = new HashMap<>();
                    map.put("conversationId", conversation.getId());
                    map.put("lastMessage", lastMessage != null ? lastMessage.getMessage() : null);
                    map.put("updatedAt", conversation.getUpdatedDate());
                    map.put("unreadCount", unreadCount != null ? unreadCount : 0L);
                    map.put("otherUser", otherUser);
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
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Tài khoản của bạn đã bị vô hiệu", null);
        }

        Optional<Account> accParent = accountRepo.findByEmail(parentEmail);

        if (accParent.isEmpty()) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Tài khoản của phụ huynh không tìm thấy", null);
        }

        Optional<Account> accCounsellor = accountRepo.findByEmail(counsellorEmail);

        if (accCounsellor.isEmpty()) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Tài khoản tư vấn viên không tìm thấy", null);
        }

        Optional<StudentProfile> studentProfile = studentInfoRepo.findById(studentProfileId);

        if(studentProfile.isEmpty()) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Thông tin trẻ không tồn tại trong hệ thống", null);
        }

        Counsellor counsellor = counsellorRepo.findByAccountId(accCounsellor.get().getId());

        Optional<Conversation> existingConversation =
                conversationRepo.findByParentEmailAndCampusIdAndStudentProfile(parentEmail, counsellor.getCampus().getId(), studentProfile.get());

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
            }
            hasMore = messages.size() == 20;
            nextCursorId = messages.isEmpty() ? null : messages.get(messages.size() - 1).getId();
        } else {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Không tìm thấy cuộc hội thoại", null);
        }
        return ResponseBuilder.build(HttpStatus.OK, "", buildHistoryMessages(conversation, studentProfile.get(), messages, hasMore, nextCursorId));
    }
    private Map<String, Object> buildHistoryMessages(Conversation conversation, StudentProfile childProfile, List<ChatMessage> messages, boolean hasMore, Long nextCursorId) {

        Optional<PersonalityType> personalityType =
                personalityTypeRepo.findByCode(childProfile.getPersonalityTypeName());

        Map<String, Object> response = new HashMap<>();

        response.put("conversationId", conversation.getId());
        response.put("campusId", conversation.getCampusId());
        response.put("studentProfileId", conversation.getStudentProfile().getId());
        response.put("childName", childProfile.getStudentName());
        response.put("messages", buildMessages(messages));
        response.put("hasMore", hasMore);
        response.put("nextCursorId", nextCursorId);

        return response;
    }

    private List<Map<String, Object>> getSubjects() {
        List<Subject> subjects = subjectRepo.findAllByTypeIn(
                List.of(SubjectType.REGULAR_SUBJECT, SubjectType.FOREIGN_LANGUAGE_SUBJECT)
        );
        return subjects.stream()
                .collect(Collectors.groupingBy(Subject::getType))
                .entrySet()
                .stream()
                .map(entry -> {
                    Map<String, Object> groupMap = new HashMap<>();

                    // 🔥 dùng value thay vì name()
                    groupMap.put("type", entry.getKey().getValue());

                    String label = switch (entry.getKey()) {
                        case REGULAR_SUBJECT -> "Môn học chính";
                        case FOREIGN_LANGUAGE_SUBJECT -> "Ngoại ngữ";
                        case THPT_SUBJECT -> "Môn học THPT";
                    };

                    groupMap.put("label", label);

                    List<Map<String, Object>> subjectList = entry.getValue().stream()
                            .map(s -> {
                                Map<String, Object> item = new HashMap<>();
                                item.put("id", s.getId());
                                item.put("name", s.getName());
                                return item;
                            })
                            .toList();

                    groupMap.put("subjects", subjectList);

                    return groupMap;
                })
                .toList();
    }

    private List<Map<String, Object>> buildAcademicProfileMetadata(StudentProfile studentProfile) {

        List<Map<String, Object>> storedAcademicProfiles =
                (List<Map<String, Object>>) studentProfile.getAcademicProfileMetadata();

        if (storedAcademicProfiles == null || storedAcademicProfiles.isEmpty()) {
            return new ArrayList<>();
        }

        List<Map<String, Object>> result = new ArrayList<>();

        for (Map<String, Object> academicItem : storedAcademicProfiles) {
            if (academicItem == null) {
                continue;
            }

            String gradeLevel = academicItem.get("gradeLevel") == null
                    ? null
                    : academicItem.get("gradeLevel").toString();

            List<Map<String, Object>> storedSubjectResults =
                    (List<Map<String, Object>>) academicItem.get("subjectResults");

            List<Map<String, Object>> subjectResults = new ArrayList<>();

            if (storedSubjectResults != null) {
                for (Map<String, Object> subjectItem : storedSubjectResults) {
                    if (subjectItem == null) {
                        continue;
                    }

                    Object subjectNameObj = subjectItem.get("subjectName");
                    if (subjectNameObj == null) {
                        continue;
                    }

                    String subjectName = subjectNameObj.toString().trim();
                    if (subjectName.isEmpty()) {
                        continue;
                    }

                    Optional<Subject> subjectOpt = subjectRepo.findByNameAndType(subjectName, SubjectType.REGULAR_SUBJECT);

                    Map<String, Object> subjectMap = new HashMap<>();
                    subjectMap.put("id", subjectItem.get("id"));
                    subjectMap.put("subjectName", subjectName);
                    subjectMap.put("type", subjectItem.get("type"));
                    subjectMap.put("score", subjectItem.get("score"));
                    subjectMap.put("isAvailable", subjectOpt.isPresent());

                    // Nếu subject còn trong hệ thống thì cập nhật lại thông tin mới nhất
                    if (subjectOpt.isPresent()) {
                        Subject subject = subjectOpt.get();
                        subjectMap.put("id", subject.getId());
                        subjectMap.put("subjectName", subject.getName());
                        subjectMap.put("type", subject.getType().getValue());
                    }

                    subjectResults.add(subjectMap);
                }
            }

            Map<String, Object> academicMap = new HashMap<>();
            academicMap.put("gradeLevel", gradeLevel);
            academicMap.put("subjectResults", subjectResults);

            result.add(academicMap);
        }

        return result;
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
