package com.sp26se041.edubridgehcm.services.implementors;

import com.sp26se041.edubridgehcm.enums.Gender;
import com.sp26se041.edubridgehcm.enums.GradeLevel;
import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.enums.SubjectType;
import com.sp26se041.edubridgehcm.models.Account;
import com.sp26se041.edubridgehcm.models.AdmissionCampaign;
import com.sp26se041.edubridgehcm.models.Campus;
import com.sp26se041.edubridgehcm.models.CampusScheduleTemplate;
import com.sp26se041.edubridgehcm.models.ChatMessage;
import com.sp26se041.edubridgehcm.models.ConsultationOfflineRequest;
import com.sp26se041.edubridgehcm.models.Conversation;
import com.sp26se041.edubridgehcm.models.Counsellor;
import com.sp26se041.edubridgehcm.models.FavouriteSchool;
import com.sp26se041.edubridgehcm.models.Major;
import com.sp26se041.edubridgehcm.models.Parent;
import com.sp26se041.edubridgehcm.models.PersonalityType;
import com.sp26se041.edubridgehcm.models.School;
import com.sp26se041.edubridgehcm.models.SchoolConfig;
import com.sp26se041.edubridgehcm.models.StudentProfile;
import com.sp26se041.edubridgehcm.models.Subject;
import com.sp26se041.edubridgehcm.repositories.AccountRepo;
import com.sp26se041.edubridgehcm.repositories.AdmissionCampaignRepo;
import com.sp26se041.edubridgehcm.repositories.CampusRepo;
import com.sp26se041.edubridgehcm.repositories.CampusScheduleTemplateRepo;
import com.sp26se041.edubridgehcm.repositories.ChatMessageRepo;
import com.sp26se041.edubridgehcm.repositories.ConsultationOfflineRequestRepo;
import com.sp26se041.edubridgehcm.repositories.ConversationRepo;
import com.sp26se041.edubridgehcm.repositories.CounsellorRepo;
import com.sp26se041.edubridgehcm.repositories.FavouriteSchoolRepo;
import com.sp26se041.edubridgehcm.repositories.MajorRepo;
import com.sp26se041.edubridgehcm.repositories.ParentRepo;
import com.sp26se041.edubridgehcm.repositories.PersonalityTypeRepo;
import com.sp26se041.edubridgehcm.repositories.SchoolConfigRepo;
import com.sp26se041.edubridgehcm.repositories.SchoolRepo;
import com.sp26se041.edubridgehcm.repositories.StudentInfoRepo;
import com.sp26se041.edubridgehcm.repositories.SubjectRepo;
import com.sp26se041.edubridgehcm.requests.AddFavouriteSchoolRequest;
import com.sp26se041.edubridgehcm.requests.AddStudentInfoRequest;
import com.sp26se041.edubridgehcm.requests.AutoFillTranscriptRequest;
import com.sp26se041.edubridgehcm.requests.CreateConsultationOfflineRequest;
import com.sp26se041.edubridgehcm.requests.CreateConversationRequest;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

