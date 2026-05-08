package com.sp26se041.edubridgehcm.services.implementors;

import com.sp26se041.edubridgehcm.enums.CategoryTemplate;
import com.sp26se041.edubridgehcm.enums.NotificationEventType;
import com.sp26se041.edubridgehcm.enums.PackageType;
import com.sp26se041.edubridgehcm.enums.ParentPostPermission;
import com.sp26se041.edubridgehcm.enums.PersonalityTypeGroup;
import com.sp26se041.edubridgehcm.enums.Role;
import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.enums.SubjectType;
import com.sp26se041.edubridgehcm.enums.SupportLevel;
import com.sp26se041.edubridgehcm.models.Account;
import com.sp26se041.edubridgehcm.models.Campus;
import com.sp26se041.edubridgehcm.models.ChatMessage;
import com.sp26se041.edubridgehcm.models.Conversation;
import com.sp26se041.edubridgehcm.models.PaymentTransaction;
import com.sp26se041.edubridgehcm.models.PersonalityType;
import com.sp26se041.edubridgehcm.models.PlatformConfig;
import com.sp26se041.edubridgehcm.models.School;
import com.sp26se041.edubridgehcm.models.SchoolRegistrationRequest;
import com.sp26se041.edubridgehcm.models.SchoolSubscription;
import com.sp26se041.edubridgehcm.models.Subject;
import com.sp26se041.edubridgehcm.models.Subscription;
import com.sp26se041.edubridgehcm.models.TemplateDocx;
import com.sp26se041.edubridgehcm.repositories.AccountRepo;
import com.sp26se041.edubridgehcm.repositories.CampusRepo;
import com.sp26se041.edubridgehcm.repositories.ChatMessageRepo;
import com.sp26se041.edubridgehcm.repositories.ConversationRepo;
import com.sp26se041.edubridgehcm.repositories.PaymentTransactionRepo;
import com.sp26se041.edubridgehcm.repositories.PersonalityTypeRepo;
import com.sp26se041.edubridgehcm.repositories.PlatformConfigRepo;
import com.sp26se041.edubridgehcm.repositories.SchoolRegistrationRequestRepo;
import com.sp26se041.edubridgehcm.repositories.SchoolRepo;
import com.sp26se041.edubridgehcm.repositories.SchoolSubscriptionRepo;
import com.sp26se041.edubridgehcm.repositories.SubjectRepo;
import com.sp26se041.edubridgehcm.repositories.SubscriptionRepo;
import com.sp26se041.edubridgehcm.repositories.TemplateDocxRepo;
import com.sp26se041.edubridgehcm.requests.AddSubjectRequest;
import com.sp26se041.edubridgehcm.requests.AutoFillQuotasByYearRequest;
import com.sp26se041.edubridgehcm.requests.CreatePersonalityTypeRequest;
import com.sp26se041.edubridgehcm.requests.UpsertServicePackageFeeRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.AdminService;
import com.sp26se041.edubridgehcm.services.NotificationService;
import com.sp26se041.edubridgehcm.services.SchoolConfigService;
import com.sp26se041.edubridgehcm.services.SupabaseStorageService;
import com.sp26se041.edubridgehcm.utils.AccountRestrictionUtil;
import com.sp26se041.edubridgehcm.utils.AuthRequestUtil;
import com.sp26se041.edubridgehcm.utils.ConfigSystemUtil;
import com.sp26se041.edubridgehcm.utils.ResponseBuilder;
import com.sp26se041.edubridgehcm.validations.admin.SubscriptionValidation;
import com.sp26se041.edubridgehcm.validations.admin.VerifyRegistrationValidation;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
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
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final NotificationService notificationService;

    private final SchoolConfigService schoolConfigService;

    @Value("${AI_SERVICE_N8N}")
    private String n8nUrl;

    private final SchoolRegistrationRequestRepo schoolRegistrationRequestRepo;

    private final AccountRepo accountRepo;

    private final SchoolRepo schoolRepo;

    private final CampusRepo campusRepo;

    private final PersonalityTypeRepo personalityTypeRepo;

    private final SubjectRepo subjectRepo;

    private final SupabaseStorageService supabaseStorageService;

    private final SubscriptionRepo subscriptionRepo;

    private final SchoolSubscriptionRepo schoolSubscriptionRepo;

    private final TemplateDocxRepo templateDocxRepo;

    private final ConversationRepo conversationRepo;

    private final ChatMessageRepo chatMessageRepo;

    private final PlatformConfigRepo platformConfigRepo;

    private final PaymentTransactionRepo paymentTransactionRepo;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public ResponseEntity<ResponseObject> autoFillQuotasByYear(AutoFillQuotasByYearRequest request) {

        Map<String, Object> payload = new LinkedHashMap<>();

        payload.put("url", request.getUrl());

        List<Map<String, Object>> items = new ArrayList<>();

        List<School> schools = schoolRepo.findAll();

        if (schools.isEmpty()) {
            return ResponseBuilder.build(
                    HttpStatus.NOT_FOUND,
                    "Không tìm thấy trường nào trong hệ thống để điền",
                    null
            );
        }

        schools.forEach(school -> {
            Map<String, Object> schoolMap = new HashMap<>();
            schoolMap.put("id", school.getId());
            schoolMap.put("name", school.getName());
            items.add(schoolMap);
        });

        payload.put("schools", items);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    "https://n8n-service-ijbl.onrender.com/webhook/news_article",
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
                    "Không gọi được n8n workflow",
                    null
            );
        }

    }

    @Override
    public ResponseEntity<ResponseObject> getConversations(Long cursorId) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        Optional<Account> accAdmin = accountRepo.findByEmail(email);

        List<Conversation> conversations;

        if (cursorId == null) {
            conversations = conversationRepo.findTop20ByAccAdminId(accAdmin.get().getId());
        } else {
            conversations = conversationRepo.findTop20ByAccAdminIdAndIdLessThan(accAdmin.get().getId(), cursorId);
        }
        List<Map<String, Object>> items = buildConversationList(conversations, accAdmin.get().getEmail().toString());

        boolean hasMore = conversations.size() == 20;
        Long nextCursorId = hasMore && !conversations.isEmpty()
                ? conversations.get(conversations.size() - 1).getId()
                : null;

        Map<String, Object> result = new HashMap<>();
        result.put("items", items);
        result.put("hasMore", hasMore);
        result.put("nextCursorId", nextCursorId);

        return ResponseBuilder.build(HttpStatus.OK, "", result);
    }

    @Override
    public ResponseEntity<ResponseObject> getChatHistory(int campusId, Long cursorId) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        Optional<Account> accAdmin = accountRepo.findByEmail(email);

        if (accAdmin.isEmpty()) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Không tìm thấy hoặc đã xóa tài khoản quản trị viên.", null);
        }

        Optional<Campus> campus = campusRepo.findById(campusId);


        if (campus.isEmpty()) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Không tìm thấy cơ sở.", null);
        }

        Optional<Conversation> existingConversation = conversationRepo.findByCampusIdAndAccAdminId(campus.get().getId(), accAdmin.get().getId());

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
            return ResponseBuilder.build(HttpStatus.OK, "Lấy lịch sử trò chuyện thành công", buildHistoryMessages(existingConversation.get(), accAdmin.get().getEmail(), campus.get().getAccount().getEmail(), messages, hasMore, nextCursorId));
        }
        Conversation conversation = Conversation.builder()
                .campusId(campus.get().getId())
                .accAdminId(accAdmin.get().getId())
//                .status(Status.CONVERSATION_ACTIVE)
                .createdDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .build();
