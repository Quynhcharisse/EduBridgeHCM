package com.sp26se041.edubridgehcm.services.implementors;

import com.sp26se041.edubridgehcm.enums.ConsultationAction;
import com.sp26se041.edubridgehcm.enums.NotificationEventType;
import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.models.Account;
import com.sp26se041.edubridgehcm.models.AdmissionCampaign;
import com.sp26se041.edubridgehcm.models.CampusScheduleTemplate;
import com.sp26se041.edubridgehcm.models.ChatMessage;
import com.sp26se041.edubridgehcm.models.ConsultationOfflineRequest;
import com.sp26se041.edubridgehcm.models.Conversation;
import com.sp26se041.edubridgehcm.models.Counsellor;
import com.sp26se041.edubridgehcm.models.CounsellorSlot;
import com.sp26se041.edubridgehcm.models.Parent;
import com.sp26se041.edubridgehcm.models.SchoolConfig;
import com.sp26se041.edubridgehcm.models.SchoolSubscription;
import com.sp26se041.edubridgehcm.models.StudentProfile;
import com.sp26se041.edubridgehcm.repositories.AccountRepo;
import com.sp26se041.edubridgehcm.repositories.AdmissionCampaignRepo;
import com.sp26se041.edubridgehcm.repositories.CampusScheduleTemplateRepo;
import com.sp26se041.edubridgehcm.repositories.ChatMessageRepo;
import com.sp26se041.edubridgehcm.repositories.ConsultationOfflineRequestRepo;
import com.sp26se041.edubridgehcm.repositories.ConversationRepo;
import com.sp26se041.edubridgehcm.repositories.CounsellorRepo;
import com.sp26se041.edubridgehcm.repositories.CounsellorSlotRepo;
import com.sp26se041.edubridgehcm.repositories.SchoolConfigRepo;
import com.sp26se041.edubridgehcm.repositories.SchoolSubscriptionRepo;
import com.sp26se041.edubridgehcm.repositories.StudentInfoRepo;
import com.sp26se041.edubridgehcm.requests.ChatMessageForChatBot;
import com.sp26se041.edubridgehcm.requests.UpdateConsultationOfflineRequest;
import com.sp26se041.edubridgehcm.responses.PageResponse;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.CounsellorService;
import com.sp26se041.edubridgehcm.services.NotificationService;
import com.sp26se041.edubridgehcm.utils.AccountRestrictionUtil;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;


import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static com.sp26se041.edubridgehcm.enums.Status.CONSULTATION_CANCELLED;
import static com.sp26se041.edubridgehcm.enums.Status.CONSULTATION_COMPLETED;
import static com.sp26se041.edubridgehcm.enums.Status.CONSULTATION_CONFIRMED;
import static com.sp26se041.edubridgehcm.enums.Status.CONSULTATION_IN_PROGRESS;
import static com.sp26se041.edubridgehcm.enums.Status.CONSULTATION_NO_SHOW;
import static com.sp26se041.edubridgehcm.enums.Status.CONSULTATION_PENDING;

@Service
@RequiredArgsConstructor
public class CounsellorServiceImpl implements CounsellorService {

    private final ConversationRepo conversationRepo;

    private final ChatMessageRepo chatMessageRepo;

    private final AccountRepo accountRepo;

    private final StudentInfoRepo studentInfoRepo;

    private final CounsellorRepo counsellorRepo;

    private final CounsellorSlotRepo counsellorSlotRepo;

    private final ConsultationOfflineRequestRepo consultationOfflineRequestRepo;

    private final CampusScheduleTemplateRepo campusScheduleTemplateRepo;

    private final AdmissionCampaignRepo admissionCampaignRepo;

    private final SchoolConfigRepo schoolConfigRepo;

    private final NotificationService notificationService;

    private final RestTemplate restTemplate = new RestTemplate();

    private final SchoolSubscriptionRepo schoolSubscriptionRepo;

    @Override
    public ResponseEntity<ResponseObject> chatWithChatbotForSchool(ChatMessageForChatBot messageForChatBot) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Optional<Account> account = accountRepo.findByEmail(email);