import static com.sp26se041.edubridgehcm.enums.Status.CONSULTATION_CANCELLED;
import static com.sp26se041.edubridgehcm.enums.Status.CONSULTATION_COMPLETED;
import static com.sp26se041.edubridgehcm.enums.Status.CONSULTATION_CONFIRMED;
import static com.sp26se041.edubridgehcm.enums.Status.CONSULTATION_IN_PROGRESS;
import static com.sp26se041.edubridgehcm.enums.Status.CONSULTATION_NO_SHOW;
import static com.sp26se041.edubridgehcm.enums.Status.CONSULTATION_PENDING;

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

    private final CounsellorRepo counsellorRepo;

    private final RestTemplate restTemplate = new RestTemplate();

    private final CampusScheduleTemplateRepo campusScheduleTemplateRepo;

    private final ConsultationOfflineRequestRepo consultationOfflineRequestRepo;

    private final AdmissionCampaignRepo admissionCampaignRepo;

    private final SchoolConfigRepo schoolConfigRepo;

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
                    "",
                    result
            );
        } catch (Exception e) {
            return ResponseBuilder.build(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Thất bại: " + e.getMessage(),
                    null
            );
        }
    }

    @Override
    public ResponseEntity<ResponseObject> getChatHistory(String parentEmail, int campusId, int studentProfileId, Long cursorId) {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Tài khoản của bạn đã bị vô hiệu", null);
        }

        Optional<Account> accParent = accountRepo.findByEmail(parentEmail);

        if (accParent.isEmpty()) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Tài khoản phụ huynh không tìm thấy", null);
        }

        Optional<Campus> campus = campusRepo.findById(campusId);

        if (campus.isEmpty()) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Cơ sở không tìm thấy", null);
        }

        Optional<StudentProfile> studentProfile = studentInfoRepo.findById(studentProfileId);

        if(studentProfile.isEmpty()) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Thông tin trẻ không tồn tại trong hệ thống", null);
        }

        Optional<Conversation> existingConversation =
                conversationRepo.findByParentEmailAndCampusIdAndStudentProfile(parentEmail, campusId, studentProfile.get());

        List<ChatMessage> messages = new ArrayList<>();

        boolean hasMore = false;
        Long nextCursorId = null;

        if (existingConversation.isPresent()) {

            if (cursorId == null) {
                messages = chatMessageRepo.findTop20ByConversationIdOrderByTimestampDesc(existingConversation.get().getId());
            } else {
                messages = chatMessageRepo.findTop20ByConversationIdAndIdLessThanOrderByIdDesc(existingConversation.get().getId(), cursorId);
            }
            hasMore = messages.size() == 20;
            nextCursorId = messages.isEmpty() ? null : messages.get(messages.size() - 1).getId();
            return ResponseBuilder.build(HttpStatus.OK, "", buildHistoryMessages(existingConversation.get(), studentProfile.get(), messages, hasMore, nextCursorId));

        }

             Conversation conservation = Conversation.builder()
                    .parentEmail(parentEmail)
                    .counsellorEmail("N/A")
                    .campusId(campusId)
                    .studentProfile(studentProfile.get())
                     .build();

            return ResponseBuilder.build(HttpStatus.OK, "", buildHistoryMessages(conservation, studentProfile.get(), messages, hasMore, cursorId));

    }

    private Map<String, Object> buildHistoryMessages(Conversation conversation, StudentProfile childProfile, List<ChatMessage> messages, boolean hasMore, Long nextCursorId) {

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

    @Override
    public ResponseEntity<ResponseObject> getStudents() {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        List<Map<String, Object>> result = studentInfoRepo.findByParent_Account_Email(email).stream()
                .map(this::buildStudentProfile)
                .toList();

        return ResponseBuilder.build(
                HttpStatus.OK,
                "",
                result
        );
    }

    @Override
    public ResponseEntity<ResponseObject> autoFillTranscript(AutoFillTranscriptRequest request) {

        Map<String, Object> payload = new LinkedHashMap<>();

        List<Map<String, Object>> items = request.getImages()
                .stream()
                .map(item -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("gradeLevel", item.getGradeLevel());
                    map.put("imageUrl", item.getImageUrl());
                    return map;
                })
                .toList();;

        payload.put("images", items);
        payload.put("subjectsInSystem", getSubjects());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    "https://n8n-service-ijbl.onrender.com/webhook/ocr-academic",
                    entity,
                    String.class
            );

            String rawBody = response.getBody();

            if (rawBody == null || rawBody.isBlank()) {
                return ResponseBuilder.build(
                        HttpStatus.BAD_GATEWAY,
                        "n8n không trả dữ liệu",
                        null
                );
            }

            ObjectMapper objectMapper = new ObjectMapper();

            Map<String, Object> responseBody = objectMapper.readValue(
                    rawBody,
                    new TypeReference<>() {
                    }
            );

            String message = responseBody.get("message") != null
                    ? responseBody.get("message").toString()
                    : "Thành công";

            Object body = responseBody.get("body");

            return ResponseBuilder.build(
                    HttpStatus.OK,
                    message,
                    body
            );

        } catch (HttpClientErrorException e) {
            String rawError = e.getResponseBodyAsString();

            if (rawError == null || rawError.isBlank()) {
                return ResponseBuilder.build(
                        HttpStatus.BAD_REQUEST,
                        "Yêu cầu không hợp lệ",
                        null
                );
            }

            try {
                ObjectMapper objectMapper = new ObjectMapper();

                Map<String, Object> errorBody = objectMapper.readValue(
                        rawError,
                        new TypeReference<>() {
                        }
                );

                String message = errorBody.get("message") != null
                        ? errorBody.get("message").toString()
                        : "Yêu cầu không hợp lệ";

                Object body = errorBody.get("body");

                return ResponseBuilder.build(
                        HttpStatus.BAD_REQUEST,
                        message,
                        body
                );
            } catch (Exception ex) {
                return ResponseBuilder.build(
                        HttpStatus.BAD_REQUEST,
                        rawError,
                        null
                );
            }

        } catch (HttpServerErrorException e) {
            return ResponseBuilder.build(
                    HttpStatus.BAD_GATEWAY,
                    "n8n đang lỗi phía server",
                    null
            );

        } catch (Exception e) {
            return ResponseBuilder.build(
                    HttpStatus.BAD_GATEWAY,
                    "Không gọi được n8n workflow: " + e.getMessage(),
                    null
            );
        }

    }

    @Override
    public ResponseEntity<ResponseObject> getStudentInfo(int studentProfileId) {

        Optional<StudentProfile> studentProfile = studentInfoRepo.findById(studentProfileId);

        if(studentProfile.isEmpty()) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Thông tin trẻ không tồn tại trong hệ thốngd", null);
        }

        return ResponseBuilder.build(HttpStatus.OK, "", buildStudentProfile(studentProfile.get()));
    }

    // FAVOURITE SCHOOLS

    @Override
    public ResponseEntity<ResponseObject> addFavouriteSchool(AddFavouriteSchoolRequest request) {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "", null);
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Parent parent = parentRepo.findByAccount_Email(email)
                .orElse(null);

        if (parent == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Tài khoản phụ huynh không tìm thấy", null);
        }

        School school = schoolRepo.findById(request.getSchoolId())
                .orElse(null);
        if (school == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Trường không tồn tại trong hệ thống", null);
        }

        boolean exists = favouriteSchoolRepo.existsByParentAndSchool(parent, school);
        if (exists) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Trường đã tồn tại trong danh sách trường yêu thích", null);
        }

        FavouriteSchool favouriteSchool = FavouriteSchool.builder()
                .parent(parent)
                .school(school)
                .createdAt(LocalDateTime.now())
                .build();

        favouriteSchoolRepo.save(favouriteSchool);

        //notification when parent click add favorite for school

        return ResponseBuilder.build(HttpStatus.OK, "", null);
    }

    @Override
    public ResponseEntity<ResponseObject> getFavouriteSchools(int page, int size) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Parent parent = parentRepo.findByAccount_Email(email)
                .orElse(null);

        if (parent == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Tài khoản phụ huynh không tìm thấy", null);
        }

        Pageable pageable;

        try {
            pageable = PaginationUtil.buildPageRequest(page, size);
        } catch (IllegalArgumentException e) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, e.getMessage(), null);
        }

        Page<FavouriteSchool> favouriteSchoolPage = favouriteSchoolRepo.findByParentIdOrderByCreatedAtDesc(parent.getId(), pageable);

        PageResponse<Map<String, Object>> result = PaginationUtil.buildPageResponse(favouriteSchoolPage, this::buildFavouriteSchool);

        return ResponseBuilder.build(HttpStatus.OK, "", result);
    }

    @Transactional
    @Override
    public ResponseEntity<ResponseObject> removeFavouriteSchool(long favouriteSchoolId) {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Tài khoản của bạn đã bị vô hiệu", null);
        }

        boolean exists = favouriteSchoolRepo.existsById(favouriteSchoolId);

        if (!exists) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Lỗi: Không tìm thấy dữ liệu", null);
        }

        favouriteSchoolRepo.deleteAllByIdInBatch(List.of(favouriteSchoolId));
        favouriteSchoolRepo.flush();

        return ResponseBuilder.build(HttpStatus.OK, "", null);
    }

    @Override
    public ResponseEntity<ResponseObject> createConversation(CreateConversationRequest request) {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Tài khoản của bạn bị vô hiệu", null);
        }

        Optional<Account> accParent = accountRepo.findByEmail(request.getParentEmail().trim());

        if (accParent.isEmpty()) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Tài khoản phụ huynh không tìm thấy", null);
        }

        Optional<Campus> campus = campusRepo.findById(request.getCampusId());

        if (campus.isEmpty()) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Cơ sở không tồn tại trong hệ thống. Vui lòng làm mới trang", null);
        }

        Optional<StudentProfile> studentProfile = studentInfoRepo.findById(request.getStudentProfileId());

        if (studentProfile.isEmpty()) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Thông tin trẻ không tồn tại trong hệ thống", null);
        }

        Optional<Conversation> existingConversation =
                conversationRepo.findByParentEmailAndCampusIdAndStudentProfile(request.getParentEmail(), request.getCampusId(), studentProfile.get());

        if (existingConversation.isPresent()) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Conversation already exists", null);
        }

        Conversation conservation = Conversation.builder()
                .parentEmail(request.getParentEmail().trim())
                .counsellorEmail("N/A")
                .campusId(request.getCampusId())
                .studentProfile(studentProfile.get())
                .status(Status.CONVERSATION_PENDING)
                .build();

            conversationRepo.save(conservation);

        return ResponseBuilder.build(HttpStatus.CREATED, "", conservation.getId());
    }

    @Override
    public List<String> findCounsellorEmailsByCampusId(int campusId) {

        List<Counsellor> counsellors = counsellorRepo.findByCampus_IdAndAccount_Status(campusId, Status.ACCOUNT_ACTIVE);

        return counsellors.stream()
                .map(Counsellor::getAccount)
                .map(Account::getEmail)
                .collect(Collectors.toList());

    }

    private LocalDate getDateByDayOfWeek(LocalDate startDate, LocalDate endDate, String dayOfWeek) {
        LocalDate date = startDate;

        while (!date.isAfter(endDate)) {
            if (date.getDayOfWeek().name().substring(0, 3).equals(dayOfWeek)) {
                return date;
            }
            date = date.plusDays(1);
        }

        return null;
    }

    private Status getSlotStatus(LocalDate slotDate, LocalTime startTime, LocalTime endTime) {

        LocalDateTime now = LocalDateTime.now();

        LocalDateTime startDateTime = LocalDateTime.of(slotDate, startTime);
        LocalDateTime endDateTime = LocalDateTime.of(slotDate, endTime);

        if (now.isBefore(startDateTime)) {
            return Status.UPCOMING;
        }

        if (now.isAfter(endDateTime)) {
            return Status.PAST;
        }

        return Status.ONGOING;
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

        Optional<PersonalityType> personalityType =
                personalityTypeRepo.findByCode(studentProfile.getPersonalityTypeName());


        Map<String, Object> result = new HashMap<>();
        result.put("id", studentProfile.getId());
        result.put("studentName", studentProfile.getStudentName());
        result.put("gender", studentProfile.getGender());
        result.put("personalityTypeCode", studentProfile.getPersonalityTypeName());
        result.put("favouriteJob", studentProfile.getFavouriteJob());
        result.put("academicProfileMetadata", mergeAcademicProfileMetadata(studentProfile));
        result.put("personalityCode",
                personalityType.map(PersonalityType::getCode).orElse("N/A"));
        result.put("traits",
                personalityType.map(PersonalityType::getTraits).orElse(List.of()));
        result.put("subjectsInSystem", getSubjects());
        result.put("transcriptImages", studentProfile.getTranscriptImages());

        return result;

    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    //Add Student Info

    @Override
    public ResponseEntity<ResponseObject> addStudentInfo(AddStudentInfoRequest request) {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Tài khoản của bạn đã bị vô hiệu", null);
        }

        Account account = AuthRequestUtil.extractAuthenticatedAccount();

        String error = validateAddStudentInfoRequest(request);

        if (!error.isEmpty()) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, error, null);
        }

        List<Map<String, Object>> academicProfileMetaData = new ArrayList<>();

        if (request.getAcademicInfos() != null) {

            for (AddStudentInfoRequest.AcademicInfo academicInfo : request.getAcademicInfos()) {

                // academicInfo null -> bỏ qua
                if (academicInfo == null) {
                    continue;
                }

                String gradeLevel = academicInfo.getGradeLevel();
                List<AddStudentInfoRequest.SubjectResult> subjectResults = academicInfo.getSubjectResults();

                boolean blankGradeLevel = isBlank(gradeLevel);
                boolean emptySubjectResults = subjectResults == null || subjectResults.isEmpty();

                // Dòng rỗng hoàn toàn -> bỏ qua
                if (blankGradeLevel && emptySubjectResults) {
                    continue;
                }

                // gradeLevel rỗng -> đã được validate phía trên, ở đây bỏ qua để an toàn
                if (blankGradeLevel) {
                    continue;
                }

                Map<String, Object> academicMap = new HashMap<>();
                academicMap.put("gradeLevel", normalize(gradeLevel));

                List<Map<String, Object>> subjectResultList = new ArrayList<>();

                if (subjectResults != null) {

                    for (AddStudentInfoRequest.SubjectResult subjectResult : subjectResults) {

                        // subjectResult null -> bỏ qua
                        if (subjectResult == null) {
                            continue;
                        }

                        String subjectName = subjectResult.getSubjectName();
                        Double score = subjectResult.getScore();

                        boolean blankSubjectName = isBlank(subjectName);

                        // Dòng môn học rỗng hoàn toàn -> bỏ qua
                        if (blankSubjectName && score == null) {
                            continue;
                        }

                        // Có điểm nhưng không có tên -> đã validate, ở đây bỏ qua để an toàn
                        if (blankSubjectName) {
                            continue;
                        }

                        Map<String, Object> subjectMap = new HashMap<>();
                        subjectMap.put("subjectName", normalize(subjectName));
                        subjectMap.put("score", score); // có thể null
                        subjectResultList.add(subjectMap);
                    }
                }

                if (!subjectResultList.isEmpty()) {
                    academicMap.put("subjectResults", subjectResultList);
                    academicProfileMetaData.add(academicMap);
                }
            }
        }

        List<Map<String, Object>> transcriptImages = request.getTranscriptImages()
                .stream()
                .map(item -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("grade", item.getGrade());
                    map.put("imageUrl", item.getImageUrl());
                    return map;
                })
                .toList();

        studentInfoRepo.save(StudentProfile.builder()
                .studentName(normalize(request.getStudentName()))
                .parent(account.getParent())
                .transcriptImages(transcriptImages)
                .favouriteJob(normalize(request.getFavouriteJob()))
                .gender(Gender.valueOf(normalize(request.getGender())))
                .personalityTypeName(normalize(request.getPersonalityTypeCode()))
                .academicProfileMetadata(academicProfileMetaData)
                .build());

        return ResponseBuilder.build(HttpStatus.OK, "Thêm thông tin học sinh thành công", null);
    }

    @Override
    public ResponseEntity<ResponseObject> updateStudentInfo(UpdateStudentInfoRequest request) {

        Optional<StudentProfile> studentProfileOpt = studentInfoRepo.findById(request.getStudentId());

        if (studentProfileOpt.isEmpty()) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Thông tin học sinh không tìm thấy", null);
        }

        String error = validateUpdateStudentInfoRequest(request);
        if (!error.isEmpty()) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, error, null);
        }

        StudentProfile studentProfile = studentProfileOpt.get();

        List<Map<String, Object>> academicProfileMetaData = new ArrayList<>();

        if (request.getAcademicInfos() != null) {
            for (UpdateStudentInfoRequest.AcademicInfo academicInfo : request.getAcademicInfos()) {

                // academicInfo null -> bỏ qua
                if (academicInfo == null) {
                    continue;
                }

                String gradeLevel = academicInfo.getGradeLevel();
                List<UpdateStudentInfoRequest.SubjectResult> subjectResults = academicInfo.getSubjectResults();

                boolean blankGradeLevel = isBlank(gradeLevel);
                boolean emptySubjectResults = subjectResults == null || subjectResults.isEmpty();

                // Dòng rỗng hoàn toàn -> bỏ qua
                if (blankGradeLevel && emptySubjectResults) {
                    continue;
                }

                // gradeLevel rỗng -> đã được validate phía trên, ở đây bỏ qua để an toàn
                if (blankGradeLevel) {
                    continue;
                }

                Map<String, Object> academicMap = new HashMap<>();
                academicMap.put("gradeLevel", normalize(gradeLevel));

                List<Map<String, Object>> subjectResultList = new ArrayList<>();

                if (subjectResults != null) {
                    for (UpdateStudentInfoRequest.SubjectResult subjectResult : subjectResults) {

                        // subjectResult null -> bỏ qua
                        if (subjectResult == null) {
                            continue;
                        }

                        String subjectName = subjectResult.getSubjectName();
                        Double score = subjectResult.getScore();

                        boolean blankSubjectName = isBlank(subjectName);

                        // Dòng môn học rỗng hoàn toàn -> bỏ qua
                        if (blankSubjectName && score == null) {
                            continue;
                        }

                        // Có điểm nhưng không có tên -> đã validate phía trên, ở đây bỏ qua để an toàn
                        if (blankSubjectName) {
                            continue;
                        }

                        Map<String, Object> subjectMap = new HashMap<>();
                        subjectMap.put("subjectName", normalize(subjectName));
                        subjectMap.put("score", score); // có thể null
                        subjectResultList.add(subjectMap);
                    }
                }

                if (!subjectResultList.isEmpty()) {
                    academicMap.put("subjectResults", subjectResultList);
                    academicProfileMetaData.add(academicMap);
                }
            }
        }

        List<Map<String, Object>> transcriptImages = request.getTranscriptImages()
                .stream()
                .map(item -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("grade", item.getGrade());
                    map.put("imageUrl", item.getImageUrl());
                    return map;
                })
                .toList();

        studentProfile.setStudentName(normalize(request.getStudentName()));
        studentProfile.setGender(parseGender(request.getGender()));
        studentProfile.setFavouriteJob(normalize(request.getFavouriteJob()));
        studentProfile.setPersonalityTypeName(normalize(request.getPersonalityTypeCode()));
        studentProfile.setAcademicProfileMetadata(academicProfileMetaData);
        studentProfile.setTranscriptImages(transcriptImages);

        studentInfoRepo.save(studentProfile);

        return ResponseBuilder.build(HttpStatus.OK, "Cập nhật thông tin học sinh thành công", null);

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

        return ResponseBuilder.build(
                HttpStatus.OK,
                "Get all subjects successfully",
                result
        );
    }

    private String validateUpdateStudentInfoRequest(UpdateStudentInfoRequest request) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        if (isBlank(request.getStudentName())) {
            return "Tên học sinh không được để trống";
        }

        if (request.getStudentName().trim().length() > 200) {
            return "Tên học sinh không được vượt quá 200 ký tự";
        }

        if (studentInfoRepo.existsByStudentNameIgnoreCaseAndParent_Account_EmailAndIdNot(
                request.getStudentName().trim(),
                email,
                request.getStudentId()
        )) {
            return "Tên học sinh đã tồn tại";
        }

        if (isBlank(request.getGender())) {
            return "Giới tính là bắt buộc";
        }

        if (parseGender(request.getGender()) == null) {
            return "Giới tính không hợp lệ";
        }

        Optional<PersonalityType> personalityType =
                personalityTypeRepo.findByCode(request.getPersonalityTypeCode());
        if (personalityType.isEmpty()) {
            return "Loại tính cách không hợp lệ";
        }

        Optional<Major> major = majorRepo.findByName(request.getFavouriteJob());
        if (major.isEmpty()) {
            return "Ngành nghề yêu thích không hợp lệ";
        }

        if (request.getAcademicInfos() == null || request.getAcademicInfos().isEmpty()) {
            return "";
        }

        Set<String> gradeLevels = new HashSet<>();

        for (int i = 0; i < request.getAcademicInfos().size(); i++) {

            UpdateStudentInfoRequest.AcademicInfo academicInfo = request.getAcademicInfos().get(i);

            // academicInfo null -> bỏ qua
            if (academicInfo == null) {
                continue;
            }

            String gradeLevel = academicInfo.getGradeLevel();
            List<UpdateStudentInfoRequest.SubjectResult> subjectResults = academicInfo.getSubjectResults();

            boolean blankGradeLevel = isBlank(gradeLevel);

            boolean emptySubjectResults = subjectResults == null || subjectResults.isEmpty();

            // Dòng rỗng hoàn toàn -> bỏ qua
            if (blankGradeLevel && emptySubjectResults) {
                continue;
            }

            // Có dữ liệu môn học nhưng chưa nhập khối lớp -> lỗi
            if (blankGradeLevel) {
                return "Khối lớp là bắt buộc khi đã nhập danh sách môn học tại vị trí " + i;
            }

            if (parseGrade(gradeLevel) == null) {
                return "Khối lớp không hợp lệ: " + gradeLevel;
            }

            String normalizedGradeLevel = gradeLevel.trim().toLowerCase();
            if (!gradeLevels.add(normalizedGradeLevel)) {
                return "Khối lớp bị trùng: " + gradeLevel;
            }

            String error = validateAcademicSubjectsForUpdate(subjectResults, normalizedGradeLevel);
            if (!error.isEmpty()) {
                return error;
            }
        }

        return "";
    }

    private String validateAddStudentInfoRequest(AddStudentInfoRequest request) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        if (isBlank(request.getStudentName())) {
            return "Tên học sinh không được để trống";
        }
        if (request.getStudentName().trim().length() > 200) {
            return "Tên học sinh không được vượt quá 200 ký tự";
        }

        if (studentInfoRepo.existsByStudentNameIgnoreCaseAndParent_Account_Email(
                request.getStudentName().trim(),
                email
        )) {
            return "Tên học sinh " + request.getStudentName().trim()  +" đã tồn tại";
        }


        if (isBlank(request.getGender())) {
            return "Giới tính là bắt buộc";
        }
        if (parseGender(request.getGender()) == null) {
            return "Giới tính không hợp lệ";
        }


        if (!isBlank(request.getPersonalityTypeCode())) {

            Optional<PersonalityType> personalityType = personalityTypeRepo.findByCode(request.getPersonalityTypeCode());

            if (personalityType.isEmpty()){
                return "Loại tính cách không hợp lệ";
            }

        }
        if (!isBlank(request.getFavouriteJob())) {

            Optional<Major> major = majorRepo.findByName(request.getFavouriteJob());

            if(major.isEmpty()){
                return "Ngành nghề yêu thích không hợp lệ";
            }

        }


        if (request.getAcademicInfos() != null) {
            if (!request.getAcademicInfos().isEmpty()) {
               Set<String> gradeLevels = new HashSet<>();

               for (int i = 0; i < request.getAcademicInfos().size(); i++) {

                   AddStudentInfoRequest.AcademicInfo academicInfo = request.getAcademicInfos().get(i);

                   // academicInfo null -> bỏ qua
                   if (academicInfo == null) {
                       continue;
                   }

                   String gradeLevel = academicInfo.getGradeLevel();
                   List<AddStudentInfoRequest.SubjectResult> subjectResults = academicInfo.getSubjectResults();

                   boolean blankGradeLevel = isBlank(gradeLevel);
                   boolean emptySubjectResults = subjectResults == null || subjectResults.isEmpty();

                   // Dòng rỗng hoàn toàn -> bỏ qua
                   if (blankGradeLevel && emptySubjectResults) {
                       continue;
                   }

                   // Có dữ liệu môn học nhưng chưa nhập khối lớp -> lỗi
                   if (blankGradeLevel) {
                       return "Khối lớp là bắt buộc khi đã nhập danh sách môn học tại vị trí " + i;
                   }

                   if (parseGrade(gradeLevel) == null) {
                       return "Khối lớp không hợp lệ: " + gradeLevel;
                   }

                   String normalizedGradeLevel = gradeLevel.trim().toLowerCase();
                   if (!gradeLevels.add(normalizedGradeLevel)) {
                       return "Khối lớp bị trùng: " + gradeLevel;
                   }

                   String error = validateAcademicSubjectsForCreate(subjectResults, gradeLevel);

                   if (!error.isEmpty()) {
                       return error;
                   }
               }
            }
        }

        return "";
    }

    private String validateAcademicSubjectsForCreate(
            List<AddStudentInfoRequest.SubjectResult> subjectResults,
            String gradeLevel) {
        if (subjectResults == null || subjectResults.isEmpty()) {
            return "";
        }

        Set<String> providedSubjectNames = new HashSet<>();

        for (int j = 0; j < subjectResults.size(); j++) {
            AddStudentInfoRequest.SubjectResult subjectResult = subjectResults.get(j);

            // subjectResult null -> bỏ qua
            if (subjectResult == null) {
                continue;
            }

            String subjectName = subjectResult.getSubjectName();
            Double score = subjectResult.getScore();

            boolean blankSubjectName = isBlank(subjectName);

            // Dòng môn học rỗng hoàn toàn -> bỏ qua
            if (blankSubjectName && score == null) {
                continue;
            }

            // Có điểm nhưng chưa nhập tên môn học -> lỗi
            if (blankSubjectName && score != null) {
                return "Bạn đã nhập điểm nhưng chưa điền tên môn học tại khối "
                        + gradeLevel + ", vị trí " + j;
            }

            String normalizedSubjectName = subjectName.trim().toLowerCase();
            if (!providedSubjectNames.add(normalizedSubjectName)) {
                return "Môn học bị trùng: " + subjectName + " tại khối " + gradeLevel;
            }

            // subjectName có nhưng score null -> vẫn hợp lệ, vẫn lưu
            if (score != null && (score < 0 || score > 10)) {
                return "Điểm phải nằm trong khoảng từ 0 đến 10 cho môn '"
                        + subjectName + "' tại khối " + gradeLevel;
            }
        }

        return "";
    }

    private String validateAcademicSubjectsForUpdate(
            List<UpdateStudentInfoRequest.SubjectResult> subjectResults,
            String gradeLevel
    ) {
        if (subjectResults == null || subjectResults.isEmpty()) {
            return "";
        }

        Set<String> providedSubjectNames = new HashSet<>();

        for (int j = 0; j < subjectResults.size(); j++) {
            UpdateStudentInfoRequest.SubjectResult subjectResult = subjectResults.get(j);

            if (subjectResult == null) {
                continue;
            }

            String subjectName = subjectResult.getSubjectName();
            Double score = subjectResult.getScore();

            boolean blankSubjectName = isBlank(subjectName);

            if (blankSubjectName && score == null) {
                continue;
            }

            if (blankSubjectName && score != null) {
                return "Bạn đã nhập điểm nhưng chưa điền tên môn học tại khối "
                        + gradeLevel + ", vị trí " + j;
            }

            String normalizedSubjectName = subjectName.trim().toLowerCase();
            if (!providedSubjectNames.add(normalizedSubjectName)) {
                return "Môn học bị trùng: " + subjectName + " tại khối " + gradeLevel;
            }

            if (score != null && (score < 0 || score > 10)) {
                return "Điểm phải nằm trong khoảng từ 0 đến 10 cho môn '"
                        + subjectName + "' tại khối " + gradeLevel;
            }
        }

        return "";
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

    private List<Map<String, Object>> getSubjects() {
        List<Subject> subjects = subjectRepo.findAllByTypeIn(List.of(SubjectType.REGULAR_SUBJECT, SubjectType.FOREIGN_LANGUAGE_SUBJECT));

        return subjects.stream()
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
                    map.put("campusId", campus.get().getId());
                    map.put("lastMessage", lastMessage != null ? lastMessage.getMessage() : null);
                    map.put("updatedAt", conversation.getUpdatedDate());
                    map.put("unreadCount", unreadCount != null ? unreadCount : 0L);
                    map.put("otherUser", conversation.getCounsellorEmail());
                    map.put("campusName",  campus.get().getName());
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

    private List<Map<String, Object>> mergeAcademicProfileMetadata(StudentProfile studentProfile) {

        List<Subject> allSubjects = subjectRepo.findAllByTypeIn( List.of(SubjectType.REGULAR_SUBJECT, SubjectType.FOREIGN_LANGUAGE_SUBJECT));

        List<Map<String, Object>> storedAcademicProfiles =
                (List<Map<String, Object>>) studentProfile.getAcademicProfileMetadata();

        if (storedAcademicProfiles == null || storedAcademicProfiles.isEmpty()) {
            return new ArrayList<>();
        }

        List<Map<String, Object>> mergedAcademicProfiles = new ArrayList<>();

        for (Map<String, Object> academicItem : storedAcademicProfiles) {
            if (academicItem == null) {
                continue;
            }

            String gradeLevel = academicItem.get("gradeLevel") == null
                    ? null
                    : academicItem.get("gradeLevel").toString();

            List<Map<String, Object>> storedSubjectResults =
                    (List<Map<String, Object>>) academicItem.get("subjectResults");

            Map<String, Object> storedSubjectMap = new LinkedHashMap<>();

            // Giữ subject cũ đã lưu
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

                    String normalizedName = subjectName.toLowerCase();

                    Map<String, Object> oldSubject = new HashMap<>();
                    oldSubject.put("id", subjectItem.get("id"));
                    oldSubject.put("name", subjectName);
                    oldSubject.put("type", subjectItem.get("type"));
                    oldSubject.put("score", subjectItem.get("score"));
                    oldSubject.put("isAvailable", false); // mặc định coi là môn cũ không còn trong hệ thống

                    storedSubjectMap.put(normalizedName, oldSubject);
                }
            }

            for (Subject subject : allSubjects) {

                String normalizedName = subject.getName().trim().toLowerCase();

                boolean existedInProfile = storedSubjectMap.containsKey(normalizedName);

                // Môn ngoại ngữ: chỉ hiện nếu đã có dữ liệu
                if (subject.getType() == SubjectType.FOREIGN_LANGUAGE_SUBJECT && !existedInProfile) {
                    continue;
                }

                Map<String, Object> mergedSubject = new HashMap<>();
                mergedSubject.put("id", subject.getId());
                mergedSubject.put("name", subject.getName());
                mergedSubject.put("type", subject.getType().getValue());
                mergedSubject.put("isAvailable", true);

                if (existedInProfile) {
                    Map<String, Object> oldSubject = (Map<String, Object>) storedSubjectMap.get(normalizedName);
                    mergedSubject.put("score", oldSubject.get("score"));
                } else {
                    mergedSubject.put("score", null);
                }

                storedSubjectMap.put(normalizedName, mergedSubject);
            }

            Map<String, Object> mergedAcademicItem = new HashMap<>();
            mergedAcademicItem.put("gradeLevel", gradeLevel);
            mergedAcademicItem.put("subjectResults", new ArrayList<>(storedSubjectMap.values()));

            mergedAcademicProfiles.add(mergedAcademicItem);
        }

        return mergedAcademicProfiles;
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

    @Override
    public ResponseEntity<ResponseObject> getSlots(int campusId, LocalDate startDate, LocalDate endDate) {

        if (startDate.getDayOfWeek() != DayOfWeek.MONDAY) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "startDate phải là Thứ 2", null);
        }

        if (endDate.getDayOfWeek() != DayOfWeek.SUNDAY) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "endDate phải là Chủ nhật", null);
        }

        if (endDate.isBefore(startDate)) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "endDate phải lớn hơn startDate", null);
        }

        if (!startDate.plusDays(6).equals(endDate)) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Phải chọn đúng 1 tuần (7 ngày)", null);
        }

        Optional<Campus> campusOpt = campusRepo.findById(campusId);

        if (campusOpt.isEmpty()) {
            return ResponseBuilder.build(
                    HttpStatus.NOT_FOUND,
                    "Cơ sở không tồn tại. Vui lòng tải lại trang",
                    null
            );
        }

        Campus campus = campusOpt.get();

        Optional<AdmissionCampaign> campaignOpt =
                admissionCampaignRepo.findBySchoolIdAndYearAndStatus(
                        campus.getSchool().getId(),
                        startDate.getYear(),
                        Status.OPEN_ADMISSION_CAMPAIGN
                );

        if (campaignOpt.isEmpty()) {
            return ResponseBuilder.build(
                    HttpStatus.OK,
                    "Lấy danh sách slot thành công",
                    Collections.emptyList()
            );
        }

        AdmissionCampaign campaign = campaignOpt.get();

        List<CampusScheduleTemplate> campusScheduleTemplates =
                campusScheduleTemplateRepo.findByCampusIdAndActiveTrueOrderByStartTimeAsc(campusId);

        Object maxBookingPerSlot = "N/A";
        Object allowBookingBeforeHours = "N/A";

        if (campus.getPolicyDetail() == null) {

            Optional<SchoolConfig> schoolConfig = schoolConfigRepo.findBySchoolIdAndKey(campus.getSchool().getId(), "operationSettingsData");

            if (schoolConfig.isPresent()) {

                SchoolConfig schoolConfigData = schoolConfig.get();

                Map<String, Object> operationSettingsData = (Map<String, Object>) schoolConfigData.getValue();

                maxBookingPerSlot = (int) operationSettingsData.get("maxBookingPerSlot");
                allowBookingBeforeHours =
                        ((Number) operationSettingsData.get("allowBookingBeforeHours")).intValue();
            }
        }

        Map<String, Object> policyDetail = (Map<String, Object>) campus.getPolicyDetail();

        if (policyDetail != null) {
            maxBookingPerSlot = (int) policyDetail.get("maxBookingPerSlot");
            allowBookingBeforeHours =
                    ((Number) policyDetail.get("allowBookingBeforeHours")).intValue();
        }

        List<Map<String, Object>> result = new ArrayList<>();

        for (CampusScheduleTemplate campusScheduleTemplate : campusScheduleTemplates) {

            LocalDate slotDate = getDateByDayOfWeek(
                    startDate,
                    endDate,
                    campusScheduleTemplate.getDayOfWeek()
            );

            if (slotDate == null) {
                continue;
            }

            if (slotDate.isBefore(campaign.getStartDate()) || slotDate.isAfter(campaign.getEndDate())) {
                continue;
            }

            Status status = getSlotStatus(
                    slotDate,
                    campusScheduleTemplate.getStartTime(),
                    campusScheduleTemplate.getEndTime()
            );

            Map<String, Object> slotMap = new HashMap<>();

            slotMap.put("campusScheduleTemplateId", campusScheduleTemplate.getId());
            slotMap.put("admissionCampaignId", campaign.getId());
            slotMap.put("date", slotDate);
            slotMap.put("dayOfWeek", campusScheduleTemplate.getDayOfWeek());
            slotMap.put("startTime", campusScheduleTemplate.getStartTime());
            slotMap.put("endTime", campusScheduleTemplate.getEndTime());
            slotMap.put("status", status);
            slotMap.put("statusLabel", status.getValue());
            slotMap.put("maxBookingPerSlot", maxBookingPerSlot);
            slotMap.put("allowBookingBeforeHours", allowBookingBeforeHours);

            result.add(slotMap);
        }

        result.sort(
                Comparator
                        .comparing((Map<String, Object> m) -> (LocalDate) m.get("date"))
                        .thenComparing(m -> (LocalTime) m.get("startTime"))
        );

        return ResponseBuilder.build(
                HttpStatus.OK,
                "Lấy danh sách slot thành công",
                result
        );
    }

    @Override
    public ResponseEntity<ResponseObject> createConsultationOfflineRequest(CreateConsultationOfflineRequest request) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Optional<Parent> parent = parentRepo.findByAccount_Email(email);

        if (parent.isEmpty()) {
            return ResponseBuilder.build(HttpStatus.UNAUTHORIZED, "Tài khoản phụ huynh không tồn tại", null);
        }

        if (AccountRestrictionUtil.isRestricted(parent.get().getAccount())) {
            return ResponseBuilder.build(HttpStatus.UNAUTHORIZED, "Tài khoản của bạn hiện tại đang bị cấm", null);
        }

        Optional<Campus> campus = campusRepo.findById(request.getCampusId());

        if (campus.isEmpty()) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Cơ sở không tồn tại", null);
        }

        if (request.getPhone() == null || request.getPhone().isBlank()) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Số điện thoại không được để trống", null);
        }

        if (!request.getPhone().matches("^(0|\\+84)(3|5|7|8|9)[0-9]{8}$")) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Số điện thoại không hợp lệ", null);
        }

        if (request.getQuestion() == null || request.getQuestion().isBlank()) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Câu hỏi tư vấn không được để trống", null);
        }

        if (request.getQuestion().length() > 1000) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Câu hỏi tư vấn không được vượt quá 1000 ký tự", null);
        }

        if (request.getAppointmentDate() == null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Ngày hẹn tư vấn không được để trống", null);
        }

        if (request.getAppointmentTime() == null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Giờ hẹn tư vấn không được để trống", null);
        }

        LocalDateTime appointmentDateTime = LocalDateTime.of(
                request.getAppointmentDate(),
                request.getAppointmentTime()
        );

        if (appointmentDateTime.isBefore(LocalDateTime.now())) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Thời gian hẹn phải lớn hơn thời gian hiện tại", null);
        }

        LocalDateTime now = LocalDateTime.now();

        if (appointmentDateTime.isBefore(now)) {
            return ResponseBuilder.build(
                    HttpStatus.BAD_REQUEST,
                    "Thời gian hẹn không được ở quá khứ",
                    null
            );
        }

        boolean hasActive =
                consultationOfflineRequestRepo.existsByParentAndCampusAndAppointmentDateAndStatusIn(
                        parent.get(),
                        campus.get(),
                        request.getAppointmentDate(),
                        List.of(
                                CONSULTATION_PENDING,
                                Status.CONSULTATION_CONFIRMED
                        )
                );

        if (hasActive) {
            return ResponseBuilder.build(
                    HttpStatus.BAD_REQUEST,
                    "Bạn đã có lịch tư vấn cùng ngày tại cơ sở này",
                    null
            );
        }

        if (campus.get().getPolicyDetail() == null) {

            Optional<SchoolConfig> schoolConfig = schoolConfigRepo.findBySchoolIdAndKey(campus.get().getSchool().getId(), "operationSettingsData");

            if (schoolConfig.isEmpty()) {

                ConsultationOfflineRequest consultationOfflineRequest = ConsultationOfflineRequest.builder()
                        .appointmentDate(request.getAppointmentDate())
                        .parent(parent.get())
                        .phone(request.getPhone().trim())
                        .question(request.getQuestion().trim())
                        .appointmentTime(request.getAppointmentTime())
                        .appointmentDate(request.getAppointmentDate())
                        .campus(campus.get())
                        .createdDate(LocalDate.now())
                        .status(CONSULTATION_PENDING)
                        .build();

                consultationOfflineRequestRepo.save(consultationOfflineRequest);

                return ResponseBuilder.build(HttpStatus.CREATED, "", null);

            }

            SchoolConfig schoolConfigData = schoolConfig.get();

            Map<String, Object> operationSettingsData = (Map<String, Object>) schoolConfigData.getValue();

            int maxBookingPerSlot = (int) operationSettingsData.get("maxBookingPerSlot");
            int allowBookingBeforeHour =
                    ((Number) operationSettingsData.get("allowBookingBeforeHours")).intValue();

            LocalDateTime latestAllowedBookingTime =
                    appointmentDateTime.minusHours(allowBookingBeforeHour);

            if (consultationOfflineRequestRepo.countByAppointmentDateAndAppointmentTimeAndCampus(request.getAppointmentDate(), request.getAppointmentTime(), campus.get()) == maxBookingPerSlot) {
                return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Đã quá số lượng đặt lịch tối đa trong một slot", null);
            }

            if (LocalDateTime.now().isAfter(latestAllowedBookingTime)) {
                return ResponseBuilder.build(
                        HttpStatus.BAD_REQUEST,
                        "Phải đặt lịch trước ít nhất " + allowBookingBeforeHour + " giờ",
                        null
                );
            }
        }

        Map<String, Object> policyDetail = (Map<String, Object>) campus.get().getPolicyDetail();

        if (policyDetail != null) {

            int maxBookingPerSlot = (int) policyDetail.get("maxBookingPerSlot");
            int allowBookingBeforeHour =
                    ((Number) policyDetail.get("allowBookingBeforeHours")).intValue();

            if (consultationOfflineRequestRepo.countByAppointmentDateAndAppointmentTimeAndCampus(request.getAppointmentDate(), request.getAppointmentTime(), campus.get()) == maxBookingPerSlot) {
                return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Đã quá số lượng đặt lịch tối đa trong một slot", null);
            }

            LocalDateTime latestAllowedBookingTime =
                    appointmentDateTime.minusHours(allowBookingBeforeHour);

            if (LocalDateTime.now().isAfter(latestAllowedBookingTime)) {
                return ResponseBuilder.build(
                        HttpStatus.BAD_REQUEST,
                        "Phải đặt lịch trước ít nhất " + allowBookingBeforeHour + " giờ",
                        null
                );
            }

        }

        ConsultationOfflineRequest consultationOfflineRequest = ConsultationOfflineRequest.builder()
                .appointmentDate(request.getAppointmentDate())
                .parent(parent.get())
                .phone(request.getPhone().trim())
                .question(request.getQuestion().trim())
                .appointmentTime(request.getAppointmentTime())
                .appointmentDate(request.getAppointmentDate())
                .campus(campus.get())
                .createdDate(LocalDate.now())
                .status(CONSULTATION_PENDING)
                .build();

        consultationOfflineRequestRepo.save(consultationOfflineRequest);

        return ResponseBuilder.build(HttpStatus.CREATED, "", null);

    }

    @Override
    public ResponseEntity<ResponseObject> getConsultationOfflineRequests(String status, int page, int size) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Optional<Parent> parent = parentRepo.findByAccount_Email(email);

        if (parent.isEmpty()) {
            return ResponseBuilder.build(HttpStatus.UNAUTHORIZED, "Tài khoản phụ huynh không tồn tại", null);
        }

        Status parsedStatus;

        try {
            parsedStatus = fromConsultationValue(status);
        } catch (IllegalArgumentException e) {
            return ResponseBuilder.build(
                    HttpStatus.BAD_REQUEST,
                    e.getMessage(),
                    null
            );
        }

        PageResponse<Map<String, Object>> pageResponse;

        try {

            if (parsedStatus == null) {

                Pageable pageable = PaginationUtil.buildPageRequest(page, size);

                Page<ConsultationOfflineRequest> consultationOfflineRequests = consultationOfflineRequestRepo
                        .findAllWithTimelineSort(parent.get(), pageable);

                pageResponse = PaginationUtil.buildPageResponse(consultationOfflineRequests, this::buildConsultationOfflineRequest);

            } else {

                Sort sort = buildConsultationSort(parsedStatus);

                Pageable pageable = PaginationUtil.buildPageRequest(page, size, sort);

                Page<ConsultationOfflineRequest> consultationOfflineRequestsWithStatus = consultationOfflineRequestRepo.findAllByParentAndStatus(parent.get(), parsedStatus, pageable);

                pageResponse = PaginationUtil.buildPageResponse(consultationOfflineRequestsWithStatus, this::buildConsultationOfflineRequest);

            }
        } catch (Exception e) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, e.getMessage(), null);
        }

        return ResponseBuilder.build(
                HttpStatus.OK,
                "Lấy danh sách lịch tư vấn thành công",
                pageResponse
        );
    }

    private Sort buildConsultationSort(Status status) {

        boolean isUpcomingStatus = List.of(
                Status.CONSULTATION_PENDING,
                Status.CONSULTATION_CONFIRMED
        ).contains(status);

        if (isUpcomingStatus) {
            return Sort.by(
                    Sort.Order.asc("appointmentDate"),
                    Sort.Order.asc("appointmentTime")
            );
        }

        return Sort.by(
                Sort.Order.desc("appointmentDate"),
                Sort.Order.desc("appointmentTime")
        );
    }

    private Map<String, Object> buildConsultationOfflineRequest(ConsultationOfflineRequest consultationOfflineRequest) {
        Map<String, Object> result = new HashMap<>();

        result.put("id", consultationOfflineRequest.getId());
        result.put("phone", consultationOfflineRequest.getPhone());
        result.put("question", consultationOfflineRequest.getQuestion());
        result.put("appointmentDate", consultationOfflineRequest.getAppointmentDate());
        result.put("appointmentTime", consultationOfflineRequest.getAppointmentTime());
        result.put("status", consultationOfflineRequest.getStatus().getValue());

        return result;
    }

    private Set<Status> consultationStatuses() {
        return Set.of(
                CONSULTATION_PENDING,
                CONSULTATION_CONFIRMED,
                CONSULTATION_IN_PROGRESS,
                CONSULTATION_COMPLETED,
                CONSULTATION_CANCELLED,
                CONSULTATION_NO_SHOW
        );
    }

    private Status fromConsultationValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalizedValue = value.trim().toLowerCase();

        return consultationStatuses()
                .stream()
                .filter(status -> status.getValue().equalsIgnoreCase(normalizedValue))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Trạng thái lịch tư vấn cung cấp không hợp lệ"));
    }

}
