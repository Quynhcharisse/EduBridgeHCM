package com.sp26se041.edubridgehcm.services.implementors;

import com.sp26se041.edubridgehcm.enums.Gender;
import com.sp26se041.edubridgehcm.enums.GradeLevel;
import com.sp26se041.edubridgehcm.enums.NotificationEventType;
import com.sp26se041.edubridgehcm.enums.Role;
import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.enums.SubjectType;
import com.sp26se041.edubridgehcm.models.Account;
import com.sp26se041.edubridgehcm.models.AdmissionCampaign;
import com.sp26se041.edubridgehcm.models.AdmissionReservationForm;
import com.sp26se041.edubridgehcm.models.Campus;
import com.sp26se041.edubridgehcm.models.CampusProgramOffering;
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
import com.sp26se041.edubridgehcm.repositories.AdmissionReservationFormRepo;
import com.sp26se041.edubridgehcm.repositories.CampusProgramOfferingRepo;
import com.sp26se041.edubridgehcm.repositories.CampusRepo;
import com.sp26se041.edubridgehcm.repositories.CampusScheduleTemplateRepo;
import com.sp26se041.edubridgehcm.repositories.ChatMessageRepo;
import com.sp26se041.edubridgehcm.repositories.ConsultationOfflineRequestRepo;
import com.sp26se041.edubridgehcm.repositories.ConversationRepo;
import com.sp26se041.edubridgehcm.repositories.CounsellorRepo;
import com.sp26se041.edubridgehcm.repositories.CounsellorSlotRepo;
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
import com.sp26se041.edubridgehcm.requests.CancelAdmissionFormRequest;
import com.sp26se041.edubridgehcm.requests.CreateAdmissionReservationFormRequest;
import com.sp26se041.edubridgehcm.requests.CreateConsultationOfflineRequest;
import com.sp26se041.edubridgehcm.requests.CreateConversationRequest;
import com.sp26se041.edubridgehcm.requests.UpdateStudentInfoRequest;
import com.sp26se041.edubridgehcm.responses.PageResponse;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.NotificationService;
import com.sp26se041.edubridgehcm.services.ParentService;
import com.sp26se041.edubridgehcm.utils.AccountRestrictionUtil;
import com.sp26se041.edubridgehcm.utils.AuthRequestUtil;
import com.sp26se041.edubridgehcm.utils.PaginationUtil;
import com.sp26se041.edubridgehcm.utils.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
import java.util.stream.Stream;