        if (account.isEmpty()) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Tài khoản không tồn tại", null);
        }

        Counsellor counsellor = counsellorRepo.findByAccountId(account.get().getId());

        if (counsellor == null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Tư vấn viên không tồn tại", null);
        }


        Optional<SchoolSubscription> activeSubOpt = schoolSubscriptionRepo.findBySchoolIdAndIsSelected(counsellor.getCampus().getSchool().getId(), true).stream().findFirst(); // tại 1 thời điểm chỉ có 1 gói đc active

        if (activeSubOpt.isEmpty()) {
            return ResponseBuilder.build(
                    HttpStatus.BAD_REQUEST,
                    "Trường chưa có gói dịch vụ đang hoạt động",
                    null
            );
        }

        // 1. Lấy thông tin Features từ gói cước
        Map<String, Object> features = (Map<String, Object>) activeSubOpt.get().getSubscription().getFeatures();

        boolean hasAiAssistant = (boolean) features.get("hasAiAssistant");

        if (!hasAiAssistant) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Gói dịch vụ hiện tại không hỗ trợ tính năng chatbot AI", null);
        }

        Map<String, Object> payload = new LinkedHashMap<>();

        payload.put("chatInput", messageForChatBot.getChatInput());
        payload.put("schoolId",  counsellor.getCampus().getSchool().getId());
        payload.put("sessionId", email);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    "https://n8n-service-ijbl.onrender.com/webhook/chatbot-ai-for-school",
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

            Object parsed = objectMapper.readValue(rawBody, Object.class);

            Map<String, Object> responseBody;

            if (parsed instanceof List) {
                // n8n trả array → lấy phần tử đầu tiên
                List<Map<String, Object>> list = (List<Map<String, Object>>) parsed;
                responseBody = list.isEmpty() ? new LinkedHashMap<>() : list.get(0);
            } else {
                responseBody = (Map<String, Object>) parsed;
            }

            return ResponseBuilder.build(HttpStatus.OK, "Thành công", responseBody);

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
    public ResponseEntity<ResponseObject> getCounsellorCalendar(LocalDate startDate, LocalDate endDate) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        if (startDate.getDayOfWeek() != DayOfWeek.MONDAY) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "startDate phải là Thứ 2", null);
        }

        if (endDate.getDayOfWeek() != DayOfWeek.SUNDAY) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "endDate phải là Chủ nhật", null);
        }

        if (endDate.isBefore(startDate)) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "endDate phải > startDate", null);
        }

        if (!startDate.plusDays(6).equals(endDate)) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Phải chọn đúng 1 tuần (7 ngày)", null);
        }

        Counsellor counsellor = counsellorRepo
                .findByAccountId(accountRepo.findByEmail(email).get().getId());

        List<CounsellorSlot> counsellorSlotList =
                counsellorSlotRepo.findByCounsellorIdAndStartDateLessThanEqualAndEndDateGreaterThanEqualAndStatus(
                        counsellor.getId(),
                        endDate,
                        startDate,
                        Status.AVAILABLE
                );

        List<Map<String, Object>> result = new ArrayList<>();

        for (CounsellorSlot counsellorSlot : counsellorSlotList) {

            CampusScheduleTemplate scheduleTemplate = counsellorSlot.getCampusScheduleTemplate();

            LocalDate slotDate = getDateByDayOfWeek(
                    startDate,
                    endDate,
                    scheduleTemplate.getDayOfWeek()
            );

            if (slotDate == null) continue;

            Status status = getSlotStatus(
                    slotDate,
                    scheduleTemplate.getStartTime(),
                    scheduleTemplate.getEndTime()
            );

            List<ConsultationOfflineRequest> requests =
                    consultationOfflineRequestRepo.findByCounsellorSlot_CounsellorAndAppointmentDateAndAppointmentTime(
                    counsellor,
                    slotDate,
                    scheduleTemplate.getStartTime()
            );

            List<Map<String, Object>> consultationOfflineRequests = requests.stream()
                    .map(this::buildConsultationOfflineRequest)
                    .toList();

            Map<String, Object> slotMap = new HashMap<>();

            slotMap.put("counsellorSlotId", counsellorSlot.getId());
            slotMap.put("counsellorId", counsellor.getId());
            slotMap.put("campusScheduleTemplateId", scheduleTemplate.getId());
            slotMap.put("date", slotDate);
            slotMap.put("dayOfWeek", scheduleTemplate.getDayOfWeek());
            slotMap.put("startTime", scheduleTemplate.getStartTime());
            slotMap.put("endTime", scheduleTemplate.getEndTime());
            slotMap.put("status", status);           // UPCOMING / ONGOING / PAST
            slotMap.put("statusLabel", status.getValue());  // Tiếng Việt

            slotMap.put("consultationOfflineRequest", consultationOfflineRequests);

            result.add(slotMap);
        }

        result.sort(
                Comparator
                        .comparing((Map<String, Object> m) -> (LocalDate) m.get("date"))
                        .thenComparing(m -> (LocalTime) m.get("startTime"))
        );

        return ResponseBuilder.build(HttpStatus.OK, "", result);
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

    @Override
    public ResponseEntity<ResponseObject> getConsultationOfflineRequests(String status, int page, int size) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Counsellor counsellor = counsellorRepo.findByAccountId(accountRepo.findByEmail(email).get().getId());

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

        Sort sort = buildConsultationSort(parsedStatus);

        Pageable pageable = PaginationUtil.buildPageRequest(page, size, sort);

        Page<ConsultationOfflineRequest> consultationOfflineRequests = consultationOfflineRequestRepo.findAllByCampusAndStatus(counsellor.getCampus(), parsedStatus, pageable);

        PageResponse<Map<String, Object>> pageResponse = PaginationUtil.buildPageResponse(consultationOfflineRequests, this::buildConsultationOfflineRequest);

        return ResponseBuilder.build(
                HttpStatus.OK,
                "Lấy danh sách lịch tư vấn thành công",
                pageResponse
        );
    }



    @Override
    @Transactional
    public ResponseEntity<ResponseObject> updateConsultationOfflineRequest(UpdateConsultationOfflineRequest request) {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Tài khoản của bạn đang bị hạn chế, không thể thực hiện thao tác này.", null);
        }

        Optional<ConsultationOfflineRequest> consultationOfflineRequest =  consultationOfflineRequestRepo.findById(request.getId());

        if (consultationOfflineRequest.isEmpty()) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Lịch hẹn không tồn tại", null);
        }

        ConsultationAction consultationAction;

        try {
            consultationAction = parseConsultationAction(request.getAction());
        } catch (IllegalArgumentException e) {
            return ResponseBuilder.build(
                    HttpStatus.BAD_REQUEST,
                    e.getMessage(),
                    null
            );
        }

        ConsultationOfflineRequest offlineRequest = consultationOfflineRequest.get();

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Optional<Account> currentAccount = accountRepo.findByEmail(email);

        if (currentAccount.isEmpty()) {
            return ResponseBuilder.build(
                    HttpStatus.BAD_REQUEST,
                    "Không tìm thấy tài khoản",
                    null
            );
        }

        Counsellor currentCounsellor = counsellorRepo.findByAccountId(currentAccount.get().getId());

        if (currentCounsellor == null) {
            return ResponseBuilder.build(
                    HttpStatus.BAD_REQUEST,
                    "Không tìm thấy thông tin tư vấn viên",
                    null
            );
        }

        Status currentStatus = offlineRequest.getStatus();

        switch (consultationAction) {

            case CONFIRMING -> {

                if (currentStatus != Status.CONSULTATION_PENDING) {
                    return ResponseBuilder.build(
                            HttpStatus.BAD_REQUEST,
                            "Chỉ có thể tiếp nhận lịch hẹn đang chờ xác nhận",
                            null
                    );
                }

                boolean lockedByOther = offlineRequest.getCounsellor() != null
                        && offlineRequest.getLockedUntil() != null
                        && offlineRequest.getLockedUntil().isAfter(LocalDateTime.now())
                        && !offlineRequest.getCounsellor().getId().equals(currentCounsellor.getId());

                if (lockedByOther) {
                    return ResponseBuilder.build(
                            HttpStatus.BAD_REQUEST,
                            "Lịch hẹn đang được xử lý bởi: "
                                    + offlineRequest.getCounsellor().getName(),
                            null
                    );
                }

                offlineRequest.setCounsellor(currentCounsellor);
                offlineRequest.setLockedUntil(LocalDateTime.now().plusMinutes(15));

            }

            case CONFIRM -> {

                LocalDateTime now = LocalDateTime.now();

                boolean isOwner = offlineRequest.getCounsellor() != null
                        && offlineRequest.getLockedUntil() != null
                        && offlineRequest.getLockedUntil().isAfter(now)
                        && offlineRequest.getCounsellor().getId().equals(currentCounsellor.getId());

                if (!isOwner) {
                    return ResponseBuilder.build(
                            HttpStatus.BAD_REQUEST,
                            "Bạn không phải người đang tiếp nhận lịch hẹn này",
                            null
                    );
                }

                if (currentStatus != Status.CONSULTATION_PENDING) {
                    return ResponseBuilder.build(
                            HttpStatus.BAD_REQUEST,
                            "Chỉ có thể xác nhận lịch hẹn đang chờ xác nhận",
                            null
                    );
                }

                Optional<CounsellorSlot> counsellorSlotOpt =
                        counsellorSlotRepo.findById(request.getCounsellorSlotId());

                if (counsellorSlotOpt.isEmpty()) {
                    return ResponseBuilder.build(
                            HttpStatus.BAD_REQUEST,
                            "Slot tư vấn không tồn tại",
                            null
                    );
                }

                // ===== VALIDATE DATE =====
                if (request.getAppointmentDate() == null) {
                    return ResponseBuilder.build(
                            HttpStatus.BAD_REQUEST,
                            "Ngày hẹn không được để trống",
                            null
                    );
                }

                LocalDate slotDate = request.getAppointmentDate();
                LocalTime slotTime = counsellorSlotOpt.get()
                        .getCampusScheduleTemplate()
                        .getStartTime();

                String slotDay = counsellorSlotOpt.get()
                        .getCampusScheduleTemplate()
                        .getDayOfWeek(); // MON

                String requestDay = slotDate.getDayOfWeek().name().substring(0, 3);

                if (!requestDay.equals(slotDay)) {
                    return ResponseBuilder.build(
                            HttpStatus.BAD_REQUEST,
                            "Ngày hẹn không khớp với slot đã chọn",
                            null
                    );
                }

                // ===== CHECK SLOT BỊ CHIẾM =====
                boolean isBooked = consultationOfflineRequestRepo
                        .existsByCounsellorSlotAndAppointmentDateAndAppointmentTimeAndStatusInAndIdNot(
                                counsellorSlotOpt.get(),
                                slotDate,
                                slotTime,
                                List.of(
                                        Status.CONSULTATION_CONFIRMED,
                                        Status.CONSULTATION_IN_PROGRESS
                                ),
                                offlineRequest.getId()
                        );

                if (isBooked) {
                    return ResponseBuilder.build(
                            HttpStatus.BAD_REQUEST,
                            "Slot tư vấn viên đã được đặt",
                            null
                    );
                }

                // ===== UPDATE DATA =====
                offlineRequest.setAppointmentDate(slotDate);
                offlineRequest.setAppointmentTime(slotTime);
                offlineRequest.setCounsellorSlot(counsellorSlotOpt.get());
                offlineRequest.setCounsellor(null);
                offlineRequest.setLockedUntil(null);
                offlineRequest.setStatus(Status.CONSULTATION_CONFIRMED);
            }

            case START -> {
                if (currentStatus != Status.CONSULTATION_CONFIRMED) {
                    return ResponseBuilder.build(
                            HttpStatus.BAD_REQUEST,
                            "Chỉ có thể bắt đầu lịch hẹn đã được xác nhận",
                            null
                    );
                }

                offlineRequest.setStatus(Status.CONSULTATION_IN_PROGRESS);
            }

            case END -> {
                if (currentStatus != Status.CONSULTATION_IN_PROGRESS) {
                    return ResponseBuilder.build(
                            HttpStatus.BAD_REQUEST,
                            "Chỉ có thể kết thúc lịch hẹn đang tư vấn",
                            null
                    );
                }
                // Validate note
                if (request.getNote() == null || request.getNote().trim().isEmpty()) {
                    return ResponseBuilder.build(
                            HttpStatus.BAD_REQUEST,
                            "Vui lòng nhập nội dung tóm tắt buổi tư vấn",
                            null
                    );
                }

                if (request.getNote().length() > 5000) {
                    return ResponseBuilder.build(
                            HttpStatus.BAD_REQUEST,
                            "Nội dung tóm tắt không được vượt quá 5000 ký tự",
                            null
                    );
                }

                offlineRequest.setNote(request.getNote().trim());
                offlineRequest.setStatus(Status.CONSULTATION_COMPLETED);
            }

            case CANCEL -> {
                if (currentStatus != Status.CONSULTATION_PENDING &&
                        currentStatus != Status.CONSULTATION_CONFIRMED) {
                    return ResponseBuilder.build(
                            HttpStatus.BAD_REQUEST,
                            "Chỉ có thể hủy lịch hẹn đang chờ xác nhận hoặc đã xác nhận",
                            null
                    );
                }

                if (currentStatus == Status.CONSULTATION_PENDING
                        && offlineRequest.getCounsellor() != null
                        && offlineRequest.getLockedUntil() != null
                        && offlineRequest.getLockedUntil().isAfter(LocalDateTime.now())) {

                    boolean isOwner = offlineRequest.getCounsellor().getId()
                            .equals(currentCounsellor.getId());

                    if (!isOwner) {
                        return ResponseBuilder.build(
                                HttpStatus.CONFLICT,
                                "Lịch hẹn đang được xử lý bởi: "
                                        + offlineRequest.getCounsellor().getName(),
                                null
                        );
                    }
                }

                // Validate cancelReason
                if (request.getCancelReason() == null || request.getCancelReason().trim().isEmpty()) {
                    return ResponseBuilder.build(
                            HttpStatus.BAD_REQUEST,
                            "Vui lòng nhập lý do hủy lịch hẹn",
                            null
                    );
                }

                if (request.getCancelReason().length() > 500) {
                    return ResponseBuilder.build(
                            HttpStatus.BAD_REQUEST,
                            "Lý do hủy không được vượt quá 500 ký tự",
                            null
                    );
                }

                offlineRequest.setCancelBy(email);
                offlineRequest.setCancelReason(request.getCancelReason().trim());
                offlineRequest.setCancelTime(LocalDateTime.now());
                offlineRequest.setStatus(Status.CONSULTATION_CANCELLED);
            }

            case NO_SHOW -> {
                if (currentStatus != Status.CONSULTATION_CONFIRMED) {
                    return ResponseBuilder.build(
                            HttpStatus.BAD_REQUEST,
                            "Chỉ có thể đánh dấu không đến với lịch hẹn đã xác nhận",
                            null
                    );
                }

                LocalDateTime appointmentDateTime = LocalDateTime.of(
                        offlineRequest.getAppointmentDate(),
                        offlineRequest.getAppointmentTime()
                );

                LocalDateTime now = LocalDateTime.now();

                // Chưa tới giờ hẹn thì không cho NO_SHOW
                if (now.isBefore(appointmentDateTime)) {
                    return ResponseBuilder.build(
                            HttpStatus.BAD_REQUEST,
                            "Chưa tới thời gian hẹn, không thể đánh dấu không đến",
                            null
                    );
                }

                offlineRequest.setStatus(Status.CONSULTATION_NO_SHOW);
            }
        }

        consultationOfflineRequestRepo.save(offlineRequest);

        // ── Gửi notification sau khi cập nhật trạng thái ──────────────────────
        try {
            publishConsultationNotification(consultationAction, offlineRequest);
        } catch (Exception ignored) {
            // notification lỗi không block nghiệp vụ chính
        }

        return ResponseBuilder.build(HttpStatus.OK, "Cập nhật lịch hẹn thành công", null);
    }

    /**
     * Gửi notification tương ứng với từng ConsultationAction.
     * - CONFIRM / END / NO_SHOW → notify parent
     * - CANCEL → notify parent (counsellor huỷ)
     */
    private void publishConsultationNotification(ConsultationAction action,
                                                 ConsultationOfflineRequest req) {
        NotificationEventType eventType = switch (action) {
            case CONFIRM  -> NotificationEventType.CONSULTATION_CONFIRMED;
            case END      -> NotificationEventType.CONSULTATION_COMPLETED;
            case CANCEL   -> NotificationEventType.CONSULTATION_CANCELLED;
            case NO_SHOW  -> NotificationEventType.CONSULTATION_NO_SHOW;
            default       -> null;
        };
        if (eventType == null) return;

        // Recipient = parent
        Account parentAccount = req.getParent() != null ? req.getParent().getAccount() : null;
        if (parentAccount == null) return;

        String campusName = req.getCampus() != null ? req.getCampus().getName() : "Cơ sở trường";
        String appointmentDate = req.getAppointmentDate() != null
                ? req.getAppointmentDate().toString() : "";

        Map<String, Object> context = new HashMap<>();
        context.put("campusName", campusName);
        context.put("appointmentDate", appointmentDate);
        context.put("consultationId", req.getId());
        context.put("specificRecipients", List.of(parentAccount));

        notificationService.publish(eventType, null, context);
    }

    @Override
    public ResponseEntity<ResponseObject> getCounsellorsInSlot(LocalTime appointmentTime, LocalDate appointmentDate) {

        if (appointmentDate == null) {
            return ResponseBuilder.build(
                    HttpStatus.BAD_REQUEST,
                    "Ngày hẹn tư vấn không được để trống",
                    null
            );
        }

        if (appointmentTime == null) {
            return ResponseBuilder.build(
                    HttpStatus.BAD_REQUEST,
                    "Giờ hẹn tư vấn không được để trống",
                    null
            );
        }

        String dayOfWeek = appointmentDate.getDayOfWeek().name().substring(0, 3);
        // MON, TUE, WED, THU, FRI, SAT, SUN

        List<CampusScheduleTemplate> templates =
                campusScheduleTemplateRepo.findByDayOfWeekAndStartTimeAndActiveTrue(
                        dayOfWeek,
                        appointmentTime,
                        true
                );

        if (templates.isEmpty()) {
            return ResponseBuilder.build(
                    HttpStatus.OK,
                    "",
                    List.of()
            );
        }

        List<CounsellorSlot> counsellorSlots =
                counsellorSlotRepo
                        .findByCampusScheduleTemplateInAndStartDateLessThanEqualAndEndDateGreaterThanEqualAndStatus(
                                templates,
                                appointmentDate,
                                appointmentDate,
                                Status.AVAILABLE
                        );

        List<Map<String, Object>> result = counsellorSlots.stream()
                .map(slot -> buildCounsellorInSlot(slot, appointmentDate, appointmentTime))
                .toList();

        return ResponseBuilder.build(
                HttpStatus.OK,
                "Lấy danh sách tư vấn viên trong slot thành công",
                result
        );
    }

    @Override
    public ResponseEntity<ResponseObject> getCounsellorSlotsByDate(LocalDate date) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Counsellor counsellor = counsellorRepo.findByAccountId(accountRepo.findByEmail(email).get().getId());

        if (counsellor == null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Tài khoản tư vấn viên không tồn tại trong hệ thống", null);
        }

        String dayOfWeekCode = date.getDayOfWeek().name().substring(0, 3);

        List<CounsellorSlot> counsellorSlots =
                counsellorSlotRepo.findByCounsellorIdAndStartDateLessThanEqualAndEndDateGreaterThanEqualAndStatus(
                        counsellor.getId(), date, date, Status.AVAILABLE
                );

        List<Map<String, Object>> result = counsellorSlots.stream()
                .filter(slot -> dayOfWeekCode.equals(slot.getCampusScheduleTemplate().getDayOfWeek()))
                .map(slot -> {
                    CampusScheduleTemplate template = slot.getCampusScheduleTemplate();

                    List<ConsultationOfflineRequest> requests =
                            consultationOfflineRequestRepo.findByCounsellorSlot_CounsellorAndAppointmentDateAndAppointmentTime(
                                    counsellor, date, template.getStartTime()
                            );

                    List<Map<String, Object>> appointments = requests.stream()
                            .map(this::buildConsultationOfflineRequest)
                            .toList();

                    Status slotStatus = getSlotStatus(date, template.getStartTime(), template.getEndTime());

                    String sessionTypeLabel = switch (template.getSessionType()) {
                        case MORNING -> "Ca sáng";
                        case AFTERNOON -> "Ca chiều";
                        case EVENING -> "Ca tối";
                    };

                    Map<String, Object> slotMap = new HashMap<>();
                    slotMap.put("counsellorSlotId", slot.getId());
                    slotMap.put("sessionType", template.getSessionType());
                    slotMap.put("sessionTypeLabel", sessionTypeLabel);
                    slotMap.put("startTime", template.getStartTime());
                    slotMap.put("endTime", template.getEndTime());
                    slotMap.put("date", date);
                    slotMap.put("dayOfWeek", template.getDayOfWeek());
                    slotMap.put("slotStatus", slotStatus);
                    slotMap.put("slotStatusLabel", slotStatus.getValue());
                    slotMap.put("appointments", appointments);

                    return slotMap;
                })
                .sorted(Comparator.comparing(m -> (LocalTime) m.get("startTime")))
                .collect(java.util.stream.Collectors.toList());

        return ResponseBuilder.build(HttpStatus.OK, "Lấy danh sách slot thành công", result);
    }

    @Override
    public ResponseEntity<ResponseObject> getSlotsOfCampus(LocalDate startDate, LocalDate endDate) {

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

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Counsellor counsellor = counsellorRepo.findByAccountId(accountRepo.findByEmail(email).get().getId());

        if (counsellor == null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Tài khoản tư vấn viên không tồn tại trong hệ thống",null);
        }

        Optional<AdmissionCampaign> campaignOpt =
                admissionCampaignRepo.findBySchoolIdAndYearAndStatus(
                        counsellor.getCampus().getSchool().getId(),
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

        Object maxBookingPerSlot = "N/A";
        Object allowBookingBeforeHours = "N/A";

        if (counsellor.getCampus().getPolicyDetail() == null) {

            Optional<SchoolConfig> schoolConfig = schoolConfigRepo.findBySchoolIdAndKey(counsellor.getCampus().getSchool().getId(), "operationSettingsData");

            if (schoolConfig.isPresent()) {

                SchoolConfig schoolConfigData = schoolConfig.get();

                Map<String, Object> operationSettingsData = (Map<String, Object>) schoolConfigData.getValue();

                maxBookingPerSlot = (int) operationSettingsData.get("maxBookingPerSlot");
                allowBookingBeforeHours =
                        ((Number) operationSettingsData.get("allowBookingBeforeHours")).intValue();
            }
        }

        Map<String, Object> policyDetail = (Map<String, Object>) counsellor.getCampus().getPolicyDetail();

        if (policyDetail != null) {
            maxBookingPerSlot = (int) policyDetail.get("maxBookingPerSlot");
            allowBookingBeforeHours =
                    ((Number) policyDetail.get("allowBookingBeforeHours")).intValue();
        }

        AdmissionCampaign campaign = campaignOpt.get();

        List<CampusScheduleTemplate> campusScheduleTemplates =
                campusScheduleTemplateRepo.findByCampusIdAndActiveTrueOrderByStartTimeAsc(counsellor.getCampus().getId());

        List<ConsultationOfflineRequest> allRequests =
                consultationOfflineRequestRepo
                        .findByCampusIdAndAppointmentDateBetween(
                                counsellor.getCampus().getId(),
                                startDate,
                                endDate
                        );

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

            long totalRequests = allRequests.stream()
                    .filter(r ->
                            r.getAppointmentDate().equals(slotDate)
                                    && r.getAppointmentTime().equals(campusScheduleTemplate.getStartTime())
                                    && r.getStatus() != Status.CONSULTATION_CANCELLED
                    )
                    .count();

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
            slotMap.put("totalRequests", totalRequests);

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

    private ConsultationAction parseConsultationAction(String action) {

        if (action == null || action.isBlank()) {
            throw new IllegalArgumentException("Action không được để trống");
        }

        String normalized = action.trim().toLowerCase();

        return Arrays.stream(ConsultationAction.values())
                .filter(a -> a.getValue().equalsIgnoreCase(normalized))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Action không hợp lệ")
                );
    }

    private Map<String, Object> buildCounsellorInSlot(
            CounsellorSlot slot,
            LocalDate appointmentDate,
            LocalTime appointmentTime
    ) {
        Map<String, Object> map = new HashMap<>();

        boolean isBooked = consultationOfflineRequestRepo
                .existsByCounsellorSlotAndAppointmentDateAndAppointmentTimeAndStatusIn(
                        slot,
                        appointmentDate,
                        appointmentTime,
                        List.of(
                                Status.CONSULTATION_CONFIRMED,
                                Status.CONSULTATION_IN_PROGRESS
                        )
                );

        Counsellor counsellor = slot.getCounsellor();
        CampusScheduleTemplate template = slot.getCampusScheduleTemplate();

        map.put("counsellorSlotId", slot.getId());
        map.put("counsellorId", counsellor.getId());
        map.put("counsellorName", counsellor.getName());
        map.put("counsellorEmail", counsellor.getAccount().getEmail());

        map.put("dayOfWeek", template.getDayOfWeek());
        map.put("startTime", template.getStartTime());
        map.put("endTime", template.getEndTime());

        map.put("bookingStatus", isBooked ? "Đã được đặt" : "Còn trống");

        return map;
    }


    private Map<String, Object> buildConsultationOfflineRequest(
            ConsultationOfflineRequest request
    ) {

        Map<String, Object> map = new HashMap<>();

        map.put("id", request.getId());
        map.put("phone", request.getPhone());
        map.put("question", request.getQuestion());
        map.put("note", request.getNote());
        map.put("appointmentDate", request.getAppointmentDate());
        map.put("appointmentTime", request.getAppointmentTime());
        map.put("status", request.getStatus());
        map.put("parentId", request.getParent().getId());
        map.put("parentName", request.getParent().getName());

        LocalDate appointmentDate = request.getAppointmentDate();

        LocalDate startOfWeek = appointmentDate.with(java.time.DayOfWeek.MONDAY);
        LocalDate endOfWeek = appointmentDate.with(java.time.DayOfWeek.SUNDAY);

        map.put("startDateOfWeek", startOfWeek);
        map.put("endDateOfWeek", endOfWeek);

        if (request.getCounsellor() == null) {
            map.put("counsellorId", null);
            map.put("counsellorName", "N/A");
        } else {
            map.put("counsellorId", request.getCounsellor().getId());
            map.put("counsellorName", request.getCounsellor().getAccount().getEmail());
        }

        // ── Người đang tiếp nhận + thời gian xác nhận còn lại ───────
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lockedUntil = request.getLockedUntil();

        if (request.getCounsellor() != null
                && lockedUntil != null
                && lockedUntil.isAfter(now)) {

            // Còn bao nhiêu giây
            long secondsRemaining = java.time.Duration.between(now, lockedUntil).getSeconds();

            map.put("confirmingBy", request.getCounsellor().getAccount().getEmail());
            map.put("confirmingByName", request.getCounsellor().getName());
            map.put("lockedUntil", lockedUntil);
            map.put("secondsRemaining", secondsRemaining);        // FE dùng đếm ngược
            map.put("minutesRemaining", secondsRemaining / 60);   // hiển thị dạng phút
        } else {
            // Không có ai đang tiếp nhận hoặc lock đã hết hạn
            map.put("confirmingBy", null);
            map.put("confirmingByName", null);
            map.put("lockedUntil", null);
            map.put("secondsRemaining", null);
            map.put("minutesRemaining", null);
        }

        if (request.getCancelReason() == null) {
            map.put("cancelReason", null);
        } else {
            map.put("cancelReason", request.getCancelReason().trim());
        }

        if (request.getNote() == null) {
            map.put("note", null);
        } else {
            map.put("note", request.getNote().trim());
        }

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