//        conversationRepo.save(conversation);

        return ResponseBuilder.build(HttpStatus.OK, "Lấy lịch sử trò chuyện thành công", buildHistoryMessages(conversation, accAdmin.get().getEmail(), campus.get().getAccount().getEmail(), messages, hasMore, nextCursorId));
    }

    private Map<String, Object> buildHistoryMessages(Conversation conversation, String adminEmail, String campusEmail, List<ChatMessage> messages, boolean hasMore, Long nextCursorId) {

        Map<String, Object> response = new HashMap<>();

        response.put("conversationId", conversation.getId());
        response.put("accAdminId", conversation.getAccAdminId());
        response.put("adminEmail", adminEmail);
        response.put("campusId", conversation.getCampusId());
        response.put("campusEmail", campusEmail);
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
            String receiverName
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
                                    receiverName,
                                    Status.MESSAGE_READ
                            );

                    Map<String, Object> map = new HashMap<>();

                    Optional<Campus> campus = campusRepo.findById(conversation.getCampusId());

                    if (campus.isEmpty()) {
                        return map;
                    }

                    map.put("conversationId", conversation.getId());
                    map.put("campusId", campus.get().getId());
                    map.put("accAdminId", conversation.getAccAdminId());
                    map.put("lastMessage", lastMessage != null ? lastMessage.getMessage() : null);
                    map.put("updatedAt", conversation.getUpdatedDate());
                    map.put("unreadCount", unreadCount != null ? unreadCount : 0L);
                    map.put("campusName", campus.get().getName());
                    map.put("schoolId", campus.get().getSchool().getId());
                    map.put("schoolName", campus.get().getSchool().getName());
                    map.put("schoolLogoUrl", campus.get().getSchool().getLogoUrl());
                    map.put("status", conversation.getStatus());

                    return map;

                })
                .toList();
    }

    @Override
    @Transactional
    public ResponseEntity<ResponseObject> verifyRegistration(int requestId) {

        if (requestId <= 0) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Mã yêu cầu (requestId) phải lớn hơn 0.", null);
        }

        SchoolRegistrationRequest request = schoolRegistrationRequestRepo.findById(requestId).orElse(null);

        if (request == null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Không tìm thấy yêu cầu đăng ký với mã: " + requestId, null);

        }

        String error = VerifyRegistrationValidation.validationVerifyRegistration(requestId, request, schoolRepo);
        if (!error.isEmpty()) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, error, null);
        }

        return handleVerify(request);
    }


    private ResponseEntity<ResponseObject> handleVerify(SchoolRegistrationRequest request) {

        Optional<TemplateDocx> campusTemplateDocx = templateDocxRepo.findTopByTypeOrderByVersionDesc(CategoryTemplate.CAMPUS_INFO_TEMPLATE);

        if (campusTemplateDocx.isEmpty()) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Mẫu tài liệu cơ sở không khả dụng, không thể hoàn tất xác minh.", null);
        }

        Optional<TemplateDocx> schoolTemplateDocx = templateDocxRepo.findTopByTypeOrderByVersionDesc(CategoryTemplate.SCHOOL_INFO_TEMPLATE);

        if (schoolTemplateDocx.isEmpty()) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Mẫu tài liệu thông tin trường không khả dụng, không thể hoàn tất xác minh.", null);
        }

        String uuid = UUID.randomUUID().toString();

        String campusFileUrl = "";

        String schoolFileUrl = "";

        String autoGenCampusName = "Cơ sở 1 (Cơ sở chính)";

        try {

            Map<String, Object> fields = new LinkedHashMap<>();
            fields.put("name", autoGenCampusName);
            fields.put("schoolName", request.getSchoolName().trim());
            fields.put("phoneNumber", request.getCampusPhone().trim());
            fields.put("address", request.getCampusAddress().trim());
            fields.put("boardingType", "UPDATING");
            fields.put("boardingDescription", "UPDATING");

            String schoolName = toSafeObjectKey(request.getSchoolName());
            String campusName = toSafeObjectKey(autoGenCampusName);
            String templatePath = campusTemplateDocx.get().getFolderName() + "/" + campusTemplateDocx.get().getFileName();
            String folderName = schoolName + "_" + uuid + "/" + campusName;
            String fileName = "campus_info_" + uuid + ".docx";

            campusFileUrl = supabaseStorageService.generateDocFileFromTemplate(fields, templatePath, folderName, fileName);

        } catch (Exception ex) {
            return ResponseBuilder.build(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), null);
        }

        String newBusinessLicenseUrl = request.getBusinessLicenseUrl(); // Mặc định dùng lại url cũ

        try {

            String schoolNameSlug = toSafeObjectKey(request.getSchoolName());
            String oldPath = supabaseStorageService.extractObjectPath(request.getBusinessLicenseUrl());

            // Kiểm tra sơ bộ tránh lỗi substring nếu URL không có dấu chấm
            if (oldPath.contains(".")) {
                String extension = oldPath.substring(oldPath.lastIndexOf('.') + 1);
                String newPath = schoolNameSlug + "_" + uuid + "/" + schoolNameSlug + "_business_license" + "." + extension;

                // Cố gắng move file
                newBusinessLicenseUrl = supabaseStorageService.moveFile(oldPath, newPath);
            }
        } catch (Exception ex) {
            // Log lỗi nhưng không return 500 để Admin vẫn duyệt được account
            System.out.println(ex.getMessage());
        }

        Account account = accountRepo.save(Account.builder().role(Role.SCHOOL)
                .email(request.getEmail().trim())
                .registerDate(LocalDate.now())
                .status(Status.ACCOUNT_ACTIVE)
                .firstLogin(true)
                .build());

        School school = schoolRepo.save(School.builder()
                .name(request.getSchoolName().trim())
                .description(request.getDescription().trim())
                .taxCode(request.getTaxCode().trim())
                .websiteUrl(request.getWebsiteUrl())
                .logoUrl(request.getLogoUrl()).representativeName(request.getRepresentativeName())
                .hotline(request.getHotline()).foundingDate(request.getFoundingDate())
                .folderPath(toSafeObjectKey(request.getSchoolName()) + "_" + uuid)
                .fileName("school_info_" + uuid + ".docx")
                .businessLicenseUrl(newBusinessLicenseUrl)
                .build());

        try {
            schoolConfigService.regenerateSchoolInfoDoc(school.getId());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        Campus campus = campusRepo.save(
                Campus.builder()
                        .account(account)
                        .name(autoGenCampusName)
                        .phoneNumber(request.getCampusPhone())
                        .address(request.getCampusAddress().trim())
                        .folderPath(toSafeObjectKey(request.getSchoolName().trim()) + "_" + uuid + "/" + toSafeObjectKey(autoGenCampusName))
                        .fileName("campus_info_" + uuid + ".docx")
                        .status(Status.ACTIVE)
                        .isPrimaryBranch(true)
                        .school(school).build());

        if (!campusFileUrl.isEmpty()) {
            try {
                Map<String, Object> payload = new LinkedHashMap<>();
                payload.put("type", "campus_info");
                payload.put("schoolId", school.getId());
                payload.put("schoolName", request.getSchoolName().trim());
                payload.put("campusId", campus.getId());
                payload.put("campusInfoFileUrl", campusFileUrl);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

                restTemplate.postForEntity(
                        n8nUrl,
                        entity,
                        String.class
                );
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }

        // đánh dấu bảng tạm đã duyệt
        request.setStatus(Status.VERIFIED);
        request.setBusinessLicenseUrl(newBusinessLicenseUrl);
        schoolRegistrationRequestRepo.save(request);

        Map<String, Object> response = new HashMap<>();
        response.put("email", account.getEmail());

        return ResponseBuilder.build(HttpStatus.OK, "Xác minh thành công", response);
    }

    private String toSafeObjectKey(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "";
        }

        // 1. normalize Unicode (tách dấu ra)
        String normalized = Normalizer.normalize(input.trim(), Normalizer.Form.NFD);

        // 2. remove dấu (accent)
        String noAccent = normalized.replaceAll("\\p{M}", "");

        // 3. xử lý riêng đ/Đ
        noAccent = noAccent.replace("đ", "d").replace("Đ", "d");

        // 4. lowercase
        String lower = noAccent.toLowerCase(Locale.ROOT);

        // 5. replace ký tự không hợp lệ -> _
        String safe = lower.replaceAll("[^a-z0-9]+", "_");

        // 6. cleanup: nhiều _ -> 1
        safe = safe.replaceAll("_+", "_");

        // 7. remove _ đầu/cuối
        safe = safe.replaceAll("^_+|_+$", "");

        return safe;
    }

    @Override
    public ResponseEntity<ResponseObject> viewSchoolRegistrationList() {

        List<SchoolRegistrationRequest> schoolRegistrationRequestList = schoolRegistrationRequestRepo.findAllByOrderByCreatedAtDesc();

        List<Map<String, Object>> data = schoolRegistrationRequestList.stream().map(this::buildRegistrationData).toList();

        return ResponseBuilder.build(HttpStatus.OK, "Lấy danh sách đăng ký trường thành công", data);
    }

    private Map<String, Object> buildRegistrationData(SchoolRegistrationRequest request) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", request.getId());
        data.put("schoolName", request.getSchoolName());
        data.put("taxCode", request.getTaxCode());
        data.put("websiteUrl", request.getWebsiteUrl());
        data.put("logoUrl", request.getLogoUrl());
        data.put("foundingDate", request.getFoundingDate());
        data.put("representativeName", request.getRepresentativeName());
        data.put("hotline", request.getHotline());
        data.put("businessLicenseUrl", request.getBusinessLicenseUrl());
        data.put("campusName", "Cơ sở 1 (Cơ sở chính)");
        data.put("campusAddress", request.getCampusAddress());
        data.put("campusPhone", request.getCampusPhone());
        data.put("status", request.getStatus());
        data.put("createdAt", request.getCreatedAt());
        return data;
    }

    @Override
    @Transactional
    public ResponseEntity<ResponseObject> upsertServicePackageFee(UpsertServicePackageFeeRequest request) {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Tài khoản của bạn đang bị hạn chế.", null);
        }

        String error = SubscriptionValidation.upsertSubscriptionValidation(request);

        if (error != null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, error, null);
        }

        boolean isCreate = (request.getPackageId() == null);

        // Pre-fetch business config before transaction-heavy operations
        PlatformConfig businessConfig = platformConfigRepo.findByKey("business").orElse(null);

        if (businessConfig == null || businessConfig.getValue() == null) {
            return ResponseBuilder.build(HttpStatus.INTERNAL_SERVER_ERROR, "Chưa cấu hình doanh nghiệp.", null);
        }

        if (!(businessConfig.getValue() instanceof Map<?, ?> businessRaw)) {
            return ResponseBuilder.build(HttpStatus.INTERNAL_SERVER_ERROR, "Cấu hình doanh nghiệp không hợp lệ.", null);
        }

        Map<String, Object> businessMap = (Map<String, Object>) businessRaw;

        // Apply defaults based on package type (mutates request featureData)
        applyPackageTypeDefaults(request, businessMap);

        PackageType packageType = SubscriptionValidation.parsePackageType(request.getPackageType());
        SupportLevel supportLevel = SubscriptionValidation.parseSupportLevel(request.getFeatureData().getSupportLevel());

        if (packageType == null || supportLevel == null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Loại gói hoặc mức hỗ trợ không hợp lệ.", null);
        }

        // Validate trial package constraints
        if (packageType == PackageType.TRIAL) {
            String trialError = SubscriptionValidation.validateTrialPackage(request, businessMap);
            if (trialError != null) {
                return ResponseBuilder.build(HttpStatus.BAD_REQUEST, trialError, null);
            }
        }

        // Calculate pricing before saving (outside transaction where possible)
        ConfigSystemUtil.SubscriptionPriceBreakdown breakdown;
        try {
            breakdown = previewServicePackagePricing(request, businessMap);
        } catch (RuntimeException ex) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Không thể tính giá: " + ex.getMessage(), null);
        }

        Subscription subscription;

        if (!isCreate) {
            subscription = subscriptionRepo.findById(request.getPackageId()).orElse(null);

            if (subscription == null) {
                return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Không tìm thấy gói dịch vụ.", null);
            }

            if (subscription.getPackageStatus() != Status.PACKAGE_DRAFT) {
                return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Chỉ có thể chỉnh sửa gói ở trạng thái nháp.", null);
            }
        } else {
            subscription = new Subscription();
            subscription.setPackageStatus(Status.PACKAGE_DRAFT);
        }

        subscription.setName(request.getName());
        assert packageType != null;
        subscription.setPackageType(packageType);
        subscription.setDescription(request.getDescription());
        subscription.setDurationDays(request.getDurationDays());

        if (request.getFeatureData() != null) {
            subscription.setFeatures(buildFeatureJson(request.getFeatureData(), supportLevel));
        }

        subscription.setPrice(breakdown.netPrice());
        subscription.setServiceFee(breakdown.serviceFee());
        subscription.setTaxFee(breakdown.taxFee());
        subscription.setFinalPrice(breakdown.finalPrice());

        subscriptionRepo.save(subscription);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("subscription", buildSubscriptionData(subscription, true, businessMap));

        // Bảng kê tài chính chi tiết cho Admin
        Map<String, Object> pricing = new LinkedHashMap<>();
        pricing.put("basePrice", Map.of("amount", breakdown.basePrice()));
        pricing.put("totalFeatureAmount", Map.of("amount", breakdown.totalFeatureAmount()));
        pricing.put("netPrice", Map.of("amount", breakdown.netPrice()));
        pricing.put("serviceFee", Map.of("amount", breakdown.serviceFee()));
        pricing.put("taxFee", Map.of("amount", breakdown.taxFee()));
        pricing.put("finalPrice", Map.of("amount", breakdown.finalPrice()));
        response.put("pricing", pricing);

        buildFeatureContributions(request, businessMap, breakdown, response);

        return ResponseBuilder.build(isCreate ? HttpStatus.CREATED : HttpStatus.OK,
                isCreate ? "Tạo gói nháp thành công" : "Cập nhật gói nháp thành công", response);
    }

    private void buildFeatureContributions(UpsertServicePackageFeeRequest request,
                                           Map<String, Object> businessMap,
                                           ConfigSystemUtil.SubscriptionPriceBreakdown breakdown,
                                           Map<String, Object> response) {

        Map<String, Object> featureDetails = new LinkedHashMap<>();
        UpsertServicePackageFeeRequest.FeatureData featureData = request.getFeatureData();

        Map<String, Object> subscriptionPricing = (Map<String, Object>) businessMap.get("subscriptionPricing");
        Map<String, Object> unitPrices = (Map<String, Object>) subscriptionPricing.get("featureUnitPrices");

        if (Boolean.TRUE.equals(featureData.getHasAiAssistant())) {
            featureDetails.put("aiAssistantFee", unitPrices.get("aiChatbotMonthlyFee"));
        }

        if (SupportLevel.PREMIUM_SUPPORT.name().equals(featureData.getSupportLevel())) {
            featureDetails.put("premiumSupportFee", unitPrices.getOrDefault("premiumSupportFee", 0));
        }

        response.put("featureContributions", featureDetails);
    }

    private Map<String, Object> buildFeatureJson(UpsertServicePackageFeeRequest.FeatureData request, SupportLevel supportLevel) {
        Map<String, Object> data = new HashMap<>();
        data.put("maxCounsellors", request.getMaxCounsellors());
        data.put("postLimit", request.getPostLimit());
        data.put("hasAiAssistant", request.getHasAiAssistant());
        data.put("parentPostPermission", request.getParentPostPermission());
        data.put("supportLevel", supportLevel);
        return data;
    }

    private void applyPackageTypeDefaults(UpsertServicePackageFeeRequest request, Map<String, Object> businessMap) {

        PackageType packageType = SubscriptionValidation.parsePackageType(request.getPackageType());

        if (packageType == null) return;

        //get packageQuota from businessMap
        Map<String, Object> subscriptionPricing = (Map<String, Object>) businessMap.get("subscriptionPricing");

        if (subscriptionPricing == null) return;

        Map<String, Object> packageQuota = (Map<String, Object>) subscriptionPricing.get("packageQuotas");

        if (packageQuota == null) return;

        if (request.getFeatureData() == null) {
            request.setFeatureData(new UpsertServicePackageFeeRequest.FeatureData());
        }

        UpsertServicePackageFeeRequest.FeatureData featureData = request.getFeatureData();

        switch (packageType) {
            case TRIAL -> {
                Integer trialCounsellor = SubscriptionValidation.getIntFromMap(packageQuota, "trialCounsellor");
                Integer trialPostLimit = SubscriptionValidation.getIntFromMap(packageQuota, "trialPostLimit");

                if (featureData.getMaxCounsellors() == null && trialCounsellor != null) {
                    featureData.setMaxCounsellors(trialCounsellor);
                }

                if (featureData.getPostLimit() == null && trialPostLimit != null) {
                    featureData.setPostLimit(trialPostLimit);
                }

                // Cho phép tạo post trong thời gian dùng thử
                featureData.setParentPostPermission(ParentPostPermission.CREATE_POST.name());
            }
            case STANDARD -> {
                Integer standardCounsellor = SubscriptionValidation.getIntFromMap(packageQuota, "standardCounsellor");
                Integer standardPostLimit = SubscriptionValidation.getIntFromMap(packageQuota, "standardPostLimit");

                if (featureData.getMaxCounsellors() == null && standardCounsellor != null)
                    featureData.setMaxCounsellors(standardCounsellor);

                if (featureData.getPostLimit() == null && standardPostLimit != null)
                    featureData.setPostLimit(standardPostLimit);

                if (featureData.getParentPostPermission() == null)
                    featureData.setParentPostPermission(ParentPostPermission.CREATE_POST.name());
            }
            case ENTERPRISE -> {
                Integer enterpriseCounsellor = SubscriptionValidation.getIntFromMap(packageQuota, "enterpriseCounsellor");
                Integer enterprisePostLimit = SubscriptionValidation.getIntFromMap(packageQuota, "enterprisePostLimit");

                if (featureData.getMaxCounsellors() == null && enterpriseCounsellor != null)
                    featureData.setMaxCounsellors(enterpriseCounsellor);

                if (featureData.getPostLimit() == null && enterprisePostLimit != null)
                    featureData.setPostLimit(enterprisePostLimit);

                if (featureData.getParentPostPermission() == null)
                    featureData.setParentPostPermission(ParentPostPermission.CREATE_POST.name());
            }
        }
    }

    private ConfigSystemUtil.SubscriptionPriceBreakdown previewServicePackagePricing(
            UpsertServicePackageFeeRequest request, Map<String, Object> businessMap) {
        try {
            return ConfigSystemUtil.calculateSubscriptionPriceBreakdown(request, businessMap);
        } catch (RuntimeException ex) {
            throw new RuntimeException("Không thể tính giá: " + ex.getMessage(), ex);
        }
    }

    @Override
    public ResponseEntity<ResponseObject> viewServicePackageFeeList() {

        Account currentAccount = AuthRequestUtil.extractAuthenticatedAccount();
        boolean isAdmin = currentAccount != null && Role.ADMIN.equals(currentAccount.getRole());

        List<Subscription> subscriptions = isAdmin
                ? subscriptionRepo.findAll() :
                subscriptionRepo.findAllByPackageStatus(Status.PACKAGE_ACTIVE);

        if (subscriptions.isEmpty()) {
            return ResponseBuilder.build(HttpStatus.OK, "Không có gói dịch vụ nào", Collections.emptyList());
        }

        Map<String, Object> businessMap = isAdmin ? getBusinessConfigMap() : null;

        List<Map<String, Object>> data = subscriptions.stream()
                .map(sub -> buildSubscriptionData(sub, isAdmin, businessMap, isAdmin))
                .collect(Collectors.toList());

        return ResponseBuilder.build(HttpStatus.OK, "Lấy danh sách phí gói thành công", data);
    }

    private Map<String, Object> getBusinessConfigMap() {
        try {
            PlatformConfig businessConfig = platformConfigRepo.findByKey("business").orElse(null);
            if (businessConfig != null && businessConfig.getValue() instanceof Map<?, ?> businessRaw) {
                return (Map<String, Object>) businessRaw;
            }
        } catch (Exception ignored) {
            // best-effort read only
        }
        return null;
    }

    private Map<String, Object> buildSubscriptionData(Subscription subscription,
                                                      boolean isAdmin,
                                                      Map<String, Object> businessMap) {
        return buildSubscriptionData(subscription, isAdmin, businessMap, false);
    }

    private Map<String, Object> buildSubscriptionData(Subscription subscription,
                                                      boolean isAdmin,
                                                      Map<String, Object> businessMap,
                                                      boolean includeBillingDetails) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", subscription.getId());
        data.put("name", subscription.getName());
        data.put("description", subscription.getDescription());
        data.put("packageType", subscription.getPackageType());
        data.put("finalPrice", subscription.getFinalPrice());
        data.put("durationDays", subscription.getDurationDays());

        if (subscription.getFeatures() instanceof Map<?, ?> features) {
            data.put("maxCounsellors", features.get("maxCounsellors"));
            data.put("postLimit", features.get("postLimit"));
            data.put("hasAiAssistant", features.get("hasAiAssistant"));
            data.put("parentPostPermission", features.get("parentPostPermission"));
            data.put("supportLevel", features.get("supportLevel"));

            if (isAdmin) {
                data.put("netPrice", subscription.getPrice());
                data.put("serviceFee", subscription.getServiceFee());
                data.put("taxFee", subscription.getTaxFee());
                data.put("status", subscription.getPackageStatus());
                data.put("fullFeatures", subscription.getFeatures());

                // Tính toán breakdown kiểu hóa đơn nếu có config
                if (includeBillingDetails && businessMap != null) {
                    appendPriceBreakdown(subscription, data, (Map<String, Object>) features, businessMap);
                }

                try {
                    if (businessMap != null) {
                        Map<String, Object> subscriptionPricing = (Map<String, Object>) businessMap.get("subscriptionPricing");
                        if (subscriptionPricing != null) {
                            Map<String, Object> unitPrices = (Map<String, Object>) subscriptionPricing.get("featureUnitPrices");
                            if (unitPrices != null) {
                                Map<String, Object> featureContributions = new LinkedHashMap<>();
                                if (Boolean.TRUE.equals(((Map) subscription.getFeatures()).get("hasAiAssistant"))) {
                                    featureContributions.put("aiAssistantFee", unitPrices.get("aiChatbotMonthlyFee"));
                                }
                                Object supportVal = ((Map) subscription.getFeatures()).get("supportLevel");
                                if (supportVal != null && SupportLevel.PREMIUM_SUPPORT.name().equals(supportVal.toString())) {
                                    featureContributions.put("premiumSupportFee", unitPrices.getOrDefault("premiumSupportFee", 0));
                                }
                                data.put("featureContributions", featureContributions);
                            }
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        }
        return data;
    }

    private void appendPriceBreakdown(Subscription subscription,
                                      Map<String, Object> data,
                                      Map<String, Object> features,
                                      Map<String, Object> businessMap) {
        try {
            Map<String, Object> pricingConfigs = ConfigSystemUtil.getPricingConfig(businessMap);
            Map<String, Object> basePrices = (Map<String, Object>) pricingConfigs.get("basePricing");
            Map<String, Object> unitPrices = (Map<String, Object>) pricingConfigs.get("featureUnitPricing");

            if (unitPrices == null) return;

            PackageType packageType = subscription.getPackageType();

            // DÙNG CHUNG: Gọi hàm resolveBasePrice thay vì viết switch-case thủ công
            BigDecimal basePrice = ConfigSystemUtil.resolveBasePrice(basePrices, packageType);
            data.put("basePrice", basePrice);

            Map<String, BigDecimal> breakdown = new LinkedHashMap<>();
            BigDecimal totalFeatureAmount = BigDecimal.ZERO;

            // Tiền AI Assistant
            if (Boolean.TRUE.equals(features.get("hasAiAssistant"))) {
                BigDecimal fee = toBigDecimal(unitPrices.get("aiChatbotMonthlyFee"));
                breakdown.put("aiAssistantFee", fee);
                totalFeatureAmount = totalFeatureAmount.add(fee);
            }

            // Tiền Premium Support
            if (features.get("supportLevel") != null &&
                    SupportLevel.PREMIUM_SUPPORT.name().equals(features.get("supportLevel").toString())) {
                BigDecimal supportFee = toBigDecimal(unitPrices.getOrDefault("premiumSupportFee", 0));
                breakdown.put("premiumSupportFee", supportFee);
                totalFeatureAmount = totalFeatureAmount.add(supportFee);
            }

            // Đóng gói kết quả hiển thị với mô tả rõ ràng cho từng thành phần giá
            Map<String, Object> pricingBreakdown = new LinkedHashMap<>();

            Map<String, Object> basePriceObj = new LinkedHashMap<>();
            basePriceObj.put("amount", basePrice);
            pricingBreakdown.put("basePrice", basePriceObj);

            Map<String, Object> featuresObj = new LinkedHashMap<>();
            featuresObj.put("amount", totalFeatureAmount);
            featuresObj.put("details", breakdown);
            pricingBreakdown.put("features", featuresObj);

            Map<String, Object> netPriceObj = new LinkedHashMap<>();
            netPriceObj.put("amount", subscription.getPrice());
            pricingBreakdown.put("netPrice", netPriceObj);

            Map<String, Object> serviceFeeObj = new LinkedHashMap<>();
            serviceFeeObj.put("amount", subscription.getServiceFee());
            pricingBreakdown.put("serviceFee", serviceFeeObj);

            Map<String, Object> taxFeeObj = new LinkedHashMap<>();
            taxFeeObj.put("amount", subscription.getTaxFee());
            pricingBreakdown.put("taxFee", taxFeeObj);

            Map<String, Object> finalPriceObj = new LinkedHashMap<>();
            finalPriceObj.put("amount", subscription.getFinalPrice());
            pricingBreakdown.put("finalPrice", finalPriceObj);

            data.put("pricingBreakdown", pricingBreakdown);

        } catch (Exception e) {
            // Log lỗi nếu cần thiết để debug cấu hình
        }
    }

    private BigDecimal toBigDecimal(Object obj) {
        if (obj instanceof Number n) return java.math.BigDecimal.valueOf(n.doubleValue());
        return java.math.BigDecimal.ZERO;
    }

    @Override
    public ResponseEntity<ResponseObject> getAllActiveSchools() {
        List<SchoolSubscription> activeList = schoolSubscriptionRepo.findAllByIsSelectedTrueAndEndDateGreaterThanEqualOrderByEndDateAsc(LocalDate.now());
        List<Map<String, Object>> response = activeList.stream()
                .map(a -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("id", a.getId());
                    data.put("schoolName", a.getSchool().getName());
                    data.put("subscriptionName", a.getSubscription().getName());
                    data.put("startDate", a.getStartDate());
                    data.put("endDate", a.getEndDate());

                    long daysRemaining = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), a.getEndDate());
                    data.put("daysRemaining", daysRemaining);

                    data.put("licenseKey", a.getLicenseKey());
                    data.put("isExpiringSoon", daysRemaining <= 7);

                    return data;
                })
                .collect(Collectors.toList());

        return ResponseBuilder.build(HttpStatus.OK, "", response);
    }

    @Override
    public ResponseEntity<ResponseObject> getAllTransaction() {
        List<PaymentTransaction> transactions = paymentTransactionRepo.findAllByOrderByCreatedAtAsc();

        List<Map<String, Object>> tableData = transactions.stream().map(t -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("transactionId", t.getId());
            row.put("schoolName", t.getSchool().getName());

            String packageName = "N/A";
            packageName = t.getSchoolSubscription().getSubscription().getName();
            row.put("packageName", packageName);
            row.put("txnRef", t.getVnpTxnRef());
            row.put("vnpTransactionNo", t.getVnpTransactionNo());
            row.put("orderInfo", t.getVnpOrderInfo());
            row.put("bankCode", t.getVnpBankCode());
            row.put("cardType", t.getVnpCardType());

            Long displayAmount = t.getStatus() == Status.PAYMENT_PENDING ? t.getExpectedAmount() : t.getVnpAmount();
            row.put("amount", displayAmount / 100.0);
            row.put("expectedAmount", t.getExpectedAmount() / 100.0);
            row.put("paidAmount", t.getVnpAmount() / 100.0);

            row.put("status", t.getStatus());
            row.put("createdAt", t.getCreatedAt());
            return row;
        }).toList();

        //Doanh thu thành công theo ngày
        Map<String, Double> dailyRevenue = transactions.stream()
                .filter(t -> t.getStatus().equals(Status.PAYMENT_SUCCESS))
                .collect(Collectors.groupingBy(
                        t -> t.getCreatedAt().toLocalDate().toString(),
                        TreeMap::new,
                        Collectors.summingDouble(t -> t.getVnpAmount() / 100.0)
                ));

        List<Map<String, Object>> chartData = dailyRevenue.entrySet().stream().map(entry -> {
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("date", entry.getKey());
            point.put("value", entry.getValue());
            return point;
        }).toList();

        long totalSuccess = transactions.stream()
                .filter(t -> t.getStatus() == Status.PAYMENT_SUCCESS)
                .count();

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalTransactions", transactions.size());
        summary.put("successTransactions", totalSuccess);
        summary.put("failedTransactions", transactions.size() - totalSuccess);
        summary.put("totalRevenue", dailyRevenue.values().stream().mapToDouble(Double::doubleValue).sum());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("summary", summary);
        response.put("chart", chartData);
        response.put("table", tableData);

        return ResponseBuilder.build(HttpStatus.OK, "", response);
    }

    @Override
    public ResponseEntity<ResponseObject> publishServicePackageFee(Integer packageId) {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Tài khoản của bạn đang bị hạn chế.", null);
        }

        Subscription subscription = subscriptionRepo.findById(packageId).orElse(null);

        if (subscription == null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Không tìm thấy gói đăng ký (subscription).", null);
        }

        if (!subscription.getPackageStatus().equals(Status.PACKAGE_DRAFT)) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Chỉ có thể phát hành gói ở trạng thái nháp (DRAFT). Trạng thái hiện tại: " + subscription.getPackageStatus(), null);
        }

        if (subscription.getDurationDays() <= 0) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Không thể phát hành: thời hạn gói phải lớn hơn 0 ngày.", null);
        }

        if (subscription.getFeatures() == null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Không thể phát hành: gói phải có danh sách tính năng.", null);
        }

        subscription.setPackageStatus(Status.PACKAGE_ACTIVE);
        subscriptionRepo.save(subscription);

        notificationService.publish(
                NotificationEventType.CREATE_PACKAGE_FEE,
                null,
                buildCreatePackageNotificationContext(subscription)
        );


        return ResponseBuilder.build(HttpStatus.OK, "Phát hành gói đăng ký thành công", null);
    }

    private Map<String, Object> buildCreatePackageNotificationContext(Subscription sub) {
        Map<String, Object> contextData = new HashMap<>();
        if (sub == null) {
            return contextData;
        }
        contextData.put("packageName", sub.getName().trim());
        contextData.put("price", sub.getPrice());
        contextData.put("duration", sub.getDurationDays());
        return contextData;
    }

    @Override
    public ResponseEntity<ResponseObject> deActiveServicePackageFee(Integer packageId) {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Tài khoản của bạn đang bị hạn chế.", null);
        }

        Subscription subscription = subscriptionRepo.findById(packageId).orElse(null);
        if (subscription == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Không tìm thấy gói dịch vụ.", null);
        }

        if (subscription.getPackageStatus() == Status.PACKAGE_DEACTIVATED) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Gói đã được hủy kích hoạt trước đó.", null);
        }

        boolean hasActiveSchools = schoolSubscriptionRepo.existsBySubscriptionAndEndDateAfter(
                subscription,
                LocalDate.now()
        );

        if (hasActiveSchools) {
            subscription.setPackageStatus(Status.PACKAGE_INACTIVE_PENDING);
        } else {
            subscription.setPackageStatus(Status.PACKAGE_DEACTIVATED);
        }

        subscriptionRepo.save(subscription);

        return ResponseBuilder.build(HttpStatus.OK, hasActiveSchools ? "Gói đang được sử dụng; đã chuyển sang trạng thái chờ hủy và ẩn với người đăng ký mới." : "Hủy kích hoạt gói thành công.", null);
    }

    @Override
    public ResponseEntity<ResponseObject> createPersonalityType(CreatePersonalityTypeRequest request) {

        String error = validateCreatePersonalityType(request);

        if (!error.isEmpty()) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, error, null);
        }

        if (personalityTypeRepo.existsByCode(request.getCode())) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Mã loại tính cách đã tồn tại trong hệ thống.", null);
        }

        PersonalityTypeGroup group;

        try {
            group = parsePersonalityTypeGroup(request.getPersonalityTypeGroup());
        } catch (IllegalArgumentException e) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, e.getMessage(), null);
        }

        PersonalityType personalityType = PersonalityType.builder()
                .personalityTypeGroup(group)
                .name(normalize(request.getName()))
                .description(normalize(request.getDescription()))
                .code(normalize(request.getCode()))
                .quote(request.getQuoteInfo())
                .traits(request.getTraits())
                .strengths(request.getStrengths())
                .weaknesses(request.getWeaknesses())
                .sources(request.getSources())
                .recommendedCareers(request.getRecommendedCareers())
                .build();

        personalityTypeRepo.save(personalityType);

        return ResponseBuilder.build(HttpStatus.OK, "Tạo loại tính cách thành công", null);
    }

    @Override
    public ResponseEntity<ResponseObject> getPersonalityTypeList() {

        List<PersonalityType> personalityTypes = personalityTypeRepo.findAll();

        Map<String, List<PersonalityType>> result = new LinkedHashMap<>();

        for (PersonalityType p : personalityTypes) {
            String group = p.getPersonalityTypeGroup().getValue();
            result
                    .computeIfAbsent(group, k -> new ArrayList<>())
                    .add(p);
        }

        return ResponseBuilder.build(HttpStatus.OK, "Lấy danh sách loại tính cách thành công", result);
    }

    @Override
    public ResponseEntity<ResponseObject> createSubject(AddSubjectRequest request) {

        String error = validateAddSubjectInfo(request);

        if (!error.isEmpty()) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, error, null);
        }

        SubjectType subjectType;

        try {
            subjectType = parseSubjectType(request.getSubjectType());
        } catch (IllegalArgumentException e) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, e.getMessage(), null);
        }

        if (subjectRepo.existsByName((normalize(request.getName())))) {
            return ResponseBuilder.build(HttpStatus.CONFLICT, "Môn học đã tồn tại trong hệ thống.", null);
        }

        Subject subject = Subject.builder()
                .type(subjectType)
                .name(normalize(request.getName()))
                .build();

        subjectRepo.save(subject);

        return ResponseBuilder.build(HttpStatus.OK, "Tạo môn học thành công", subject);
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
                "Lấy danh sách môn học thành công",
                result
        );
    }

    @Override
    public ResponseEntity<ResponseObject> uploadTemplateDocxTemplate(MultipartFile file, String categoryTemplate) {

        CategoryTemplate categoryTempl;

        try {
            categoryTempl = parseCategoryTemplate(categoryTemplate);
        } catch (Exception ex) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
        }

        PlatformConfig media = platformConfigRepo.findByKey("media").orElse(null);
        if (media == null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Không tìm thấy cấu hình media", null);
        }

        try {
            ConfigSystemUtil.validateFileSize(media, file, false);
            ConfigSystemUtil.validateFileFormat(media, file, false);
        } catch (RuntimeException ex) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
        }

        TemplateDocx docx = TemplateDocx.builder()
                .type(categoryTempl)
                .createdDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .build();

        Optional<TemplateDocx> templateDocx = templateDocxRepo.findTopByTypeOrderByVersionDesc(categoryTempl);

        if (templateDocx.isEmpty()) {
            docx.setFolderName("TEMPLATE/" + categoryTempl.name() + "_" + UUID.randomUUID());
            docx.setFileName(categoryTempl.getValue() + "_v0");
            docx.setVersion(0);
        } else {
            TemplateDocx latestTemplate = templateDocx.get();
            int newVersion = latestTemplate.getVersion() + 1;
            docx.setFileName(categoryTempl.getValue() + "_v" + newVersion);
            docx.setVersion(newVersion);
            docx.setFolderName(latestTemplate.getFolderName());
        }

        try {

            Map<String, String> result = supabaseStorageService.uploadDocument(file, docx.getFolderName(), docx.getFileName(), List.of("docx"));
            docx.setFileUrl(result.get("fileUrl"));
            docx.setFileName(result.get("fileName"));
            templateDocxRepo.save(docx);

        } catch (IllegalArgumentException ex) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
        } catch (Exception ex) {
            return ResponseBuilder.build(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), null);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("fileUrl", docx.getFileUrl());
        result.put("fileName", docx.getFileName());


        return ResponseBuilder.build(HttpStatus.OK, "Tải lên thành công", result);

    }

    @Transactional
    @Override
    public ResponseEntity<ResponseObject> removeTemplateDocx(long templateDocxId) {


        Optional<TemplateDocx> templateDocx = templateDocxRepo.findById(templateDocxId);

        if (templateDocx.isEmpty()) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Không tìm thấy mẫu docx.", null);
        }

        long totalByType = templateDocxRepo.countByType((templateDocx.get().getType()));

        if (totalByType == 1) {
            return ResponseBuilder.build(
                    HttpStatus.BAD_REQUEST,
                    "Không thể xóa mẫu docx cuối cùng của loại này.",
                    null
            );
        }

        try {
            supabaseStorageService.removeFile(templateDocx.get().getFolderName(), templateDocx.get().getFileName());
        } catch (Exception ex) {
            return ResponseBuilder.build(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), null);
        }

        templateDocxRepo.deleteAllByIdInBatch(List.of(templateDocxId));
        templateDocxRepo.flush();

        return ResponseBuilder.build(HttpStatus.OK, "Xóa mẫu docx thành công", null);

    }

    @Override
    public ResponseEntity<ResponseObject> getTemplateDocs(String categoryTemplate) {

        CategoryTemplate categoryTempl;

        try {
            categoryTempl = parseCategoryTemplate(categoryTemplate);
        } catch (Exception ex) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
        }

        List<TemplateDocx> templateDocs = templateDocxRepo.findByTypeOrderByVersionDesc(categoryTempl);

        List<Map<String, Object>> result = templateDocs.stream()
                .map(this::buildTemplateDocx)
                .toList();

        return ResponseBuilder.build(HttpStatus.OK, "Lấy danh sách mẫu tài liệu thành công", result);
    }

    private Map<String, Object> buildTemplateDocx(TemplateDocx templateDocx) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", templateDocx.getId());
        result.put("fileName", templateDocx.getFileName());
        result.put("fileUrl", templateDocx.getFileUrl());
        return result;
    }

    private String validateAddSubjectInfo(AddSubjectRequest request) {

        if (isBlank(request.getName())) {
            return "Tên môn học không được để trống.";
        }
        if (request.getSubjectType() == null || request.getSubjectType().isBlank()) {
            return "Loại môn không được để trống.";
        }
        return "";
    }

    private SubjectType parseSubjectType(String subjectType) {
        try {
            return SubjectType.valueOf(subjectType.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Loại môn không hợp lệ. Giá trị cho phép: REGULAR_SUBJECT, FOREIGN_LANGUAGE_SUBJECT."
            );
        }
    }

    private CategoryTemplate parseCategoryTemplate(String categoryTemplate) {

        if (categoryTemplate == null || categoryTemplate.isEmpty()) {
            throw new IllegalArgumentException("Danh mục mẫu không được để trống.");
        }

        try {
            return CategoryTemplate.valueOf(categoryTemplate.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Danh mục mẫu không hợp lệ. Giá trị cho phép: CAMPUS_INF0_TEMPLATE, SCHOOL_INFO_TEMPLATE."
            );
        }
    }

    private PersonalityTypeGroup parsePersonalityTypeGroup(String group) {
        if (group == null || group.isBlank()) {
            throw new IllegalArgumentException("Nhóm loại tính cách không được để trống.");
        }

        try {
            return PersonalityTypeGroup.valueOf(group.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Nhóm loại tính cách không hợp lệ. Giá trị cho phép: ANALYST, DIPLOMAT, SENTINEL, EXPLORER."
            );
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }


    private String validateCreatePersonalityType(CreatePersonalityTypeRequest request) {

        if (request == null) {
            return "Yêu cầu không được để trống.";
        }

        if (isBlank(request.getCode())) {
            return "Mã loại tính cách không được để trống.";
        }

        if (isBlank(request.getName())) {
            return "Tên loại tính cách không được để trống.";
        }

        if (isBlank(request.getDescription())) {
            return "Mô tả không được để trống.";
        }

        if (request.getQuoteInfo() == null) {
            return "Thông tin trích dẫn (QuoteInfo) không được để trống.";
        }

        if (isBlank(request.getQuoteInfo().getAuthor())) {
            return "Tác giả trích dẫn không được để trống.";
        }

        if (isBlank(request.getQuoteInfo().getContent())) {
            return "Nội dung trích dẫn không được để trống.";
        }

        if (request.getTraits().size() != 4) {
            return
                    "Danh sách đặc điểm (traits) phải đúng 4 phần tử.";
        }

        for (int i = 0; i < request.getTraits().size(); i++) {
            if (isBlank(request.getTraits().get(i).getName())) {
                return "Tên đặc điểm tại vị trí [" + i + "] không được để trống.";
            }
            if (isBlank(request.getTraits().get(i).getDescription())) {
                return "Mô tả đặc điểm tại vị trí [" + i + "] không được để trống.";
            }
        }

        if (request.getStrengths().isEmpty()) {
            return "Danh sách điểm mạnh không được để trống.";
        }

        for (int j = 0; j < request.getStrengths().size(); j++) {
            if (isBlank(request.getStrengths().get(j))) {
                return "Điểm mạnh tại vị trí [" + j + "] không được để trống.";
            }
        }

        for (int i = 0; i < request.getWeaknesses().size(); i++) {
            if (isBlank(request.getWeaknesses().get(i))) {
                return "Điểm yếu tại vị trí [" + i + "] không được để trống.";
            }
        }

        for (int i = 0; i < request.getSources().size(); i++) {
            if (isBlank(request.getSources().get(i).getTitle())) {
                return "Tên hiển thị nguồn tại vị trí [" + i + "] không được để trống.";
            }
            if (isBlank(request.getSources().get(i).getUrl())) {
                return "URL nguồn tại vị trí [" + i + "] không được để trống.";
            }
        }

        if (request.getRecommendedCareers().isEmpty()) {
            return "Danh sách nghề nghiệp gợi ý không được để trống.";
        }

        for (int j = 0; j < request.getRecommendedCareers().size(); j++) {
            if (isBlank(request.getRecommendedCareers().get(j).getName())) {
                return "Tên nghề nghiệp gợi ý tại vị trí [" + j + "] không được để trống.";
            }
            if (isBlank(request.getRecommendedCareers().get(j).getExplainText())) {
                return "Giải thích nghề nghiệp gợi ý tại vị trí [" + j + "] không được để trống.";
            }
        }

        return "";
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    @Override
    public ResponseEntity<ResponseObject> getRevenuesSummary(LocalDate startDate, LocalDate endDate, String packageType) {

        if (endDate == null) endDate = LocalDate.now();
        if (startDate == null) startDate = endDate.minusYears(5);

        if (endDate.isBefore(startDate)) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Ngày kết thúc không được trước ngày bắt đầu", null);
        }

        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
        DateTimeFormatter periodFormatter;
        String scope;

        if (daysBetween <= 45) {
            periodFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            scope = "DAY";
        } else if (daysBetween <= 365 * 2) { // Dưới 2 năm thì xem theo tháng
            periodFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
            scope = "MONTH";
        } else { // Trên 2 năm (ví dụ 5 năm) thì xem theo năm cho thoáng biểu đồ
            periodFormatter = DateTimeFormatter.ofPattern("yyyy");
            scope = "YEAR";
        }

        String normalizedPackageType = (packageType == null || packageType.isBlank())
                ? null
                : packageType.trim().toUpperCase(Locale.ROOT);

        if (normalizedPackageType != null) {
            try {
                PackageType.valueOf(normalizedPackageType);
            } catch (IllegalArgumentException ex) {
                return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Loại gói không hợp lệ. Vui lòng dùng TRIAL, STANDARD hoặc ENTERPRISE.", null);
            }
        }

        Map<String, RevenueAccumulator> trendAccumulator = new TreeMap<>();
        LocalDate temp = startDate;
        while (!temp.isAfter(endDate)) {
            trendAccumulator.put(temp.format(periodFormatter), new RevenueAccumulator());
            if (scope.equals("DAY")) temp = temp.plusDays(1);
            else if (scope.equals("MONTH")) temp = temp.plusMonths(1);
            else temp = temp.plusYears(1);
        }

        LocalDateTime fromAt = startDate.atStartOfDay();
        LocalDateTime toAt = endDate.atTime(LocalTime.MAX);
        List<PaymentTransaction> transactions = paymentTransactionRepo.findAll(
                buildRevenueSpecification(fromAt, toAt, normalizedPackageType)
        );

        BigDecimal totalNetRevenue = BigDecimal.ZERO;
        BigDecimal totalServiceFee = BigDecimal.ZERO;
        BigDecimal totalTaxFee = BigDecimal.ZERO;
        BigDecimal totalFinalRevenue = BigDecimal.ZERO;
        long countedTransactions = 0L;

        for (PaymentTransaction transaction : transactions) {
            Subscription subscription = (transaction.getSchoolSubscription() == null)
                    ? null
                    : transaction.getSchoolSubscription().getSubscription();

            BigDecimal netRevenue = subscription == null ? BigDecimal.ZERO : safeMoney(subscription.getPrice());
            BigDecimal serviceFee = subscription == null ? BigDecimal.ZERO : safeMoney(subscription.getServiceFee());
            BigDecimal taxFee = subscription == null ? BigDecimal.ZERO : safeMoney(subscription.getTaxFee());
            BigDecimal finalRevenue = resolveFinalRevenue(subscription, transaction);

            // Bỏ qua rác (nếu cần)
            if (finalRevenue.signum() == 0 && netRevenue.signum() == 0) continue;

            // Cộng dồn tổng quát
            totalNetRevenue = totalNetRevenue.add(netRevenue);
            totalServiceFee = totalServiceFee.add(serviceFee);
            totalTaxFee = totalTaxFee.add(taxFee);
            totalFinalRevenue = totalFinalRevenue.add(finalRevenue);
            countedTransactions++;

            String periodKey = transaction.getUpdatedAt().format(periodFormatter);
            RevenueAccumulator acc = trendAccumulator.get(periodKey);
            if (acc != null) {
                acc.netRevenue = acc.netRevenue.add(netRevenue);
                acc.serviceFee = acc.serviceFee.add(serviceFee);
                acc.taxFee = acc.taxFee.add(taxFee);
                acc.finalRevenue = acc.finalRevenue.add(finalRevenue);
                acc.transactions++;
            }
        }

        Map<String, Object> totals = new LinkedHashMap<>();
        totals.put("totalNetRevenue", totalNetRevenue);
        totals.put("totalServiceFee", totalServiceFee);
        totals.put("totalTaxFee", totalTaxFee);
        totals.put("totalFinalRevenue", totalFinalRevenue);
        totals.put("totalTransactions", countedTransactions);

        List<Map<String, Object>> trend = trendAccumulator.entrySet().stream().map(entry -> {
            RevenueAccumulator row = entry.getValue();
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("period", entry.getKey());
            point.put("netRevenue", row.netRevenue);
            point.put("serviceFee", row.serviceFee);
            point.put("taxFee", row.taxFee);
            point.put("finalRevenue", row.finalRevenue);
            point.put("transactions", row.transactions);
            return point;
        }).toList();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("startDate", startDate);
        data.put("endDate", endDate);
        data.put("packageType", normalizedPackageType);
        data.put("scope", scope);
        data.put("dataPoints", trend.size());
        data.put("totals", totals);
        data.put("trend", trend);

        return ResponseBuilder.build(HttpStatus.OK, "Lấy thống kê doanh thu thành công", data);
    }

    @Override
    public ResponseEntity<ResponseObject> getDashboardOverview(Integer year) {
        if (year == null || year < 2000 || year > 3000) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Năm không hợp lệ", null);
        }

        long activeSchools = schoolRepo.count();

        Map<String, Long> usersByRole = new LinkedHashMap<>();
        usersByRole.put(Role.PARENT.name(), 0L);
        usersByRole.put(Role.COUNSELLOR.name(), 0L);
        usersByRole.put(Role.SCHOOL.name(), 0L);

        for (Account account : accountRepo.findAll()) {
            if (account == null || account.getRole() == null) {
                continue;
            }
            String roleName = account.getRole().name();
            if (usersByRole.containsKey(roleName)) {
                usersByRole.put(roleName, usersByRole.get(roleName) + 1);
            }
        }

        List<SchoolRegistrationRequest> registrationRequests = schoolRegistrationRequestRepo.findAll();

        Map<Integer, Long> growthByMonth = new HashMap<>();
        long verifiedCount = 0;
        long unverifiedCount = 0;

        for (SchoolRegistrationRequest request : registrationRequests) {
            if (request == null) {
                continue;
            }

            LocalDateTime createdAt = request.getCreatedAt();
            if (createdAt != null && createdAt.getYear() == year) {
                int month = createdAt.getMonthValue();
                growthByMonth.put(month, growthByMonth.getOrDefault(month, 0L) + 1);
            }

            if (request.getStatus() == Status.VERIFIED) {
                verifiedCount++;
            } else {
                unverifiedCount++;
            }
        }

        long totalVerification = verifiedCount + unverifiedCount;
        BigDecimal verifiedRate = totalVerification == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(verifiedCount)
                .divide(BigDecimal.valueOf(totalVerification), 4, RoundingMode.HALF_UP);

        Map<String, Object> verificationStatus = new LinkedHashMap<>();
        verificationStatus.put("verified", verifiedCount);
        verificationStatus.put("unverified", unverifiedCount);
        verificationStatus.put("verifiedRate", verifiedRate);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("year", year);
        data.put("activeSchools", activeSchools);
        data.put("usersByRole", usersByRole);
        data.put("verificationStatus", verificationStatus);

        return ResponseBuilder.build(HttpStatus.OK, "Lấy thống kê tổng quát thành công", data);
    }

    private Specification<PaymentTransaction> buildRevenueSpecification(LocalDateTime fromAt,
                                                                        LocalDateTime toAt,
                                                                        String packageType) {
        return (root, query, cb) -> {
            root.fetch("schoolSubscription", JoinType.INNER).fetch("subscription", JoinType.INNER);
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("status"), Status.PAYMENT_SUCCESS));
            predicates.add(cb.greaterThanOrEqualTo(root.get("updatedAt"), fromAt));
            predicates.add(cb.lessThan(root.get("updatedAt"), toAt));

            if (packageType != null) {
                predicates.add(cb.equal(
                        root.get("schoolSubscription").get("subscription").get("packageType"),
                        PackageType.valueOf(packageType)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private BigDecimal safeMoney(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal resolveFinalRevenue(Subscription subscription, PaymentTransaction transaction) {
        if (subscription != null) {
            return subscription.getFinalPrice();
        }

        return BigDecimal.valueOf(transaction.getVnpAmount()).movePointLeft(2);

    }

    private static class RevenueAccumulator {
        private BigDecimal netRevenue = BigDecimal.ZERO;
        private BigDecimal serviceFee = BigDecimal.ZERO;
        private BigDecimal taxFee = BigDecimal.ZERO;
        private BigDecimal finalRevenue = BigDecimal.ZERO;
        private long transactions = 0L;
    }

}
