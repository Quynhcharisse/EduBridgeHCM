package com.sp26se041.edubridgehcm.services.implementors;

import com.sp26se041.edubridgehcm.enums.Gender;
import com.sp26se041.edubridgehcm.enums.GradeLevel;
import com.sp26se041.edubridgehcm.enums.Role;
import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.models.Account;
import com.sp26se041.edubridgehcm.models.ChatMessage;
import com.sp26se041.edubridgehcm.models.Conversation;
import com.sp26se041.edubridgehcm.models.Counsellor;
import com.sp26se041.edubridgehcm.models.Major;
import com.sp26se041.edubridgehcm.models.PersonalityType;
import com.sp26se041.edubridgehcm.models.StudentProfile;
import com.sp26se041.edubridgehcm.models.Subject;
import com.sp26se041.edubridgehcm.repositories.AccountRepo;
import com.sp26se041.edubridgehcm.repositories.ChatMessageRepo;
import com.sp26se041.edubridgehcm.repositories.ConversationRepo;
import com.sp26se041.edubridgehcm.repositories.CounsellorRepo;
import com.sp26se041.edubridgehcm.repositories.MajorRepo;
import com.sp26se041.edubridgehcm.repositories.PersonalityTypeRepo;
import com.sp26se041.edubridgehcm.repositories.StudentInfoRepo;
import com.sp26se041.edubridgehcm.repositories.SubjectRepo;
import com.sp26se041.edubridgehcm.requests.AddFavouriteSchoolRequest;
import com.sp26se041.edubridgehcm.requests.AddStudentInfoRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.ParentService;
import com.sp26se041.edubridgehcm.utils.AccountRestrictionUtil;
import com.sp26se041.edubridgehcm.utils.AuthRequestUtil;
import com.sp26se041.edubridgehcm.utils.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ParentServiceImpl implements ParentService {

    private final ChatMessageRepo chatMessageRepo;

    private final ConversationRepo conversationRepo;

    private final PersonalityTypeRepo personalityTypeRepo;

    private final MajorRepo majorRepo;

    private final SubjectRepo subjectRepo;

    private final StudentInfoRepo studentInfoRepo;
    private final AccountRepo accountRepo;
    private final CounsellorRepo counsellorRepo;


    @Override
    public  ResponseEntity<ResponseObject> getConversations(Long cursorId) {
        try {
            String email = SecurityContextHolder
                    .getContext()
                    .getAuthentication()
                    .getName();
            List<Conversation> conversations;
            if (cursorId == null) {
                conversations = conversationRepo
                            .findTop20ByParentEmailAndStudentProfileIsNotNullOrderByUpdatedDateDesc(email);
            } else {
                    conversations = conversationRepo
                            .findTop20ByParentEmailAndIdLessThanAndStudentProfileIsNotNullOrderByUpdatedDateDesc(email, cursorId);
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

                    String school = accountRepo.findByEmail(otherUser)
                            .map(Account::getCounsellor)
                            .map(c -> c.getCampus())
                            .map(campus -> campus.getSchool())
                            .map(s -> s.getName())
                            .orElse("N/A");

                    String avatarUrl = accountRepo.findByEmail(otherUser)
                            .map(Account::getCounsellor)
                            .map( c -> c.getAvatar())
                            .orElse("N/A");

                    map.put("avatarUrl", avatarUrl);
                    map.put("school", school);
                    map.put("studentId", conversation.getStudentProfile().getId());
                    map.put("studentName", conversation.getStudentProfile().getStudentName());

                    return map;

                })
                .toList();
    }

    @Override
    public ResponseEntity<ResponseObject> getPersonalityTypes() {
        List<PersonalityType> personalityTypes = personalityTypeRepo.findAll();
        Map<String, List<PersonalityType>> result = new LinkedHashMap<>();
        for (PersonalityType p : personalityTypes) {
            String group = p.getPersonalityTypeGroup().getValue();
            result
                    .computeIfAbsent(group, k -> new ArrayList<>())
                    .add(p);
        }
        return ResponseBuilder.build(HttpStatus.OK, "Get personality types successfully", result);
    }

    @Override
    public ResponseEntity<ResponseObject> getAllMajors() {
        List<Map<String, Object>> result = majorRepo.findAll().stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(
                        m -> Optional.ofNullable(m.getGroup()).orElse("UNKNOWN")
                ))
                .entrySet()
                .stream()
                .map(e -> Map.of(
                        "group", e.getKey(),
                        "majors", e.getValue().stream()
                                .filter(Objects::nonNull)
                                .map(m -> Map.of(
                                        "code", Optional.ofNullable(m.getCode()).orElse(0L),
                                        "name", Optional.ofNullable(m.getName()).orElse("N/A")
                                ))
                                .toList()
                ))
                .toList();

        return ResponseBuilder.build(
                HttpStatus.OK,
                "Get all majors successfully",
                result
        );
    }

    @Override
    public ResponseEntity<ResponseObject> getAllSubjects() {

        List<Subject> subjects = subjectRepo.findAll();

        List<Map<String, Object>> result = subjects.stream()
                .collect(Collectors.groupingBy(Subject::getType))
                .entrySet()
                .stream()
                .map(entry -> {
                    Map<String, Object> groupMap = new HashMap<>();

                    // 🔥 dùng value thay vì name()
                    groupMap.put("type", entry.getKey().getValue());

                    // label cho FE
                    String label = switch (entry.getKey()) {
                        case REGULAR_SUBJECT -> "Môn học chính";
                        case FOREIGN_LANGUAGE_SUBJECT -> "Ngoại ngữ";
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

        return ResponseBuilder.build(
                HttpStatus.OK,
                "Get all subjects successfully",
                result
        );
    }

    @Override
    public ResponseEntity<ResponseObject> addStudentInfo(AddStudentInfoRequest request) {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Your account is restricted", null);
        }

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        Account account = AuthRequestUtil.extractAuthenticatedAccount();

        String error = validateAddStudentInfoRequest(request, email);

        if(!error.isEmpty()){
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, error, null);
        }

        List<Map<String, Object>> academicProfileMetaData = new ArrayList<>();

        for (AddStudentInfoRequest.AcademicInfo academicInfo : request.getAcademicInfos()) {
            Map<String, Object> academicMap = new HashMap<>();
            academicMap.put("gradeLevel", normalize(academicInfo.getGradeLevel()));

            List<Map<String, Object>> subjectResultList = new ArrayList<>();

            if (academicInfo.getSubjectResults() != null) {
                for (AddStudentInfoRequest.SubjectResult subjectResult : academicInfo.getSubjectResults()) {
                    Map<String, Object> subjectMap = new HashMap<>();
                    subjectMap.put("subjectName", subjectResult.getSubjectName());
                    subjectMap.put("score", subjectResult.getScore());
                    subjectResultList.add(subjectMap);
                }
            }
            academicMap.put("subjectResults", subjectResultList);
            academicProfileMetaData.add(academicMap);
        }

            studentInfoRepo.save(StudentProfile.builder()
                        .studentName(normalize(request.getStudentName()))
                        .parent(account.getParent())
                        .favouriteJob(normalize(request.getFavouriteJob()))
                        .gender(Gender.valueOf(normalize(request.getGender())))
                        .personalityTypeName(normalize(request.getPersonalityTypeCode()))
                        .academicProfileMetadata(academicProfileMetaData)
                .build());
        return ResponseBuilder.build(HttpStatus.OK, "Add student info successfully", null);
    }

    @Override
    public ResponseEntity<ResponseObject> getStudents() {
        List<Map<String, Object>> result = studentInfoRepo.findAll().stream()
                .map(this::buildStudentProfile)
                .toList();

        return ResponseBuilder.build(
                HttpStatus.OK,
                "Get student infos successfully",
                result
        );
    }

    @Override
    public ResponseEntity<ResponseObject> addFavouriteSchool(AddFavouriteSchoolRequest request) {
        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Your account is restricted", null);
        }

        return null;
    }

    private Map<String, Object> buildStudentProfile(StudentProfile studentProfile) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", studentProfile.getId());
        result.put("studentName", studentProfile.getStudentName());
        result.put("gender", studentProfile.getGender());
        result.put("personalityTypeCode", studentProfile.getPersonalityTypeName());
        result.put("favouriteJob", studentProfile.getFavouriteJob());
        result.put("academicProfileMetadata", studentProfile.getAcademicProfileMetadata());
        return result;
    }

    private String validateAddStudentInfoRequest(AddStudentInfoRequest request, String parentEmail) {
        if (request == null) {
           return "Request must not be null";
        }
        if (isBlank(request.getStudentName())) {
            return "Child name must not be blank";
        }
        if (request.getStudentName().length() > 200) {
            return "Child name must be less than or equal to 200 characters";
        }
        if (studentInfoRepo.existsByStudentNameIgnoreCaseAndParent_Account_Email(request.getStudentName().trim(), parentEmail)) {
            return "You already already has a child with the same name";
        }
        if (isBlank(request.getGender())) {
            return "Gender is required";
        }
        if (parseGender(request.getGender()) == null) {
            return "Invalid gender";
        }
        if (request.getAcademicInfos() == null || request.getAcademicInfos().isEmpty()) {
            return "Academic infos must not be empty";
        }

        Optional<PersonalityType> personalityType = personalityTypeRepo.findByCode(request.getPersonalityTypeCode());

        if(personalityType.isEmpty()){
            return "Invalid personality type";
        }

        Optional<Major> major = majorRepo.findByName(request.getFavouriteJob());

        if(major.isEmpty()){
            return "Invalid major";
        }

        List<Subject> allSubjects = subjectRepo.findAll();
        if (allSubjects.isEmpty()) {
            return  "Subject data is empty";
        }

        Set<String> gradeLevels = new HashSet<>();
        for (int i = 0; i < request.getAcademicInfos().size(); i++) {

            AddStudentInfoRequest.AcademicInfo academicInfo = request.getAcademicInfos().get(i);
            if (academicInfo == null) {
              return  "Academic info at index " + i + " must not be null";
            }

            if (isBlank(academicInfo.getGradeLevel())) {
                return "Grade level is required at academic info index " + i;
            }
            if (parseGrade(academicInfo.getGradeLevel()) == null) {
                return "Invalid grade level";
            }
            String normalizedGradeLevel = academicInfo.getGradeLevel().trim().toLowerCase();
            if (!gradeLevels.add(normalizedGradeLevel)) {
                return  "Duplicate grade level: " + academicInfo.getGradeLevel();
            }
            String error = validateAcademicInfoSubjects(academicInfo, i);
            if(!error.isEmpty()){
                return error;
            }
        }
        return "";
    }

    private Gender parseGender(String value) {
        String normalizedValue = normalize(value);
        if (normalizedValue == null) {
            return null;
        }

        return Arrays.stream(Gender.values())
                .filter(g -> g.getValue().equalsIgnoreCase(normalizedValue) || g.name().equalsIgnoreCase(normalizedValue))
                .findFirst()
                .orElse(null);
    }

    private GradeLevel parseGrade(String value) {
        String normalizedValue = normalize(value);
        if (normalizedValue == null) {
            return null;
        }

        return Arrays.stream(GradeLevel.values())
                .filter(g -> g.getValue().equalsIgnoreCase(normalizedValue) || g.name().equalsIgnoreCase(normalizedValue))
                .findFirst()
                .orElse(null);
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String validateAcademicInfoSubjects(
            AddStudentInfoRequest.AcademicInfo academicInfo,
            int academicIndex
    ) {
        List<AddStudentInfoRequest.SubjectResult> subjectResults = academicInfo.getSubjectResults();

        if (subjectResults == null) {
           return "Subject results must not be null at academic info index " + academicIndex;
        }
        Set<String> providedSubjectIds = new HashSet<>();
        for (int j = 0; j < subjectResults.size(); j++) {
            AddStudentInfoRequest.SubjectResult subjectResult = subjectResults.get(j);
            if (subjectResult == null) {
              return "Subject result must not be null at academic info index "
                      + academicIndex + ", subject index " + j;
            }
            if (subjectResult.getSubjectName() == null) {
               return "Subject name is required at academic info index "
                    + academicIndex + ", subject index " + j;
            }
            if (!providedSubjectIds.add(subjectResult.getSubjectName())) {
                return  "Duplicate subject name: " + subjectResult.getSubjectName()
                        + " at academic info index " + academicIndex;
            }
            Optional<Subject> subject = subjectRepo.findByName((subjectResult.getSubjectName()));
            if(subject.isEmpty()) {
                return "Subject not found with name: " + subjectResult.getSubjectName();
            }
            Double score = subjectResult.getScore();
            if (score == null){
                return  "";
            }
            if (score < 0 || score > 10) {
                return "Score must be between 0 and 10 for subject '"
                                + subject.get().getName()
                                + "' at academic info index " + academicIndex
                                + ", subject index " + j;
            }
        }
        return "";
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

}
