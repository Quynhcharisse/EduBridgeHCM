package com.sp26se041.edubridgehcm.services.implementors;

import com.sp26se041.edubridgehcm.enums.ConsultationAction;
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
import com.sp26se041.edubridgehcm.models.StudentProfile;
import com.sp26se041.edubridgehcm.repositories.AccountRepo;
import com.sp26se041.edubridgehcm.repositories.AdmissionCampaignRepo;
import com.sp26se041.edubridgehcm.repositories.CampusScheduleTemplateRepo;
import com.sp26se041.edubridgehcm.repositories.ChatMessageRepo;
import com.sp26se041.edubridgehcm.repositories.ConsultationOfflineRequestRepo;
import com.sp26se041.edubridgehcm.repositories.ConversationRepo;
import com.sp26se041.edubridgehcm.repositories.CounsellorRepo;
import com.sp26se041.edubridgehcm.repositories.CounsellorSlotRepo;
import com.sp26se041.edubridgehcm.repositories.PersonalityTypeRepo;
import com.sp26se041.edubridgehcm.repositories.SchoolConfigRepo;
import com.sp26se041.edubridgehcm.repositories.StudentInfoRepo;
import com.sp26se041.edubridgehcm.repositories.SubjectRepo;
import com.sp26se041.edubridgehcm.requests.UpdateConsultationOfflineRequest;
import com.sp26se041.edubridgehcm.responses.PageResponse;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.CounsellorService;
import com.sp26se041.edubridgehcm.utils.AccountRestrictionUtil;
import com.sp26se041.edubridgehcm.utils.PaginationUtil;
import com.sp26se041.edubridgehcm.utils.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
                counsellorSlotRepo.findByCounsellorIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        counsellor.getId(),
                        endDate,
                        startDate
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
                    consultationOfflineRequestRepo
                            .findByCounsellorAndAppointmentDateAndAppointmentTime(
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

        Status currentStatus = offlineRequest.getStatus();

        switch (consultationAction) {

            case CONFIRM -> {
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
                offlineRequest.setCounsellor(counsellorSlotOpt.get().getCounsellor());
                offlineRequest.setCounsellorSlot(counsellorSlotOpt.get());
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

                String email = SecurityContextHolder.getContext().getAuthentication().getName();

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

        return ResponseBuilder.build(HttpStatus.OK, "Cập nhật lịch hẹn thành công", null);
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