import static com.sp26se041.edubridgehcm.enums.Status.CONSULTATION_CANCELLED;
import static com.sp26se041.edubridgehcm.enums.Status.CONSULTATION_COMPLETED;
import static com.sp26se041.edubridgehcm.enums.Status.CONSULTATION_CONFIRMED;
import static com.sp26se041.edubridgehcm.enums.Status.CONSULTATION_IN_PROGRESS;
import static com.sp26se041.edubridgehcm.enums.Status.CONSULTATION_NO_SHOW;
import static com.sp26se041.edubridgehcm.enums.Status.CONSULTATION_PENDING;
import static com.sp26se041.edubridgehcm.enums.Status.RESERVATION_CANCELLED;

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

    private final CounsellorSlotRepo counsellorSlotRepo;

    private final NotificationService notificationService;

    private final RestTemplate restTemplate = new RestTemplate();

    private final CampusScheduleTemplateRepo campusScheduleTemplateRepo;

    private final ConsultationOfflineRequestRepo consultationOfflineRequestRepo;

    private final AdmissionCampaignRepo admissionCampaignRepo;

    private final SchoolConfigRepo schoolConfigRepo;

    private final CampusProgramOfferingRepo campusProgramOfferingRepo;
    private final AdmissionReservationFormRepo admissionReservationFormRepo;

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

        if (studentProfile.isEmpty()) {
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
                .toList();
        ;

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

        if (studentProfile.isEmpty()) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Thông tin trẻ không tồn tại trong hệ thốngd", null);
        }

        return ResponseBuilder.build(HttpStatus.OK, "", buildStudentProfile(studentProfile.get()));
    }

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

        try {
            List<Account> schoolAccounts = accountRepo.findByCampus_School_IdAndRole(school.getId(), Role.SCHOOL);
            if (!schoolAccounts.isEmpty()) {
                String displayName = !parent.getName().isBlank()
                        ? parent.getName()
                        : parent.getAccount().getEmail();

                Map<String, Object> context = new HashMap<>();
                context.put("actorName", displayName);
                context.put("specificRecipients", schoolAccounts);

                notificationService.publish(
                        NotificationEventType.FAVORITE_SCHOOL,
                        parent.getAccount(),
                        context
                );
            }
        } catch (Exception e) {
            System.err.println("[FAVORITE_SCHOOL notification] Lỗi: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }

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

        FavouriteSchool favourite = favouriteSchoolRepo.findById(favouriteSchoolId).orElse(null);

        if (favourite == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Lỗi: Không tìm thấy dữ liệu", null);
        }

        // Lưu lại thông tin trước khi xoá
        School school = favourite.getSchool();
        Parent parent = favourite.getParent();

        favouriteSchoolRepo.deleteAllByIdInBatch(List.of(favouriteSchoolId));
        favouriteSchoolRepo.flush();

        try {
            if (school != null && parent != null) {
                List<Account> schoolAccounts = accountRepo.findByCampus_School_IdAndRole(
                        school.getId(), Role.SCHOOL);
                if (!schoolAccounts.isEmpty()) {
                    String name = parent.getName();
                    String displayName = (name != null && !name.isBlank())
                            ? name
                            : (parent.getAccount() != null ? parent.getAccount().getEmail() : "Một phụ huynh");

                    Map<String, Object> context = new HashMap<>();
                    context.put("actorName", displayName);
                    context.put("specificRecipients", schoolAccounts);
                    notificationService.publish(
                            NotificationEventType.REMOVE_FAVORITE_SCHOOL,
                            parent.getAccount(),
                            context);
                }
            }
        } catch (Exception e) {
            System.err.println("[REMOVE_FAVORITE_SCHOOL notification] Lỗi: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }

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
            return "Tên học sinh " + request.getStudentName().trim() + " đã tồn tại";
        }


        if (isBlank(request.getGender())) {
            return "Giới tính là bắt buộc";
        }
        if (parseGender(request.getGender()) == null) {
            return "Giới tính không hợp lệ";
        }


        if (!isBlank(request.getPersonalityTypeCode())) {

            Optional<PersonalityType> personalityType = personalityTypeRepo.findByCode(request.getPersonalityTypeCode());

            if (personalityType.isEmpty()) {
                return "Loại tính cách không hợp lệ";
            }

        }
        if (!isBlank(request.getFavouriteJob())) {

            Optional<Major> major = majorRepo.findByName(request.getFavouriteJob());

            if (major.isEmpty()) {
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
                    map.put("campusName", campus.get().getName());
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

        List<Subject> allSubjects = subjectRepo.findAllByTypeIn(List.of(SubjectType.REGULAR_SUBJECT, SubjectType.FOREIGN_LANGUAGE_SUBJECT));

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

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

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

        List<ConsultationOfflineRequest> allRequests =
                consultationOfflineRequestRepo
                        .findByCampusIdAndAppointmentDateBetween(
                                campus.getId(),
                                startDate,
                                endDate
                        );

        List<ConsultationOfflineRequest> parentRequests = new ArrayList<>();

        if (email != null) {

            Optional<Parent> parent = parentRepo.findByAccount_Email(email);

            if (parent.isPresent()) {
                parentRequests =
                        consultationOfflineRequestRepo
                                .findByParentIdAndCampusIdAndAppointmentDateBetween(
                                        parent.get().getId(),
                                        campus.getId(),
                                        startDate,
                                        endDate
                                );
            }
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

            ConsultationOfflineRequest parentRequest = parentRequests.stream()
                    .filter(r ->
                            r.getAppointmentDate().equals(slotDate)
                                    && r.getAppointmentTime().equals(campusScheduleTemplate.getStartTime())
                    )
                    .findFirst()
                    .orElse(null);

            Status status = getSlotStatus(
                    slotDate,
                    campusScheduleTemplate.getStartTime(),
                    campusScheduleTemplate.getEndTime()
            );

            long totalRequests = allRequests.stream()
                    .filter(r ->
                            r.getAppointmentDate().equals(slotDate)
                                    && r.getAppointmentTime().equals(campusScheduleTemplate.getStartTime())
                                    && r.getStatus() != Status.CONSULTATION_CANCELLED
                    )
                    .count();

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
            slotMap.put("totalRequests", totalRequests);
            if (parentRequest != null) {
                Map<String, Object> requestMap = new HashMap<>();
                requestMap.put("id", parentRequest.getId());
                requestMap.put("status", parentRequest.getStatus());

                slotMap.put("consultationOfflineRequest", requestMap);
            } else {
                slotMap.put("consultationOfflineRequest", null);
            }

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

        Parent parent = parentRepo.findByAccount_Email(email)
                .orElse(null);

        if (parent == null) {
            return ResponseBuilder.build(HttpStatus.UNAUTHORIZED, "Tài khoản phụ huynh không tồn tại", null);
        }

        if (AccountRestrictionUtil.isRestricted(parent.getAccount())) {
            return ResponseBuilder.build(HttpStatus.UNAUTHORIZED, "Tài khoản của bạn hiện tại đang bị cấm", null);
        }

        Campus campus = campusRepo.findById(request.getCampusId()).orElse(null);

        if (campus == null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Cơ sở không tồn tại", null);
        }

        // ===== VALIDATE INPUT =====
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

        if (request.getAppointmentDate() == null || request.getAppointmentTime() == null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Ngày giờ hẹn không được để trống", null);
        }

        LocalDateTime appointmentDateTime = LocalDateTime.of(
                request.getAppointmentDate(),
                request.getAppointmentTime()
        );

        if (appointmentDateTime.isBefore(LocalDateTime.now())) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Thời gian hẹn không được ở quá khứ", null);
        }

        // ===== RULE: 1 PARENT / 1 DAY =====
        boolean hasActive = consultationOfflineRequestRepo
                .existsByParentAndCampusAndAppointmentDateAndStatusIn(
                        parent,
                        campus,
                        request.getAppointmentDate(),
                        List.of(
                                Status.CONSULTATION_PENDING,
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

        // ===== LẤY POLICY (gộp 1 lần) =====
        int maxBookingPerSlot = Integer.MAX_VALUE;
        int allowBookingBeforeHour = 0;

        Map<String, Object> policyDetail = (Map<String, Object>) campus.getPolicyDetail();

        if (policyDetail != null) {
            maxBookingPerSlot = (int) policyDetail.get("maxBookingPerSlot");
            allowBookingBeforeHour = ((Number) policyDetail.get("allowBookingBeforeHours")).intValue();
        } else {
            Optional<SchoolConfig> schoolConfig = schoolConfigRepo
                    .findBySchoolIdAndKey(campus.getSchool().getId(), "operationSettingsData");

            if (schoolConfig.isPresent()) {
                Map<String, Object> data = (Map<String, Object>) schoolConfig.get().getValue();
                maxBookingPerSlot = (int) data.get("maxBookingPerSlot");
                allowBookingBeforeHour = ((Number) data.get("allowBookingBeforeHours")).intValue();
            }
        }

        long currentBooking = consultationOfflineRequestRepo
                .countByAppointmentDateAndAppointmentTimeAndCampusAndStatusIn(
                        request.getAppointmentDate(),
                        request.getAppointmentTime(),
                        campus,
                        List.of(
                                Status.CONSULTATION_PENDING,
                                Status.CONSULTATION_CONFIRMED
                        )
                );

        if (currentBooking >= maxBookingPerSlot) {
            return ResponseBuilder.build(
                    HttpStatus.BAD_REQUEST,
                    "Đã quá số lượng đặt lịch tối đa trong một slot",
                    null
            );
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

        // ===== SAVE =====
        ConsultationOfflineRequest entity = ConsultationOfflineRequest.builder()
                .appointmentDate(request.getAppointmentDate())
                .appointmentTime(request.getAppointmentTime())
                .parent(parent)
                .campus(campus)
                .phone(request.getPhone().trim())
                .question(request.getQuestion().trim())
                .createdDate(LocalDate.now())
                .status(Status.CONSULTATION_PENDING)
                .build();

        consultationOfflineRequestRepo.save(entity);

        // ── Notify school accounts của campus ─────────────────────────────────
        try {
            List<Account> schoolAccounts = accountRepo.findByCampus_School_IdAndRole(
                    campus.getSchool().getId(), Role.SCHOOL);
            if (!schoolAccounts.isEmpty()) {
                String displayName = (parent.getName() != null && !parent.getName().isBlank())
                        ? parent.getName() : parent.getAccount().getEmail();
                Map<String, Object> context = new HashMap<>();
                context.put("actorName", displayName);
                context.put("appointmentDate", request.getAppointmentDate().toString());
                context.put("specificRecipients", schoolAccounts);
                notificationService.publish(NotificationEventType.CONSULTATION_BOOKED,
                        parent.getAccount(), context);
            }
        } catch (Exception ignored) {
            // notification lỗi không block nghiệp vụ chính
        }

        return ResponseBuilder.build(HttpStatus.CREATED, "Đặt lịch thành công", null);
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

    @Override
    public ResponseEntity<ResponseObject> createAdmissionReservationForm(CreateAdmissionReservationFormRequest request) {

        Optional<StudentProfile> studentProfile = studentInfoRepo.findById(request.getStudentProfileId());

        if (studentProfile.isEmpty()){
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Không tìm thấy hồ sơ học sinh, vui lòng kiểm tra lại.", null);
        }


        if (request.getSubmissionDocuments() == null || request.getSubmissionDocuments().isEmpty()) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Vui lòng cung cấp hồ sơ tài liệu trước khi nộp.", null);
        }

        Optional<CampusProgramOffering> campusProgramOffering = campusProgramOfferingRepo.findById(request.getCampusProgramOfferingId());

        if (campusProgramOffering.isEmpty()) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Không tìm thấy gói chương trình tuyển sinh", null);
        }

        if (campusProgramOffering.get().getStatus().equals(Status.OFFERING_INACTIVE)){
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Gói tuyển sinh đã ngừng hoạt động, không thể nộp hồ sơ.", null);
        }

        if (campusProgramOffering.get().getApplicationStatus().equals(Status.UPCOMING_OFFERING)) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Thời gian nộp đơn gói chương trình tuyển sinh chưa mở, vui lòng quay lại sau.", null);
        }

        if (campusProgramOffering.get().getApplicationStatus().equals(Status.CLOSED)) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Thời hạn nộp đơn gói chương trình tuyển sinh đã kết thúc, không thể nộp hồ sơ.", null);
        }

        boolean alreadyPending = admissionReservationFormRepo.existsByCampusProgramOfferingAndStudentProfileAndStatus(
                campusProgramOffering.get(),
                studentProfile.get(),
                Status.RESERVATION_PENDING
        );

        if (alreadyPending) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Bạn đã có hồ sơ bảo lưu đang chờ xét duyệt, vui lòng chờ nhà trường phản hồi trước khi nộp lại.", null);
        }

        boolean alreadyApproved = admissionReservationFormRepo
                .existsByCampusProgramOfferingAndStudentProfileAndStatus(
                        campusProgramOffering.get(),
                        studentProfile.get(),
                        Status.RESERVATION_APPROVAL
                );

        if (alreadyApproved) {
            return ResponseBuilder.build(
                    HttpStatus.BAD_REQUEST,
                    "Hồ sơ này đã được duyệt giữ chỗ thành công, không thể tạo thêm đơn mới.",
                    null
            );
        }

        List<Map<String, Object>> admissionMethodTimelines = (List<Map<String, Object>>) campusProgramOffering.get().getAdmissionCampaign().getAdmissionMethodTimelines();

        Map<String, Object> matchedMethod = admissionMethodTimelines.stream()
                .filter(method -> Objects.equals(method.get("methodCode"), campusProgramOffering.get().getAdmissionMethod()))
                .findFirst().orElse(null);

        if (matchedMethod == null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Không tìm thấy phương thức tuyên sinh với method code " + campusProgramOffering.get().getAdmissionMethod() + " trong chiến dịch tuyển sinh", null);
        }

        boolean allowReservationSubmission = (boolean) matchedMethod.get("allowReservationSubmission");

        if (!allowReservationSubmission) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Phương thức tuyển sinh này hiện không hỗ trợ nộp hồ sơ bảo lưu. Vui lòng chọn phương thức khác.", null);
        }

        int remainingQuota = campusProgramOffering.get().getRemainingQuota();

        if (remainingQuota <= 0) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Phương thức tuyển sinh này đã đủ chỉ tiêu, không thể nộp thêm hồ sơ bảo lưu.", null);
        }

        Optional<SchoolConfig> documentRequirementsData = schoolConfigRepo.findBySchoolIdAndKey(
                campusProgramOffering.get().getCampus().getSchool().getId(),
                "documentRequirementsData"
        );

        if (documentRequirementsData.isEmpty()) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Trường chưa cấu hình danh sách hồ sơ yêu cầu, vui lòng liên hệ nhà trường để được hỗ trợ.", null);
        }

        Map<String, Object> docConfig = (Map<String, Object>) documentRequirementsData.get().getValue();

        List<Map<String, Object>> mandatoryAll = (List<Map<String, Object>>) docConfig.get("mandatoryAll");

        List<Map<String, Object>> byMethod = (List<Map<String, Object>>) docConfig.get("byMethod");

        List<Map<String, Object>> methodDocs = byMethod.stream()
                .filter(m -> Objects.equals(m.get("methodCode"), campusProgramOffering.get().getAdmissionMethod()))
                .map(m -> (List<Map<String, Object>>) m.get("documents"))
                .findFirst()
                .orElse(List.of());

        List<String> requiredCodes = Stream.concat(mandatoryAll.stream(), methodDocs.stream())
                .filter(doc -> Boolean.TRUE.equals(doc.get("required")))
                .map(doc -> (String) doc.get("code"))
                .toList();

        List<String> submittedCodes = request.getSubmissionDocuments().stream()
                .map(doc ->  doc.getKey())
                .toList();

        // Kiểm tra thiếu document nào
        List<String> missingCodes = requiredCodes.stream()
                .filter(code -> !submittedCodes.contains(code))
                .toList();

        if (!missingCodes.isEmpty()) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST,
                    "Hồ sơ còn thiếu các giấy tờ bắt buộc: " + String.join(", ", missingCodes), null);
        }

        // Chỉ check imageUrl với các document required
        List<String> emptyImageDocs = request.getSubmissionDocuments().stream()
                .filter(doc -> requiredCodes.contains(doc.getKey()))
                .filter(doc -> doc.getImageUrl() == null || doc.getImageUrl().isEmpty())
                .map(CreateAdmissionReservationFormRequest.SubmissionDocument::getKey)
                .toList();

        if (!emptyImageDocs.isEmpty()) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST,
                    "Các giấy tờ bắt buộc sau chưa có hình ảnh đính kèm: " + String.join(", ", emptyImageDocs), null);
        }

        List<Map<String, Object>> profileMetaData = request.getSubmissionDocuments().stream()
                .map(doc -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("key", doc.getKey());
                    map.put("imageUrl", doc.getImageUrl());
                    return map;
                })
                .toList();

        admissionReservationFormRepo.save(
                AdmissionReservationForm.builder()
                        .createdTime(LocalDateTime.now())
                        .status(Status.RESERVATION_PENDING)
                        .studentProfile(studentProfile.get())
                        .campusProgramOffering(campusProgramOffering.get())
                        .methodName(campusProgramOffering.get().getAdmissionMethod())
                        .profileMetadata(profileMetaData)
                        .updatedTime(LocalDateTime.now())
                        .build()
        );

        return ResponseBuilder.build(HttpStatus.OK, "Nộp hồ sơ giữ chỗ thành công, vui lòng chờ nhà trường xét duyệt.", null);

    }

    @Override
    public ResponseEntity<ResponseObject> getDocumentRequirements(int campusProgramOfferingId) {

        Optional<CampusProgramOffering> campusProgramOffering = campusProgramOfferingRepo.findById(campusProgramOfferingId);

        if (campusProgramOffering.isEmpty()) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Không tìm thấy gói chương trình tuyển sinh", null);
        }

        if (campusProgramOffering.isEmpty()) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Không tìm thấy gói tuyển sinh", null);
        }

        if (campusProgramOffering.get().getStatus().equals(Status.OFFERING_INACTIVE)){
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Gói tuyển sinh đã ngừng hoạt động, không thể nộp hồ sơ.", null);
        }

        if (campusProgramOffering.get().getApplicationStatus().equals(Status.UPCOMING_OFFERING)) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Thời gian nộp đơn gói chương trình tuyển sinh chưa mở, vui lòng quay lại sau.", null);
        }

        if (campusProgramOffering.get().getApplicationStatus().equals(Status.CLOSED)) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Thời hạn nộp đơn gói chương trình tuyển sinh đã kết thúc, không thể nộp hồ sơ.", null);
        }


        Optional<SchoolConfig> documentRequirementsData = schoolConfigRepo.findBySchoolIdAndKey(
                campusProgramOffering.get().getCampus().getSchool().getId(),
                "documentRequirementsData"
        );

        if (documentRequirementsData.isEmpty()) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Trường chưa cấu hình danh sách hồ sơ yêu cầu.", null);
        }

        Map<String, Object> docConfig = (Map<String, Object>) documentRequirementsData.get().getValue();

        List<Map<String, Object>> mandatoryAll = (List<Map<String, Object>>) docConfig.get("mandatoryAll");

        List<Map<String, Object>> byMethod = (List<Map<String, Object>>) docConfig.get("byMethod");

        List<Map<String, Object>> methodDocs = byMethod.stream()
                .filter(m -> Objects.equals(m.get("methodCode"), campusProgramOffering.get().getAdmissionMethod()))
                .map(m -> (List<Map<String, Object>>) m.get("documents"))
                .findFirst()
                .orElse(List.of());

        List<Map<String, Object>> allDocs = Stream.concat(mandatoryAll.stream(), methodDocs.stream()).toList();

        List<Map<String, Object>> requiredDocs = allDocs.stream()
                .filter(doc -> Boolean.TRUE.equals(doc.get("required")))
                .toList();

        List<Map<String, Object>> optionalDocs = allDocs.stream()
                .filter(doc -> !Boolean.TRUE.equals(doc.get("required")))
                .toList();

        Map<String, Object> result = Map.of(
                "required", requiredDocs,
                "optional", optionalDocs
        );

        return ResponseBuilder.build(HttpStatus.OK, "Lấy danh sách tài liệu thành công.", result);
    }

    @Override
    public ResponseEntity<ResponseObject> getAdmissionReservationForms(String status) {
        // Lấy parent đang đăng nhập
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Optional<Account> account = accountRepo.findByEmail(email);
        if (account.isEmpty()) {
            return ResponseBuilder.build(HttpStatus.UNAUTHORIZED, "Không tìm thấy tài khoản.", null);
        }

        List<AdmissionReservationForm> forms;

        if (status != null && !status.isBlank()) {
            Status filterStatus;
            try {
                filterStatus = Status.valueOf(status);
            } catch (IllegalArgumentException e) {
                return ResponseBuilder.build(HttpStatus.BAD_REQUEST,
                        "Trạng thái không hợp lệ. Các trạng thái hợp lệ: RESERVATION_PENDING, RESERVATION_APPROVAL, RESERVATION_REJECTED, RESERVATION_CANCELLED.", null);
            }
            forms = admissionReservationFormRepo.findByStudentProfile_Parent_Account_IdAndStatus(account.get().getId(), filterStatus);
        } else {
            forms = admissionReservationFormRepo.findByStudentProfile_Parent_Account_Id(account.get().getId());
        }

        List<Map<String, Object>> result = buildAdmissionReservationForms(forms);

        return ResponseBuilder.build(HttpStatus.OK, "Lấy danh sách đơn thành công.", result);
    }

    @Override
    public ResponseEntity<ResponseObject> cancelAdmissionReservationForm(CancelAdmissionFormRequest request) {

        Optional<AdmissionReservationForm> admissionReservationForm = admissionReservationFormRepo.findById(request.getAdmissionFormId());

        if (admissionReservationForm.isEmpty()) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Không tìm thấy đơn giữ chỗ", null);
        }

        if (request.getCancelReason() == null || request.getCancelReason().trim().isBlank()) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Vui lòng nhập lý do hủy đơn giữ chỗ", null);
        }

        admissionReservationForm.get().setCancelReason(request.getCancelReason().trim());
        admissionReservationForm.get().setStatus(RESERVATION_CANCELLED);

        admissionReservationFormRepo.save(admissionReservationForm.get());

        return ResponseBuilder.build(HttpStatus.OK, "Hủy đơn giữ chỗ thành công", null);
    }


    private List<Map<String, Object>> buildAdmissionReservationForms(List<AdmissionReservationForm> admissionReservationForms) {
        return admissionReservationForms.stream()
                .map(this::buildAdmissionReservationForm)
                .toList();
    }

    private Map<String, Object> buildAdmissionReservationForm(AdmissionReservationForm form) {
        Map<String, Object> map = new HashMap<>();

        map.put("id", form.getId());
        map.put("status", form.getStatus());
        map.put("methodName", form.getMethodName());
        map.put("profileMetadata", form.getProfileMetadata());
        map.put("createdTime", form.getCreatedTime());
        map.put("updatedTime", form.getUpdatedTime());
        map.put("cancelReason", form.getCancelReason());
        map.put("rejectReason", form.getRejectReason());
        map.put("verifiedBy", form.getVerifiedBy());

        // CampusProgramOffering info
        CampusProgramOffering offering = form.getCampusProgramOffering();
        map.put("campusProgramOfferingId", offering.getId());
        map.put("admissionMethodCode", offering.getAdmissionMethod());
        map.put("programName", offering.getProgram().getName());
        map.put("campusName", offering.getCampus().getName());
        map.put("schoolName", offering.getCampus().getSchool().getName());


        // StudentProfile info
        StudentProfile student = form.getStudentProfile();
        map.put("studentProfileId", student.getId());
        map.put("studentName", student.getStudentName());
        map.put("gender", student.getGender());

        Parent parent = student.getParent();
        map.put("parentProfileId", parent.getId());
        map.put("parentName", parent.getName());
        map.put("parentPhone", parent.getPhone());
        map.put("parentEmail", parent.getAccount().getEmail());
        map.put("identityCard", parent.getIdCardNumber());
        map.put("address", parent.getCurrentAddress());

        return map;
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
        result.put("schoolName", consultationOfflineRequest.getCampus().getSchool().getName());
        result.put("campusName", consultationOfflineRequest.getCampus().getName());
        result.put("address", consultationOfflineRequest.getCampus().getAddress());
        result.put("status", consultationOfflineRequest.getStatus().getValue());

        if (consultationOfflineRequest.getNote() == null) {
            result.put("note", null);
        } else {
            result.put("note", consultationOfflineRequest.getNote().trim());
        }

        if (consultationOfflineRequest.getCancelReason() == null) {
            result.put("cancelReason", null);
        } else {
            result.put("cancelReason", consultationOfflineRequest.getCancelReason());
        }

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
