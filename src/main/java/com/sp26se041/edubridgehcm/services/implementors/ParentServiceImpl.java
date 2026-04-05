package com.sp26se041.edubridgehcm.services.implementors;

import com.sp26se041.edubridgehcm.enums.Gender;
import com.sp26se041.edubridgehcm.enums.GradeLevel;
import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.models.Account;
import com.sp26se041.edubridgehcm.models.Campus;
import com.sp26se041.edubridgehcm.models.ChatMessage;
import com.sp26se041.edubridgehcm.models.Conversation;
import com.sp26se041.edubridgehcm.models.FavouriteSchool;
import com.sp26se041.edubridgehcm.models.Major;
import com.sp26se041.edubridgehcm.models.Parent;
import com.sp26se041.edubridgehcm.models.PersonalityType;
import com.sp26se041.edubridgehcm.models.School;
import com.sp26se041.edubridgehcm.models.StudentProfile;
import com.sp26se041.edubridgehcm.models.Subject;
import com.sp26se041.edubridgehcm.repositories.AccountRepo;
import com.sp26se041.edubridgehcm.repositories.CampusRepo;
import com.sp26se041.edubridgehcm.repositories.ChatMessageRepo;
import com.sp26se041.edubridgehcm.repositories.ConversationRepo;
import com.sp26se041.edubridgehcm.repositories.FavouriteSchoolRepo;
import com.sp26se041.edubridgehcm.repositories.MajorRepo;
import com.sp26se041.edubridgehcm.repositories.ParentRepo;
import com.sp26se041.edubridgehcm.repositories.PersonalityTypeRepo;
import com.sp26se041.edubridgehcm.repositories.SchoolRepo;
import com.sp26se041.edubridgehcm.repositories.StudentInfoRepo;
import com.sp26se041.edubridgehcm.repositories.SubjectRepo;
import com.sp26se041.edubridgehcm.requests.AddFavouriteSchoolRequest;
import com.sp26se041.edubridgehcm.requests.AddStudentInfoRequest;
import com.sp26se041.edubridgehcm.requests.UpdateStudentInfoRequest;
import com.sp26se041.edubridgehcm.responses.PageResponse;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.ParentService;
import com.sp26se041.edubridgehcm.utils.AccountRestrictionUtil;
import com.sp26se041.edubridgehcm.utils.AuthRequestUtil;
import com.sp26se041.edubridgehcm.utils.PaginationUtil;
import com.sp26se041.edubridgehcm.utils.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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

    private final SchoolRepo schoolRepo;

    private final ParentRepo parentRepo;

    private final FavouriteSchoolRepo favouriteSchoolRepo;

    private final CampusRepo campusRepo;


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
                            .findTop20ByParentEmailAndStudentProfileIsNotNullOrderByIdDesc(email);
            } else {
                    conversations = conversationRepo
                            .findTop20ByParentEmailAndIdLessThanAndStudentProfileIsNotNullOrderByIdDesc(email, cursorId);
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
    public ResponseEntity<ResponseObject> getChatHistory(String parentEmail, int campusId, int studentProfileId, Long cursorId) {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Your account is restricted", null);
        }

        Optional<Account> accParent = accountRepo.findByEmail(parentEmail);

        if (accParent.isEmpty()) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Parent not found or be deleted", null);
        }

        Optional<Campus> campus = campusRepo.findById(campusId);

        if (campus.isEmpty()) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Campus not found or be deleted", null);
        }

        Optional<StudentProfile> studentProfile = studentInfoRepo.findById(studentProfileId);

        if(studentProfile.isEmpty()) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Student profile not found or be deleted", null);
        }

        Optional<Conversation> existingConversation =
                conversationRepo.findByParentEmailAndCampusIdAndStudentProfile(parentEmail, campusId, studentProfile.get());

        List<ChatMessage> messages = new ArrayList<>();

        boolean hasMore = false;
        Long nextCursorId = null;

        if (existingConversation.isPresent()) {
            Optional<Account> counsellorAcc = accountRepo.findByEmail(existingConversation.get().getCounsellorEmail());

            if (counsellorAcc.isEmpty()) {
                return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Counsellor not found or be deleted", null);
            }

            if(AccountRestrictionUtil.isRestricted(counsellorAcc.get())) {
                return ResponseBuilder.build(HttpStatus.CONFLICT, "Counsellor account is not active. Please create a new conversation.", null);
            }

            if (cursorId == null) {
                messages = chatMessageRepo.findTop20ByConversationIdOrderByTimestampDesc(existingConversation.get().getId());
            } else {
                messages = chatMessageRepo.findTop20ByConversationIdAndIdLessThanOrderByIdDesc(existingConversation.get().getId(), cursorId);
                hasMore = messages.size() == 20;
                nextCursorId = messages.isEmpty() ? null : messages.get(messages.size() - 1).getId();
            }

            return ResponseBuilder.build(HttpStatus.OK, "Success", buildHistoryMessages(existingConversation.get(), studentProfile.get(), messages, hasMore, nextCursorId));

        }
             Conversation conservation = Conversation.builder()
                    .parentEmail(parentEmail)
                    .counsellorEmail("N/A")
                    .campusId(campusId)
                    .studentProfile(studentProfile.get())
                    .status(Status.CONVERSATION_PENDING)
                    .build();
            conversationRepo.save(conservation);

            return ResponseBuilder.build(HttpStatus.OK, "Success", buildHistoryMessages(conservation, studentProfile.get(), messages, hasMore, cursorId));

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

                    Optional<Campus> campus = campusRepo.findById(conversation.getCampusId());

                    if (campus.isEmpty()) {
                        return map;
                    }

                    map.put("conversationId", conversation.getId());

                    map.put("lastMessage", lastMessage != null ? lastMessage.getMessage() : null);
                    map.put("updatedAt", conversation.getUpdatedDate());
                    map.put("unreadCount", unreadCount != null ? unreadCount : 0L);
                    map.put("otherUser", conversation.getCounsellorEmail());
                    map.put("schoolId", campus.get().getSchool().getId());
                    map.put("schoolName", campus.get().getSchool().getName());
                    map.put("schoolLogoUrl", campus.get().getSchool().getLogoUrl());
                    map.put("studentId", conversation.getStudentProfile().getId());
                    map.put("studentName", conversation.getStudentProfile().getStudentName());
                    map.put("status", conversation.getStatus());

                    return map;

                })
                .toList();
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

    // FAVOURITE SCHOOLS

    @Override
    public ResponseEntity<ResponseObject> addFavouriteSchool(AddFavouriteSchoolRequest request) {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Your account is restricted", null);
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Parent parent = parentRepo.findByAccount_Email(email)
                .orElse(null);

        if (parent == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Parent not found", null);
        }

        School school = schoolRepo.findById(request.getSchoolId())
                .orElse(null);
        if (school == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "School not found", null);
        }

        boolean exists = favouriteSchoolRepo.existsByParentAndSchool(parent, school);
        if (exists) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "School is already in favourite list", null);
        }

        FavouriteSchool favouriteSchool = FavouriteSchool.builder()
                .parent(parent)
                .school(school)
                .build();

        favouriteSchoolRepo.save(favouriteSchool);

        return ResponseBuilder.build(HttpStatus.OK, "Add favourite school successfully", null);
    }



    @Override
    public ResponseEntity<ResponseObject> getFavouriteSchools(int page, int size) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Parent parent = parentRepo.findByAccount_Email(email)
                .orElse(null);

        if (parent == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Parent not found", null);
        }

        Pageable pageable;

        try {
            pageable = PaginationUtil.buildPageRequest(page, size);
        } catch (IllegalArgumentException e) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, e.getMessage(), null);
        }

        Page<FavouriteSchool> favouriteSchoolPage = favouriteSchoolRepo.findByParentId(parent.getId(), pageable);

        PageResponse<Map<String, Object>> result = PaginationUtil.buildPageResponse(favouriteSchoolPage, this::buildFavouriteSchool);

        return ResponseBuilder.build(HttpStatus.OK, "Get favourite school successfully", result);
    }

    @Override
    public ResponseEntity<ResponseObject> removeFavouriteSchool(long favouriteSchoolId) {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Your account is restricted", null);
        }

        Optional<FavouriteSchool> favouriteSchool = favouriteSchoolRepo.findById(favouriteSchoolId);

        if(favouriteSchool.isEmpty()) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Already remove favourite school", null);
        }

        favouriteSchoolRepo.delete(favouriteSchool.get());

        return ResponseBuilder.build(HttpStatus.OK, "Remove favourite school successfully", null);
    }

    private Map<String, Object> buildFavouriteSchool(FavouriteSchool favouriteSchool) {

        Map<String, Object> map = new HashMap<>();

        School school = favouriteSchool.getSchool();

        if (school == null) {
            return map;
        }

        map.put("id", favouriteSchool.getId());
        map.put("schoolId", school.getId());
        map.put("name", school.getName());
        map.put("description", school.getDescription());
        map.put("totalCampus", school.getCampusList() != null ? school.getCampusList().size() : 0);
        map.put("logoUrl", school.getLogoUrl());
        map.put("websiteUrl", school.getWebsiteUrl());
        map.put("representativeName", school.getRepresentativeName());
        map.put("hotline", school.getHotline());
        map.put("averageRating", school.getAverageRating());
        map.put("foundingDate", school.getFoundingDate());
        return map;
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

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    //Add Student Info

    @Override
    public ResponseEntity<ResponseObject> addStudentInfo(AddStudentInfoRequest request) {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Your account is restricted", null);
        }

        Account account = AuthRequestUtil.extractAuthenticatedAccount();

        String error = validateAddStudentInfoRequest(request);

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
    public ResponseEntity<ResponseObject> updateStudentInfo(UpdateStudentInfoRequest request) {

        Optional<StudentProfile> studentProfile = studentInfoRepo.findById(request.getStudentId());

        if(studentProfile.isEmpty()) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Student profile not found", null);
        }

        String error = validateUpdateStudentInfoRequest(request);

        if (!error.isEmpty()){
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, error, null);
        }

        List<Map<String, Object>> academicProfileMetaData = new ArrayList<>();

        for (UpdateStudentInfoRequest.AcademicInfo academicInfo : request.getAcademicInfos()) {
            Map<String, Object> academicMap = new HashMap<>();
            academicMap.put("gradeLevel", normalize(academicInfo.getGradeLevel()));

            List<Map<String, Object>> subjectResultList = new ArrayList<>();

            if (academicInfo.getSubjectResults() != null) {
                for (UpdateStudentInfoRequest.SubjectResult subjectResult : academicInfo.getSubjectResults()) {
                    Map<String, Object> subjectMap = new HashMap<>();
                    subjectMap.put("subjectName", subjectResult.getSubjectName());
                    subjectMap.put("score", subjectResult.getScore());
                    subjectResultList.add(subjectMap);
                }
            }
            academicMap.put("subjectResults", subjectResultList);
            academicProfileMetaData.add(academicMap);
        }

        studentProfile.get().setStudentName(normalize(request.getStudentName()));
        studentProfile.get().setGender(parseGender(request.getGender()));
        studentProfile.get().setFavouriteJob(normalize(request.getFavouriteJob()));
        studentProfile.get().setPersonalityTypeName(normalize(request.getPersonalityTypeCode()));
        studentProfile.get().setAcademicProfileMetadata(academicProfileMetaData);

        return ResponseBuilder.build(HttpStatus.OK, "Update student info successfully", null);
    }

    private String validateUpdateStudentInfoRequest(UpdateStudentInfoRequest request) {
        if (request == null) {
            return "Request must not be null";
        }
        if (isBlank(request.getStudentName())) {
            return "Child name must not be blank";
        }
        if (request.getStudentName().length() > 200) {
            return "Child name must be less than or equal to 200 characters";
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

            UpdateStudentInfoRequest.AcademicInfo academicInfo = request.getAcademicInfos().get(i);
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
            String error = validateAcademicSubjectsForUpdate(academicInfo, i);
            if(!error.isEmpty()){
                return error;
            }
        }
        return "";
    }

    private String validateAddStudentInfoRequest(AddStudentInfoRequest request) {
        if (request == null) {
            return "Request must not be null";
        }
        if (isBlank(request.getStudentName())) {
            return "Child name must not be blank";
        }
        if (request.getStudentName().length() > 200) {
            return "Child name must be less than or equal to 200 characters";
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
            String error = validateAcademicSubjectsForCreate(academicInfo, i);
            if(!error.isEmpty()){
                return error;
            }
        }
        return "";
    }

    private String validateAcademicSubjectsForCreate(
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

    private String validateAcademicSubjectsForUpdate(
            UpdateStudentInfoRequest.AcademicInfo academicInfo,
            int academicIndex
    ) {
        List<UpdateStudentInfoRequest.SubjectResult> subjectResults = academicInfo.getSubjectResults();

        if (subjectResults == null) {
            return "Subject results must not be null at academic info index " + academicIndex;
        }
        Set<String> providedSubjectIds = new HashSet<>();
        for (int j = 0; j < subjectResults.size(); j++) {
            UpdateStudentInfoRequest.SubjectResult subjectResult = subjectResults.get(j);
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

    // Get config admin persona, subject, major
    @Override
    public ResponseEntity<ResponseObject> getPersonalityTypes() {

        List<PersonalityType> personalityTypes = personalityTypeRepo.findAllByStatus(Status.PERSONALITY_TYPE_ACTIVE);

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

        List<Subject> subjects = subjectRepo.findByStatus(Status.SUBJECT_ACTIVE);

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

}
