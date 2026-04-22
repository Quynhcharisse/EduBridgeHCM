package com.sp26se041.edubridgehcm.services.implementors;

import com.sp26se041.edubridgehcm.enums.*;
import com.sp26se041.edubridgehcm.models.*;
import com.sp26se041.edubridgehcm.repositories.*;
import com.sp26se041.edubridgehcm.requests.AddSubjectRequest;
import com.sp26se041.edubridgehcm.requests.AutoFillQuotasByYearRequest;
import com.sp26se041.edubridgehcm.requests.CreatePersonalityTypeRequest;
import com.sp26se041.edubridgehcm.requests.UpsertServicePackageFeeRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.AdminService;
import com.sp26se041.edubridgehcm.services.SupabaseStorageService;
import com.sp26se041.edubridgehcm.utils.AccountRestrictionUtil;
import com.sp26se041.edubridgehcm.utils.AuthRequestUtil;
import com.sp26se041.edubridgehcm.utils.ConfigSystemUtil;
import com.sp26se041.edubridgehcm.utils.ResponseBuilder;
import com.sp26se041.edubridgehcm.validations.admin.SubscriptionValidation;
import com.sp26se041.edubridgehcm.validations.admin.VerifyRegistrationValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
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
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

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
                    new TypeReference<>() {}
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
                        new TypeReference<>() {}
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
        }

        try {

            Map<String, Object> fields = new LinkedHashMap<>();
            fields.put("name", request.getSchoolName().trim());
            fields.put("description", request.getDescription().trim());
            fields.put("taxCode", request.getTaxCode().trim());
            fields.put("websiteUrl", request.getWebsiteUrl());
            fields.put("representativeName", request.getRepresentativeName());
            fields.put("hotline", request.getHotline());
            fields.put("foundingDate", request.getFoundingDate()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            fields.put("logoUrl", request.getLogoUrl());
            fields.put("businessLicenseUrl", newBusinessLicenseUrl);
            fields.put("overview", "UPDATING");

            String schoolName = toSafeObjectKey(request.getSchoolName());
            String folderName = schoolName + "_" + uuid;
            String fileName = "school_info_" + uuid + ".docx";

            String templatePath = schoolTemplateDocx.get().getFolderName() + "/" + schoolTemplateDocx.get().getFileName();

            schoolFileUrl = supabaseStorageService.generateDocFileFromTemplate(fields, templatePath, folderName, fileName);

        } catch (Exception ex) {
            return ResponseBuilder.build(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), null);
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

        if (!schoolFileUrl.isEmpty()) {
            try {
                Map<String, Object> payload = new LinkedHashMap<>();
                payload.put("type", "school_info");
                payload.put("schoolId", school.getId());
                payload.put("schoolName", request.getSchoolName().trim());
                payload.put("schoolInfoFileUrl", schoolFileUrl);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

                restTemplate.postForEntity(
                        n8nUrl,
                        entity,
                        String.class
                );

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
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

        Subscription subscription;

        String error = SubscriptionValidation.upsertSubscriptionValidation(request);

        if (error != null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, error, null);
        }

        boolean isCreate = (request.getPackageId() == null);

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
        subscription.setPackageType(PackageType.valueOf(request.getPackageType()));
        subscription.setDescription(request.getDescription());
        subscription.setDurationDays(request.getDurationDays());

        if (request.getFeatureData() != null) {
            subscription.setFeatures(buildFeatureJson(request.getFeatureData()));
        }

        // Tính giá từ platform_config "business", gán price / phí / thuế / finalPrice
        PlatformConfig businessConfig = platformConfigRepo.findByKey("business").orElse(null);
        
        if (businessConfig == null || businessConfig.getValue() == null) {
            return ResponseBuilder.build(HttpStatus.INTERNAL_SERVER_ERROR, "Chưa cấu hình doanh nghiệp.", null);
        }

        if (!(businessConfig.getValue() instanceof Map<?, ?> businessRaw)) {
            return ResponseBuilder.build(HttpStatus.INTERNAL_SERVER_ERROR, "Cấu hình doanh nghiệp không hợp lệ.", null);
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> businessMap = (Map<String, Object>) businessRaw;

        ConfigSystemUtil.SubscriptionPriceBreakdown breakdown;

        try {
            //tính giá từ platform_config "business"
            breakdown = ConfigSystemUtil.calculateSubscriptionPriceBreakdown(request, businessMap);
        } catch (RuntimeException ex) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
        }

        subscription.setPrice(breakdown.netPrice());
        subscription.setServiceFee(breakdown.serviceFee());
        subscription.setTaxFee(breakdown.taxFee());
        subscription.setFinalPrice(breakdown.finalPrice());

        subscriptionRepo.save(subscription);

        return ResponseBuilder.build(isCreate ? HttpStatus.CREATED : HttpStatus.OK, isCreate ? "Tạo gói nháp thành công" : "Cập nhật gói nháp thành công", null);
    }

    private Map<String, Object> buildFeatureJson(UpsertServicePackageFeeRequest.FeatureData request) {
        Map<String, Object> data = new HashMap<>();
        data.put("maxCounsellors", request.getMaxCounsellors());
        data.put("hasAiAssistant", request.getHasAiAssistant());
        data.put("parentPostPermission", request.getParentPostPermission());
        data.put("isFeatured", request.getIsFeatured());
        data.put("topRanking", request.getTopRanking());
        data.put("supportLevel", SupportLevel.valueOf(request.getSupportLevel()));
        return data;
    }

    @Override
    public ResponseEntity<ResponseObject> viewServicePackageFeeList() {

        Account currentAccount = AuthRequestUtil.extractAuthenticatedAccount();

        List<Subscription> subscriptions;

        if (currentAccount != null && Role.ADMIN == currentAccount.getRole()) {
            subscriptions = subscriptionRepo.findAll();
        } else {
            subscriptions = subscriptionRepo.findAllByPackageStatus(Status.PACKAGE_ACTIVE);
        }

        if (subscriptions.isEmpty()) {
            return ResponseBuilder.build(HttpStatus.OK, "Không có gói dịch vụ nào", Collections.emptyList());
        }

        List<Map<String, Object>> data = subscriptions.stream()
                .map(this::buildSubscriptionData)
                .collect(Collectors.toList());

        return ResponseBuilder.build(HttpStatus.OK, "Lấy danh sách phí gói thành công", data);
    }

    private Map<String, Object> buildSubscriptionData(Subscription subscription) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", subscription.getId());
        data.put("name", subscription.getName());
        data.put("description", subscription.getDescription());
        data.put("packageType", subscription.getPackageType());
        data.put("price", subscription.getPrice());
        data.put("serviceFee", subscription.getServiceFee());
        data.put("taxFee", subscription.getTaxFee());
        data.put("finalPrice", subscription.getFinalPrice());
        data.put("durationDays", subscription.getDurationDays());
        data.put("status", subscription.getPackageStatus());
        data.put("features", subscription.getFeatures());
        return data;
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

        if (subscription.getPrice().compareTo(BigDecimal.ZERO)<= 0) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Không thể phát hành: giá phải lớn hơn 0.", null);
        }

        if (subscription.getDurationDays() <= 0) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Không thể phát hành: thời hạn gói phải lớn hơn 0 ngày.", null);
        }

        if (subscription.getFeatures() == null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Không thể phát hành: gói phải có danh sách tính năng.", null);
        }

        subscription.setPackageStatus(Status.PACKAGE_ACTIVE);
        subscriptionRepo.save(subscription);

        return ResponseBuilder.build(HttpStatus.OK, "Phát hành gói đăng ký thành công", null);
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

        long totalTemplates = templateDocxRepo.count();

        if (totalTemplates == 1) {
            return ResponseBuilder.build(
                    HttpStatus.BAD_REQUEST,
                    "Không thể xóa mẫu docx cuối cùng trong hệ thống.",
                    null
            );
        }

        Optional<TemplateDocx> templateDocx = templateDocxRepo.findById(templateDocxId);

        if (templateDocx.isEmpty()) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Không tìm thấy mẫu docx.", null);
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

}
