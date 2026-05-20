package com.sp26se041.edubridgehcm.services.implementors;

import com.sp26se041.edubridgehcm.enums.BoardingType;
import com.sp26se041.edubridgehcm.enums.CategoryTemplate;
import com.sp26se041.edubridgehcm.enums.NotificationEventType;
import com.sp26se041.edubridgehcm.enums.OfferingProgramAction;
import com.sp26se041.edubridgehcm.enums.ResourceType;
import com.sp26se041.edubridgehcm.enums.Role;
import com.sp26se041.edubridgehcm.enums.SessionType;
import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.models.Account;
import com.sp26se041.edubridgehcm.models.AdmissionCampaign;
import com.sp26se041.edubridgehcm.models.AdmissionReservationForm;
import com.sp26se041.edubridgehcm.models.Campus;
import com.sp26se041.edubridgehcm.models.CampusProgramOffering;
import com.sp26se041.edubridgehcm.models.CampusResourceQuota;
import com.sp26se041.edubridgehcm.models.CampusScheduleTemplate;
import com.sp26se041.edubridgehcm.models.ChatMessage;
import com.sp26se041.edubridgehcm.models.ConsultationOfflineRequest;
import com.sp26se041.edubridgehcm.models.Conversation;
import com.sp26se041.edubridgehcm.models.Counsellor;
import com.sp26se041.edubridgehcm.models.CounsellorSlot;
import com.sp26se041.edubridgehcm.models.Parent;
import com.sp26se041.edubridgehcm.models.Program;
import com.sp26se041.edubridgehcm.models.School;
import com.sp26se041.edubridgehcm.models.SchoolConfig;
import com.sp26se041.edubridgehcm.models.SchoolHoliday;
import com.sp26se041.edubridgehcm.models.SchoolSubscription;
import com.sp26se041.edubridgehcm.models.StudentProfile;
import com.sp26se041.edubridgehcm.models.TemplateDocx;
import com.sp26se041.edubridgehcm.repositories.AccountRepo;
import com.sp26se041.edubridgehcm.repositories.AdmissionCampaignRepo;
import com.sp26se041.edubridgehcm.repositories.AdmissionReservationFormRepo;
import com.sp26se041.edubridgehcm.repositories.CampusProgramOfferingRepo;
import com.sp26se041.edubridgehcm.repositories.CampusRepo;
import com.sp26se041.edubridgehcm.repositories.CampusResourceQuotaRepo;
import com.sp26se041.edubridgehcm.repositories.CampusScheduleTemplateRepo;
import com.sp26se041.edubridgehcm.repositories.ChatMessageRepo;
import com.sp26se041.edubridgehcm.repositories.ConsultationOfflineRequestRepo;
import com.sp26se041.edubridgehcm.repositories.ConversationRepo;
import com.sp26se041.edubridgehcm.repositories.CounsellorRepo;
import com.sp26se041.edubridgehcm.repositories.CounsellorSlotRepo;
import com.sp26se041.edubridgehcm.repositories.PlatformConfigRepo;
import com.sp26se041.edubridgehcm.repositories.ProgramRepo;
import com.sp26se041.edubridgehcm.repositories.SchoolConfigRepo;
import com.sp26se041.edubridgehcm.repositories.SchoolHolidayRepo;
import com.sp26se041.edubridgehcm.repositories.SchoolSubscriptionRepo;
import com.sp26se041.edubridgehcm.repositories.TemplateDocxRepo;
import com.sp26se041.edubridgehcm.requests.AssignCounsellorIntoSlotsRequest;
import com.sp26se041.edubridgehcm.requests.CampusScheduleTemplateRequest;
import com.sp26se041.edubridgehcm.requests.ChatMessageForChatBot;
import com.sp26se041.edubridgehcm.requests.ConfirmPaymentDepositRequest;
import com.sp26se041.edubridgehcm.requests.CreateAccountCounsellorRequest;
import com.sp26se041.edubridgehcm.requests.CreateCampusProgramOfferingRequest;
import com.sp26se041.edubridgehcm.requests.ProcessApplicantRequest;
import com.sp26se041.edubridgehcm.requests.UpdateCampusConfigRequest;
import com.sp26se041.edubridgehcm.requests.UpdateCampusProgramOfferingRequest;
import com.sp26se041.edubridgehcm.responses.PageResponse;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.responses.StorageTreeNode;
import com.sp26se041.edubridgehcm.services.CampusService;
import com.sp26se041.edubridgehcm.services.NotificationService;
import com.sp26se041.edubridgehcm.services.SupabaseStorageService;
import com.sp26se041.edubridgehcm.utils.AccountRestrictionUtil;
import com.sp26se041.edubridgehcm.utils.AuthRequestUtil;
import com.sp26se041.edubridgehcm.utils.CheckCampusOfferingStatus;
import com.sp26se041.edubridgehcm.utils.ExcelUtil;
import com.sp26se041.edubridgehcm.utils.PaginationUtil;
import com.sp26se041.edubridgehcm.utils.ResourceCheckerUtil;
import com.sp26se041.edubridgehcm.utils.ResponseBuilder;
import com.sp26se041.edubridgehcm.utils.SchoolConfigUtil;
import com.sp26se041.edubridgehcm.validations.campus.CampusProgramOfferingValidation;
import com.sp26se041.edubridgehcm.validations.campus.CampusScheduleTemplateValidation;
import com.sp26se041.edubridgehcm.validations.campus.CounsellorSlotValidation;
import com.sp26se041.edubridgehcm.validations.campus.CounsellorValidation;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeSet;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CampusServiceImpl implements CampusService {

    private final SchoolSubscriptionRepo schoolSubscriptionRepo;
    private final PlatformConfigRepo platformConfigRepo;

    @Value("${AI_SERVICE_N8N}")
    private String n8nUrl;

    private final CampusRepo campusRepo;

    private final AdmissionCampaignRepo admissionCampaignRepo;

    private final CampusProgramOfferingRepo campusProgramOfferingRepo;

    private final ProgramRepo programRepo;

    private final AccountRepo accountRepo;

    private final CounsellorRepo counsellorRepo;

    private final AdmissionReservationFormRepo admissionReservationFormRepo;

    private final SchoolConfigRepo schoolConfigRepo;

    private final CampusScheduleTemplateRepo campusScheduleTemplateRepo;

    private final CounsellorSlotRepo counsellorSlotRepo;

    private final ConsultationOfflineRequestRepo consultationOfflineRequestRepo;

    private final CampusResourceQuotaRepo campusResourceQuotaRepo;

    private final SupabaseStorageService supabaseStorageService;

    private final ConversationRepo conversationRepo;

    private final ChatMessageRepo chatMessageRepo;

    private final SchoolHolidayRepo schoolHolidayRepo;

    private final TemplateDocxRepo templateDocxRepo;

    private final NotificationService notificationService;

    private final EntityManager entityManager;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    @Transactional
    public ResponseEntity<ResponseObject> createCampusProgramOffering(CreateCampusProgramOfferingRequest request) {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Tài khoản của bạn đang bị hạn chế, không thể thực hiện thao tác này.", null);
        }

        Campus actorCampus = extractActorCampus();
        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản cơ sở trường học hoặc bạn không có quyền truy cập.", null);
        }

        if (request.getCampusId() != null && !request.getCampusId().equals(actorCampus.getId())) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Bạn không có quyền tạo chương trình tuyển sinh cho cơ sở khác.", null);
        }

        AdmissionCampaign campaign = admissionCampaignRepo.findById(request.getAdmissionCampaignId()).orElse(null);
        if (campaign == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Không tìm thấy chiến dịch tuyển sinh.", null);
        }

        if (!campaign.getSchool().getId().equals(actorCampus.getSchool().getId())) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Chiến dịch không thuộc trường của cơ sở này.", null);
        }

        if (campaign.getStatus() != Status.OPEN_ADMISSION_CAMPAIGN) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST,
                    "Chỉ được tạo gói tuyển sinh khi chiến dịch đang ở trạng thái Mở.", null);
        }

        if (!(campaign.getAdmissionMethodTimelines() instanceof List<?> rawTimelines) || rawTimelines.isEmpty()) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST,
                    "Chiến dịch chưa có phương thức tuyển sinh nào.", null);
        }

        String requestedMethod = normalize(request.getMethodCode());
        if (requestedMethod == null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Vui lòng chọn phương thức tuyển sinh.", null);
        }

        // Tìm timeline entry khớp với methodCode campus chọn
        Map<?, ?> matchedTimeline = null;
        for (Object rawItem : rawTimelines) {
            if (!(rawItem instanceof Map<?, ?> m)) continue;
            if (requestedMethod.equals(normalize(Objects.toString(m.get("methodCode"), null)))) {
                matchedTimeline = m;
                break;
            }
        }
        if (matchedTimeline == null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST,
                    "Phương thức '" + requestedMethod + "' không tồn tại trong chiến dịch này.", null);
        }

        boolean allowReservation = !Boolean.FALSE.equals(matchedTimeline.get("allowReservationSubmission"));

        Program program = programRepo.findByIdAndCurriculum_School_Id(request.getProgramId(), actorCampus.getSchool().getId());
        if (program == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Không tìm thấy chương trình đào tạo.", null);
        }

        if (program.getStatus() != Status.PRO_ACTIVE) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST,
                    "Chương trình đào tạo chưa được kích hoạt (PRO_ACTIVE).", null);
        }

        Campus targetCampus = CampusProgramOfferingValidation.resolveTargetCampus(actorCampus, request.getCampusId(), campusRepo);
        if (targetCampus == null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Không xác định được cơ sở áp dụng offering.", null);
        }

        // Check duplicate: cùng campus + cùng program + cùng method trong 1 campaign
        boolean alreadyExists = campusProgramOfferingRepo
                .findByAdmissionCampaignId(campaign.getId())
                .stream()
                .anyMatch(o -> o.getCampus().getId().equals(targetCampus.getId())
                        && o.getProgram().getId().equals(program.getId())
                        && requestedMethod.equals(normalize(o.getAdmissionMethod())));
        if (alreadyExists) {
            return ResponseBuilder.build(HttpStatus.CONFLICT,
                    "Cơ sở đã có gói tuyển sinh cho chương trình này với phương thức '" + requestedMethod + "'.", null);
        }

        // Validate quota campus tự nhập
        Integer requestedQuota = request.getQuota();
        if (requestedQuota == null || requestedQuota <= 0) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Quota phải lớn hơn 0.", null);
        }

        // Quota được campus chính phân bổ cho campus này
        Integer allocatedQuota = resolveConfiguredCampusQuota(
                actorCampus.getSchool().getId(), targetCampus.getId(), campaign.getYear());
        if (allocatedQuota == null || allocatedQuota <= 0) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST,
                    "Chưa cấu hình quota cho cơ sở này trong chiến dịch năm " + campaign.getYear() + ".", null);
        }

        // Tổng quota các offering hiện có của campus trong campaign
        List<CampusProgramOffering> existingOfferings = campusProgramOfferingRepo
                .findByAdmissionCampaignId(campaign.getId())
                .stream()
                .filter(o -> o.getCampus().getId().equals(targetCampus.getId()))
                .toList();

        int usedQuota = existingOfferings.stream().mapToInt(CampusProgramOffering::getQuota).sum();
        int remainingCampusQuota = allocatedQuota - usedQuota;
        if (usedQuota + requestedQuota > allocatedQuota) {
            // Thêm thông tin rõ ràng: đã dùng của campus này, bạn đang thêm bao nhiêu, còn lại cho campus này
            // và nếu có quota của phương thức trong campaign thì hiển thị remaining toàn trường cho method đó
            int usedQuotaForMethod = campusProgramOfferingRepo
                    .findByAdmissionCampaignId(campaign.getId())
                    .stream()
                    .filter(o -> requestedMethod.equals(normalize(o.getAdmissionMethod())))
                    .mapToInt(CampusProgramOffering::getQuota)
                    .sum();

            int methodQuota = -1;
            Object methodQuotaRawForMsg = matchedTimeline != null ? matchedTimeline.get("quota") : null;
            if (methodQuotaRawForMsg != null) {
                try {
                    methodQuota = Integer.parseInt(methodQuotaRawForMsg.toString());
                } catch (NumberFormatException ignored) {
                }
            }

            String detailMsg;
            if (methodQuota > -1) {
                int remainingMethodQuota = Math.max(0, methodQuota - usedQuotaForMethod);
                detailMsg = String.format("Tổng quota vượt mức phân bổ cho cơ sở. Cơ sở đã dùng: %d, bạn đang thêm: %d, giới hạn cơ sở: %d (còn lại cho cơ sở này: %d). Phương thức '%s' toàn trường đã dùng %d/%d, còn lại %d.",
                        usedQuota, requestedQuota, allocatedQuota, Math.max(0, remainingCampusQuota),
                        requestedMethod, usedQuotaForMethod, methodQuota, remainingMethodQuota);
            } else {
                detailMsg = String.format("Tổng quota vượt mức phân bổ cho cơ sở. Cơ sở đã dùng: %d, bạn đang thêm: %d, giới hạn cơ sở: %d (còn lại cho cơ sở này: %d).",
                        usedQuota, requestedQuota, allocatedQuota, Math.max(0, remainingCampusQuota));
            }

            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, detailMsg, null);
        }

        // Validate quota theo từng phương thức tuyển sinh (không vượt quota PTTS trong campaign)
        // Đếm tổng quota đã dùng của TẤT CẢ campus cho method này trong campaign
        Object methodQuotaRaw = matchedTimeline.get("quota");
        if (methodQuotaRaw != null) {
            try {
                int methodQuota = Integer.parseInt(methodQuotaRaw.toString());
                int usedQuotaForMethod = campusProgramOfferingRepo
                        .findByAdmissionCampaignId(campaign.getId())
                        .stream()
                        .filter(o -> requestedMethod.equals(normalize(o.getAdmissionMethod())))
                        .mapToInt(CampusProgramOffering::getQuota)
                        .sum();
                if (usedQuotaForMethod + requestedQuota > methodQuota) {
                    return ResponseBuilder.build(HttpStatus.BAD_REQUEST,
                            String.format("Quota cho phương thức '%s' vượt giới hạn chiến dịch. Đã dùng: %d, Thêm: %d, Giới hạn PTTS: %d.",
                                    requestedMethod, usedQuotaForMethod, requestedQuota, methodQuota), null);
                }
            } catch (NumberFormatException ignored) {
            }
        }

        // Tính học phí
        BigDecimal basePrice = program.getBaseTuitionFee();
        BigDecimal adjustmentPercent = request.getPriceAdjustmentPercentage() != null
                ? new BigDecimal(request.getPriceAdjustmentPercentage().toString())
                : BigDecimal.ZERO;
        BigDecimal finalFee = basePrice
                .multiply(BigDecimal.ONE.add(adjustmentPercent))
                .setScale(0, RoundingMode.HALF_UP);

        // Validate chính sách học phí
        SchoolConfig financePolicyConfig = schoolConfigRepo
                .findBySchoolIdAndKey(actorCampus.getSchool().getId(), "financePolicyData")
                .orElse(null);

        if (financePolicyConfig != null && financePolicyConfig.getValue() instanceof Map<?, ?> financeMap) {
            Object adjustmentRaw = financeMap.get("priceAdjustment");
            if (adjustmentRaw instanceof Map<?, ?> adjustmentMap) {
                BigDecimal min = adjustmentMap.get("minPercent") != null
                        ? new BigDecimal(adjustmentMap.get("minPercent").toString()) : null;
                BigDecimal max = adjustmentMap.get("maxPercent") != null
                        ? new BigDecimal(adjustmentMap.get("maxPercent").toString()) : null;
                if (min != null && adjustmentPercent.compareTo(min) < 0) {
                    return ResponseBuilder.build(HttpStatus.BAD_REQUEST,
                            String.format("Phần trăm %.2f%% nhỏ hơn mức tối thiểu %.2f%%",
                                    adjustmentPercent.multiply(BigDecimal.valueOf(100)).doubleValue(),
                                    min.multiply(BigDecimal.valueOf(100)).doubleValue()), null);
                }
                if (max != null && adjustmentPercent.compareTo(max) > 0) {
                    return ResponseBuilder.build(HttpStatus.BAD_REQUEST,
                            String.format("Phần trăm %.2f%% vượt mức tối đa %.2f%%",
                                    adjustmentPercent.multiply(BigDecimal.valueOf(100)).doubleValue(),
                                    max.multiply(BigDecimal.valueOf(100)).doubleValue()), null);
                }
            }
        }

        LocalDate methodStartDate = parseLocalDateSafe(matchedTimeline.get("startDate"));
        LocalDate methodEndDate = parseLocalDateSafe(matchedTimeline.get("endDate"));
        LocalDate targetOpenDate = request.getOpenDate() != null ? request.getOpenDate()
                : (methodStartDate != null ? methodStartDate : campaign.getStartDate());
        LocalDate targetCloseDate = request.getCloseDate() != null ? request.getCloseDate()
                : (methodEndDate != null ? methodEndDate : campaign.getEndDate());

        Status initialApplicationStatus = deriveApplicationStatusByDateWindow(targetOpenDate, targetCloseDate);

        campusProgramOfferingRepo.save(CampusProgramOffering.builder()
                .campus(targetCampus)
                .admissionCampaign(campaign)
                .program(program)
                .programNameSnapshot(program.getName())
                .baseTuitionSnapshot(basePrice)
                .admissionMethod(requestedMethod)
                .quota(requestedQuota)
                .remainingQuota(requestedQuota)
                .learningMode(request.getLearningMode())
                .priceAdjustmentPercentage(adjustmentPercent.floatValue())
                .finalTuitionFee(finalFee)
                .allowReservationSubmission(allowReservation)
                .applicationStatus(initialApplicationStatus)
                .openDate(targetOpenDate)
                .closeDate(targetCloseDate)
                .status(Status.OFFERING_ACTIVE)
                .build());

        return ResponseBuilder.build(HttpStatus.OK,
                "Tạo gói tuyển sinh cho phương thức '" + requestedMethod + "' thành công.", null);
    }

    private Integer resolveConfiguredCampusQuota(Integer schoolId, Integer campusId, int campaignYear) {
        SchoolConfig quotaConfig = schoolConfigRepo
                .findBySchoolIdAndKey(schoolId, "quotaConfigData")
                .orElse(null);

        if (quotaConfig == null || !(quotaConfig.getValue() instanceof Map<?, ?> cfg)) {
            return null;
        }

        // parse year trực tiếp
        Object yearObj = cfg.get("academicYear");
        Integer configYear = null;
        if (yearObj != null) {
            try {
                configYear = Integer.parseInt(yearObj.toString().replaceAll("[^0-9]", "").substring(0, 4));
            } catch (Exception ignored) {
            }
        }

        if (configYear != null && !configYear.equals(campaignYear)) {
            return null;
        }

        if (!(cfg.get("campusAssignments") instanceof List<?> assignments)) {
            return null;
        }

        for (Object item : assignments) {
            if (!(item instanceof Map<?, ?> m)) continue;

            Integer cid = m.get("campusId") != null ? Integer.parseInt(m.get("campusId").toString()) : null;
            if (cid == null || !cid.equals(campusId)) continue;

            Integer quota = m.get("allocatedQuota") != null ? Integer.parseInt(m.get("allocatedQuota").toString()) : null;
            return (quota != null && quota > 0) ? quota : null;
        }

        return null;
    }

    @Override
    public ResponseEntity<ResponseObject> viewCampusProgramOfferingList(Integer campusId, int page, int pageSize) {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Tài khoản của bạn đang bị hạn chế, không thể thực hiện thao tác này.", null);
        }

        Campus actorCampus = extractActorCampus();
        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản cơ sở trường học hoặc bạn không có quyền truy cập.", null);
        }

        Pageable pageable;
        try {
            pageable = PaginationUtil.buildPageRequest(page, pageSize);
        } catch (IllegalArgumentException e) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, e.getMessage(), null);
        }

        Page<CampusProgramOffering> offeringPage;

        if (actorCampus.getIsPrimaryBranch()) {
            if (campusId == null || campusId <= 0) {
                // Xem toàn bộ Offering của cả School
                offeringPage = campusProgramOfferingRepo.findByAdmissionCampaign_School_IdOrderByIdDesc(actorCampus.getSchool().getId(), pageable);
            } else {
                // Xem của một Campus cụ thể, nhưng phải thuộc cùng School
                Optional<Campus> targetCampus = campusRepo.findByIdAndSchoolId(campusId, actorCampus.getSchool().getId());

                if (targetCampus.isEmpty()) {
                    return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Cơ sở được chọn không thuộc phạm vi trường của bạn.", null);
                }
                offeringPage = campusProgramOfferingRepo.findByCampusIdOrderByIdDesc(campusId, pageable);
            }
        } else {
            if (campusId != null && !campusId.equals(actorCampus.getId())) {
                return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Bạn chỉ được xem dữ liệu của cơ sở mình.", null);
            }
            offeringPage = campusProgramOfferingRepo.findByCampusIdOrderByIdDesc(actorCampus.getId(), pageable);
        }

        PageResponse<Map<String, Object>> pageResponse = PaginationUtil.buildPageResponse(offeringPage, this::buildOfferingData);

        return ResponseBuilder.build(HttpStatus.OK, "Lấy danh sách chương trình tuyển sinh theo cơ sở thành công.", pageResponse);
    }

    @Override
    public ResponseEntity<ResponseObject> getOfferingQuotaSummary(int campaignId) {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Tài khoản của bạn đang bị hạn chế.", null);
        }

        Campus actorCampus = extractActorCampus();
        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản cơ sở trường học.", null);
        }

        AdmissionCampaign campaign = admissionCampaignRepo.findById(campaignId).orElse(null);
        if (campaign == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Không tìm thấy chiến dịch tuyển sinh.", null);
        }

        if (!campaign.getSchool().getId().equals(actorCampus.getSchool().getId())) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Chiến dịch không thuộc trường của cơ sở này.", null);
        }

        Integer allocatedQuota = resolveConfiguredCampusQuota(
                actorCampus.getSchool().getId(), actorCampus.getId(), campaign.getYear());
        if (allocatedQuota == null || allocatedQuota <= 0) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST,
                    "Chưa cấu hình quota cho cơ sở này trong chiến dịch năm " + campaign.getYear() + ".", null);
        }

        int usedQuota = campusProgramOfferingRepo
                .findByAdmissionCampaignId(campaignId)
                .stream()
                .filter(o -> o.getCampus().getId().equals(actorCampus.getId()))
                .mapToInt(CampusProgramOffering::getQuota)
                .sum();

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("campaignId", campaignId);
        summary.put("campaignYear", campaign.getYear());
        summary.put("allocatedQuota", allocatedQuota);
        summary.put("usedQuota", usedQuota);
        summary.put("remainingQuota", allocatedQuota - usedQuota);

        return ResponseBuilder.build(HttpStatus.OK, "Lấy thông tin quota thành công.", summary);
    }

    @Override
    public ResponseEntity<ResponseObject> getOfferingQuotaBreakdown(int campaignId) {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Tài khoản của bạn đang bị hạn chế.", null);
        }

        Campus actorCampus = extractActorCampus();
        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản cơ sở trường học.", null);
        }

        AdmissionCampaign campaign = admissionCampaignRepo.findById(campaignId).orElse(null);
        if (campaign == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Không tìm thấy chiến dịch tuyển sinh.", null);
        }

        if (!campaign.getSchool().getId().equals(actorCampus.getSchool().getId())) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Chiến dịch không thuộc trường của cơ sở này.", null);
        }// Lấy toàn bộ offerings để tính global total (dùng cho remainingQuota)
        List<CampusProgramOffering> allOfferings = campusProgramOfferingRepo.findByAdmissionCampaignId(campaignId);

        // displayOfferings: non-primary campus chỉ thấy dữ liệu của mình trong campusBreakdown
        List<CampusProgramOffering> displayOfferings = allOfferings;
        if (!Boolean.TRUE.equals(actorCampus.getIsPrimaryBranch())) {
            int myId = actorCampus.getId();
            displayOfferings = allOfferings.stream()
                    .filter(o -> o.getCampus().getId().equals(myId))
                    .collect(Collectors.toList());
        }

        // Group display: methodCode -> campusId -> tổng quota; lưu tên campus
        Map<String, Map<Integer, Integer>> breakdown = new LinkedHashMap<>();
        Map<Integer, String> campusNameMap = new LinkedHashMap<>();

        for (CampusProgramOffering o : displayOfferings) {
            String method = normalize(o.getAdmissionMethod());
            if (method == null) continue;
            breakdown
                    .computeIfAbsent(method, k -> new LinkedHashMap<>())
                    .merge(o.getCampus().getId(), o.getQuota(), Integer::sum);
            campusNameMap.putIfAbsent(o.getCampus().getId(), o.getCampus().getName());
        }

        // Tổng toàn trường theo method (để tính totalUsedQuota và remainingQuota chính xác)
        Map<String, Integer> globalTotalUsedMap = new LinkedHashMap<>();
        for (CampusProgramOffering o : allOfferings) {
            String method = normalize(o.getAdmissionMethod());
            if (method == null) continue;
            globalTotalUsedMap.merge(method, o.getQuota(), Integer::sum);
        }

        // Build response theo thứ tự admissionMethodTimelines trong chiến dịch
        List<Map<String, Object>> methodList = new ArrayList<>();

        if (campaign.getAdmissionMethodTimelines() instanceof List<?> rawTimelines) {
            for (Object rawItem : rawTimelines) {
                if (!(rawItem instanceof Map<?, ?> tl)) continue;

                String methodCode = normalize(Objects.toString(tl.get("methodCode"), null));
                if (methodCode == null) continue;

                int totalMethodQuota = 0;
                try {
                    Object q = tl.get("quota");
                    if (q != null) totalMethodQuota = Integer.parseInt(q.toString());
                } catch (NumberFormatException ignored) {
                }

                int totalUsed = globalTotalUsedMap.getOrDefault(methodCode, 0);
                int remainingQuota = Math.max(0, totalMethodQuota - totalUsed);

                Map<Integer, Integer> campusMap = breakdown.getOrDefault(methodCode, Collections.emptyMap());

                List<Map<String, Object>> campusBreakdown = new ArrayList<>();
                campusMap.forEach((campusId, usedQuota) -> {
                    Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("campusId", campusId);
                    entry.put("campusName", campusNameMap.getOrDefault(campusId, ""));
                    entry.put("usedQuota", usedQuota);
                    // Chỉ tiêu còn lại mà campus này có thể tạo thêm cho method này
                    entry.put("maxAdditionalQuota", remainingQuota);
                    campusBreakdown.add(entry);
                });

                Map<String, Object> methodEntry = new LinkedHashMap<>();
                methodEntry.put("methodCode", methodCode);
                methodEntry.put("totalMethodQuota", totalMethodQuota);
                methodEntry.put("totalUsedQuota", totalUsed);
                methodEntry.put("remainingQuota", remainingQuota);
                methodEntry.put("isFull", remainingQuota == 0);
                methodEntry.put("campusBreakdown", campusBreakdown);
                methodList.add(methodEntry);
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("campaignId", campaignId);
        result.put("campaignYear", campaign.getYear());
        result.put("methods", methodList);

        return ResponseBuilder.build(HttpStatus.OK, "Lấy thông tin quota theo phương thức tuyển sinh thành công.", result);
    }

    @Override
    @Transactional
    public ResponseEntity<ResponseObject> updateCampusProgramOffering(UpdateCampusProgramOfferingRequest request) {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Tài khoản của bạn đang bị hạn chế, không thể thực hiện thao tác này.", null);
        }

        Campus actorCampus = extractActorCampus();
        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản cơ sở trường học hoặc bạn không có quyền truy cập.", null);
        }

        CampusProgramOffering offering = campusProgramOfferingRepo.findById(request.getId()).orElse(null);

        if (offering == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Không tìm thấy chương trình tuyển sinh cơ sở (offering).", null);
        }

        // Kiểm tra offering thuộc campus của actor
        if (!offering.getCampus().getId().equals(actorCampus.getId())) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Bạn không có quyền cập nhật gói tuyển sinh của cơ sở khác.", null);
        }

        // Chiến dịch phải đang OPEN mới được cập nhật offering
        if (offering.getAdmissionCampaign().getStatus() != Status.OPEN_ADMISSION_CAMPAIGN) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Không thể cập nhật gói tuyển sinh khi chiến dịch không còn ở trạng thái Mở.", null);
        }

        if (offering.getStatus() == Status.OFFERING_INACTIVE) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Gói tuyển sinh đã ngừng hoạt động, không thể cập nhật.", null);
        }

        if (offering.getApplicationStatus() != Status.PAUSED) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Chỉ được cập nhật khi chương trình đang ở trạng thái tạm dừng (PAUSED).", null);
        }

        if (request.getQuota() != null) {
            if (request.getQuota() <= 0) {
                return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Quota phải lớn hơn 0.", null);
            }

            // Đếm slot thực tế đang được giữ (PAYMENT_PENDING + DEPOSITED) thay vì dùng quota-remainingQuota
            // → tránh lỗi nếu remainingQuota bị lệch so với thực tế
            int usedQuota = admissionReservationFormRepo
                    .countByCampusProgramOfferingIdAndStatusIn(offering.getId(), Status.activeOfferingStatuses());
            if (request.getQuota() < usedQuota) {
                return ResponseBuilder.build(HttpStatus.BAD_REQUEST,
                        String.format("Quota mới (%d) không được nhỏ hơn số slot đang được giữ (%d).", request.getQuota(), usedQuota), null);
            }

            Integer allocatedQuota = resolveConfiguredCampusQuota(
                    actorCampus.getSchool().getId(), offering.getCampus().getId(), offering.getAdmissionCampaign().getYear());
            if (allocatedQuota == null || allocatedQuota <= 0) {
                return ResponseBuilder.build(HttpStatus.BAD_REQUEST,
                        "Chưa cấu hình quota cho cơ sở này.", null);
            }

            // Tổng quota các offering khác của campus trong campaign (trừ offering hiện tại)
            int otherUsedQuota = campusProgramOfferingRepo
                    .findByAdmissionCampaignId(offering.getAdmissionCampaign().getId())
                    .stream()
                    .filter(o -> o.getCampus().getId().equals(offering.getCampus().getId())
                            && !o.getId().equals(offering.getId()))
                    .mapToInt(CampusProgramOffering::getQuota)
                    .sum();

            if (otherUsedQuota + request.getQuota() > allocatedQuota) {
                return ResponseBuilder.build(HttpStatus.BAD_REQUEST,
                        String.format("Tổng quota vượt mức được phân bổ. Các gói khác: %d, Quota mới: %d, Giới hạn: %d.",
                                otherUsedQuota, request.getQuota(), allocatedQuota), null);
            }

            // Validate quota theo từng phương thức tuyển sinh (không vượt quota PTTS trong campaign)
            String offeringMethod = normalize(offering.getAdmissionMethod());
            if (offeringMethod != null && offering.getAdmissionCampaign().getAdmissionMethodTimelines() instanceof List<?> rawTimelines) {
                for (Object rawItem : rawTimelines) {
                    if (!(rawItem instanceof Map<?, ?> tl)) continue;
                    if (!offeringMethod.equals(normalize(Objects.toString(tl.get("methodCode"), null)))) continue;
                    Object methodQuotaRaw = tl.get("quota");
                    if (methodQuotaRaw == null) break;
                    try {
                        int methodQuota = Integer.parseInt(methodQuotaRaw.toString());
                        // Đếm tổng quota đã dùng của TẤT CẢ campus cho method này (trừ offering đang update)
                        int otherUsedForMethod = campusProgramOfferingRepo
                                .findByAdmissionCampaignId(offering.getAdmissionCampaign().getId())
                                .stream()
                                .filter(o -> !o.getId().equals(offering.getId())
                                        && offeringMethod.equals(normalize(o.getAdmissionMethod())))
                                .mapToInt(CampusProgramOffering::getQuota)
                                .sum();
                        if (otherUsedForMethod + request.getQuota() > methodQuota) {
                            return ResponseBuilder.build(HttpStatus.BAD_REQUEST,
                                    String.format("Quota cho phương thức '%s' vượt giới hạn chiến dịch. Các gói khác: %d, Quota mới: %d, Giới hạn PTTS: %d.",
                                            offeringMethod, otherUsedForMethod, request.getQuota(), methodQuota), null);
                        }
                    } catch (NumberFormatException ignored) {
                    }
                    break;
                }
            }

            offering.setQuota(request.getQuota());
            offering.setRemainingQuota(request.getQuota() - usedQuota);
        }

        if (request.getLearningMode() != null) {
            offering.setLearningMode(request.getLearningMode());
        }

        if (request.getPriceAdjustmentPercentage() != null) {
            BigDecimal pricingBase = offering.getBaseTuitionSnapshot();
            if (pricingBase.compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseBuilder.build(HttpStatus.BAD_REQUEST,
                        "Không thể cập nhật học phí vì thiếu giá gốc hợp lệ.", null);
            }

            float adjustmentPercent = request.getPriceAdjustmentPercentage();

            SchoolConfig financePolicyConfig = schoolConfigRepo
                    .findBySchoolIdAndKey(actorCampus.getSchool().getId(), "financePolicyData")
                    .orElse(null);

            if (financePolicyConfig != null && financePolicyConfig.getValue() instanceof Map<?, ?> financeMap) {
                if (financeMap.get("priceAdjustment") instanceof Map<?, ?> adj) {
                    Double min = adj.get("minPercent") != null ? Double.valueOf(adj.get("minPercent").toString()) : null;
                    Double max = adj.get("maxPercent") != null ? Double.valueOf(adj.get("maxPercent").toString()) : null;
                    if (min != null && adjustmentPercent < min) {
                        return ResponseBuilder.build(HttpStatus.BAD_REQUEST,
                                String.format("Phần trăm %.2f%% nhỏ hơn tối thiểu %.2f%%",
                                        adjustmentPercent * 100, min * 100), null);
                    }
                    if (max != null && adjustmentPercent > max) {
                        return ResponseBuilder.build(HttpStatus.BAD_REQUEST,
                                String.format("Phần trăm %.2f%% vượt tối đa %.2f%%",
                                        adjustmentPercent * 100, max * 100), null);
                    }
                }
            }

            BigDecimal finalFee = pricingBase
                    .multiply(BigDecimal.ONE.add(BigDecimal.valueOf(adjustmentPercent)))
                    .setScale(0, RoundingMode.HALF_UP);

            offering.setPriceAdjustmentPercentage(adjustmentPercent);
            offering.setFinalTuitionFee(finalFee);
        }

        campusProgramOfferingRepo.save(offering);

        return ResponseBuilder.build(HttpStatus.OK, "Cập nhật chương trình tuyển sinh tại cơ sở thành công.", null);
    }

    @Override
    @Transactional
    public ResponseEntity<ResponseObject> changeCampusProgramOfferingStatus(int offeringId, OfferingProgramAction action) {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Tài khoản của bạn đang bị hạn chế, không thể thực hiện thao tác này.", null);
        }

        Campus actorCampus = extractActorCampus();
        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản cơ sở trường học hoặc bạn không có quyền truy cập.", null);
        }

        CampusProgramOffering offering = campusProgramOfferingRepo.findById(offeringId).orElse(null);

        if (offering == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Không tìm thấy chương trình tuyển sinh cơ sở (offering).", null);
        }

        if (!offering.getCampus().getId().equals(actorCampus.getId())) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Bạn không có quyền cập nhật trạng thái chương trình của cơ sở khác.", null);
        }

        if (offering.getStatus() == Status.OFFERING_INACTIVE) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Gói tuyển sinh đã ngừng hoạt động, không thể đổi trạng thái.", null);
        }

        // CLOSE/PAUSE/PUBLISH chỉ điều khiển lớp vận hành (applicationStatus).
        // Vòng đời record (status) chỉ đổi sang OFFERING_INACTIVE khi bị campaign/program ngừng ở luồng SchoolService.
        if (action == OfferingProgramAction.CLOSE) {
            if (offering.getApplicationStatus() == Status.CLOSED || offering.getApplicationStatus() == Status.FULL) {
                return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Chương trình này đã được đóng trước đó.", null);
            }
            if (offering.getApplicationStatus() == Status.PAUSED) {
                return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Chương trình đang tạm dừng, vui lòng mở lại trước khi đóng.", null);
            }

            int formCount = admissionReservationFormRepo.countByCampusProgramOfferingIdAndStatusIn(offeringId, Status.activeReservationStatuses());

            if (formCount >= offering.getQuota() || offering.getRemainingQuota() <= 0) {
                // Nếu đóng khi đã đủ hoặc vượt chỉ tiêu -> Hiển thị là FULL (Hết chỗ)
                offering.setApplicationStatus(Status.FULL);
                offering.setRemainingQuota(0);
            } else {
                // Nếu đóng khi chưa đủ chỉ tiêu (do Admin chủ động hoặc hết hạn) -> Hiển thị là CLOSED (Đóng)
                offering.setApplicationStatus(Status.CLOSED);
            }
            campusProgramOfferingRepo.save(offering);

            return ResponseBuilder.build(HttpStatus.OK, (formCount >= offering.getQuota()) ? "Chương trình đã đạt chỉ tiêu và được đóng tự động." : "Chương trình đã được quản trị viên đóng thành công.", null);
        }

        if (offering.getApplicationStatus() == Status.FULL) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Chương trình đã đủ chỉ tiêu, không thể đổi trạng thái.", null);
        }

        if (action == OfferingProgramAction.PAUSE) {

            if (offering.getApplicationStatus() == Status.PAUSED) {
                return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Chương trình đã ở trạng thái tạm dừng, không thể tạm dừng lại.", null);
            }

            if (offering.getApplicationStatus() == Status.CLOSED) {
                return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Chương trình đã đóng, không thể tạm dừng.", null);
            }

            offering.setApplicationStatus(Status.PAUSED);

        } else if (action == OfferingProgramAction.PUBLISH) {

            if (offering.getApplicationStatus() != Status.PAUSED) {
                return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Chỉ có thể mở lại chương trình khi đang ở trạng thái tạm dừng (PAUSED).", null);
            }

            int formCount = admissionReservationFormRepo.countByCampusProgramOfferingIdAndStatusIn(offeringId, Status.activeReservationStatuses());

            Status nextStatus = deriveApplicationStatusByWindowAndQuota(
                    offering.getOpenDate(),
                    offering.getCloseDate(),
                    offering.getQuota(),
                    offering.getRemainingQuota(),
                    formCount
            );

            if (nextStatus == Status.FULL) {
                return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Không thể mở lại vì đã đủ chỉ tiêu.", null);
            }

            if (nextStatus == Status.CLOSED) {
                return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Không thể mở lại vì đã quá thời hạn nhận hồ sơ.", null);
            }

            offering.setApplicationStatus(nextStatus);

        } else {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Hành động không hợp lệ. Chỉ hỗ trợ PUBLISH, PAUSE hoặc CLOSE.", null);
        }

        campusProgramOfferingRepo.save(offering);

        return ResponseBuilder.build(HttpStatus.OK, "Cập nhật trạng thái chương trình thành công.", null);
    }

    private Map<String, Object> buildOfferingData(CampusProgramOffering offering) {
        Map<String, Object> data = new HashMap<>();
        Program program = offering.getProgram();
        var curriculum = program.getCurriculum();

        Status effectiveOperationalStatus = resolveEffectiveOperationalStatus(offering);

        data.put("id", offering.getId());
        data.put("campusId", offering.getCampus().getId());
        data.put("campusName", offering.getCampus().getName());
        data.put("admissionCampaignId", offering.getAdmissionCampaign() != null ? offering.getAdmissionCampaign().getId() : null);
        data.put("learningMode", offering.getLearningMode());
        data.put("tuitionFee", offering.getFinalTuitionFee());
        data.put("quota", offering.getQuota());
        data.put("remainingQuota", offering.getRemainingQuota());
        data.put("openDate", offering.getOpenDate());
        data.put("closeDate", offering.getCloseDate());
        data.put("applicationStatus", effectiveOperationalStatus);
        data.put("admissionMethod", offering.getAdmissionMethod());
        data.put("allowReservationSubmission", offering.getAllowReservationSubmission());
        data.put("status", CheckCampusOfferingStatus.checkOfferingStatus(offering, campusProgramOfferingRepo).getStatus());

        if (Boolean.TRUE.equals(offering.getAllowReservationSubmission())) {
            Map<String, Object> timeline = findMethodTimeline(
                    offering.getAdmissionCampaign(), offering.getAdmissionMethod());
            if (timeline != null) {
                data.put("reservationFee", timeline.get("reservationFee"));
                data.put("depositEndDate", timeline.get("depositEndDate"));
                data.put("confirmationEndDate", timeline.get("confirmationEndDate"));
            }
        }

        // Trả block chi tiết để FE không cần gọi thêm API khi mở detail từ list offering.
        Map<String, Object> curriculumData = new LinkedHashMap<>();
        curriculumData.put("id", curriculum.getId());
        curriculumData.put("name", curriculum.getName());
        curriculumData.put("description", curriculum.getDescription());
        curriculumData.put("curriculumType", curriculum.getCurriculumType());
        curriculumData.put("applicationYear", curriculum.getApplicationYear());
        curriculumData.put("groupCode", curriculum.getGroupCode());
        curriculumData.put("status", curriculum.getCurriculumStatus());
        curriculumData.put("subjectOptions", curriculum.getSubjectsJsonb());
        curriculumData.put("methodLearnings", curriculum.getLearningMethodList());

        Map<String, Object> programData = new LinkedHashMap<>();
        programData.put("id", program.getId());
        programData.put("name", offering.getProgramNameSnapshot());
        programData.put("graduationStandard", program.getGraduationStandard());
        programData.put("languageOfInstructionList", program.getLanguageOfInstructionList());
        programData.put("targetStudentDescription", program.getTargetStudentDescription());
        programData.put("baseTuitionFee", offering.getBaseTuitionSnapshot());
        programData.put("feeUnit", program.getFeeUnit());
        programData.put("extraSubjectList", program.getExtraSubjectsJsonb());
        programData.put("status", program.getStatus());
        programData.put("curriculum", curriculumData);

        data.put("program", programData);
        return data;
    }

    private Status resolveEffectiveOperationalStatus(CampusProgramOffering offering) {
        Status current = offering.getApplicationStatus();

        if (current == Status.PAUSED
                || current == Status.CLOSED
                || current == Status.FULL) {
            return current; // campus chủ động paused / closed / full
        }

        int activeReservationCount = admissionReservationFormRepo.countByCampusProgramOfferingIdAndStatusIn(
                offering.getId(), Status.activeReservationStatuses());

        return deriveApplicationStatusByWindowAndQuota(
                offering.getOpenDate(),
                offering.getCloseDate(),
                offering.getQuota(),
                offering.getRemainingQuota(),
                activeReservationCount
        );
    }

    private static Status deriveApplicationStatusByDateWindow(LocalDate openDate, LocalDate closeDate) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        if (today.isBefore(openDate)) {
            return Status.UPCOMING_OFFERING;
        }

        if (today.isAfter(closeDate)) {
            return Status.CLOSED;
        }

        return Status.OPEN;
    }

    private Map<String, Object> findMethodTimeline(AdmissionCampaign campaign, String methodCode) {
        if (campaign == null || methodCode == null) return null;
        if (!(campaign.getAdmissionMethodTimelines() instanceof List<?> timelines)) return null;

        return timelines.stream()
                .filter(t -> t instanceof Map)
                .map(t -> (Map<String, Object>) t)
                .filter(t -> methodCode.equalsIgnoreCase(String.valueOf(t.get("methodCode"))))
                .findFirst()
                .orElse(null);
    }

    private static Status deriveApplicationStatusByWindowAndQuota(LocalDate openDate,
                                                                  LocalDate closeDate,
                                                                  int quota,
                                                                  int remainingQuota,
                                                                  int activeReservationCount) {
        if (activeReservationCount >= quota || remainingQuota <= 0) {
            return Status.FULL; // Kiểm tra hết chỗ trước
        }
        return deriveApplicationStatusByDateWindow(openDate, closeDate); // Nếu còn chỗ mới xét đến ngày tháng
    }

    private LocalDate parseLocalDateSafe(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDate ld) return ld;
        try {
            return LocalDate.parse(String.valueOf(value));
        } catch (Exception ignored) {
            return null;
        }
    }


    @Override
    @Transactional
    public ResponseEntity<ResponseObject> createAccountCounsellor(CreateAccountCounsellorRequest request) {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Tài khoản của bạn đang bị hạn chế, không thể thực hiện thao tác này.", null);
        }

        Campus actorCampus = extractActorCampus();
        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản cơ sở trường học hoặc bạn không có quyền truy cập.", null);
        }

        String validationError = CounsellorValidation.validateCreateCounsellor(request, accountRepo, campusResourceQuotaRepo, counsellorRepo, actorCampus.getId());

        if (validationError != null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, validationError, null);
        }

        Account account = accountRepo.save(Account.builder().email(normalize(request.getEmail())).role(Role.COUNSELLOR).status(Status.ACCOUNT_ACTIVE).registerDate(LocalDate.now()).firstLogin(true).build());

        String rawName = request.getEmail() != null && request.getEmail().contains("@")
                ? request.getEmail().split("@")[0]   // cắt trước @
                : "Tư vấn viên";

        String generatedName = Arrays.stream(rawName.split("[._-]"))
                .map(part -> part.isEmpty() ? "" :
                        Character.toUpperCase(part.charAt(0)) + part.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));

        Counsellor counsellor = counsellorRepo.save(Counsellor.builder()
                .account(account)
                .campus(actorCampus)
                .avatar(request.getAvatar())
                .name(generatedName)
                .employeeCode(UUID.randomUUID()).build());

        return ResponseBuilder.build(HttpStatus.OK, "Tạo tài khoản chuyên viên tư vấn thành công.", buildCounsellorData(counsellor));
    }

    @Override
    public ResponseEntity<ResponseObject> viewAccountCounsellorList(int page, int size) {

        Campus actorCampus = extractActorCampus();

        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản cơ sở trường học hoặc bạn không có quyền truy cập.", null);
        }

        //lấy thông tin Quota và Usage
        var quotaOpt = campusResourceQuotaRepo.findByCampusIdAndResourceType(actorCampus.getId(), ResourceType.COUNSELLOR);
        int maxQuota = quotaOpt.map(CampusResourceQuota::getMaxQuota).orElse(0);
        long currentUsage = counsellorRepo.countByCampusId(actorCampus.getId());

        //kiểm tra trạng thái access
        Status accessStatus = ResourceCheckerUtil.checkAccessStatus(
                actorCampus.getId(),
                ResourceType.COUNSELLOR,
                campusResourceQuotaRepo,
                currentUsage
        );

        Pageable pageable;

        try {
            pageable = PaginationUtil.buildPageRequest(page, size);
        } catch (IllegalArgumentException e) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, e.getMessage(), null);
        }

        Page<Counsellor> counsellorPage = counsellorRepo.findByCampusId(actorCampus.getId(), pageable);

        PageResponse<Map<String, Object>> pageResponse = PaginationUtil.buildPageResponse(counsellorPage, this::buildCounsellorData);

        Map<String, Object> data = new HashMap<>();
        data.put("counsellors", pageResponse);

        data.put("accessStatus", accessStatus.name());
        data.put("canCreate", accessStatus.isFeatureAvailable());
        data.put("displayMessage", accessStatus.getDisplayMessage());
        data.put("currentUsage", currentUsage);
        data.put("maxQuota", maxQuota);

        return ResponseBuilder.build(HttpStatus.OK, "Lấy danh sách chuyên viên tư vấn thành công.", data);
    }

    @Override
    public ResponseEntity<ResponseObject> getQuotaRequestSummary() {

        Campus actorCampus = extractActorCampus();

        if (actorCampus == null)
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Không tìm thấy cơ sở trường học.", null);

        var quotaOpt = campusResourceQuotaRepo.findByCampusIdAndResourceType(actorCampus.getId(), ResourceType.COUNSELLOR);
        int maxQuota = quotaOpt.map(CampusResourceQuota::getMaxQuota).orElse(0);
        long currentUsage = counsellorRepo.countByCampusId(actorCampus.getId());

        Campus primaryCampus = campusRepo.findAllBySchoolId(actorCampus.getSchool().getId()).stream()
                .filter(Campus::getIsPrimaryBranch)
                .findFirst()
                .orElse(null);

        Map<String, Object> summary = new HashMap<>();
        summary.put("campusName", actorCampus.getName());
        summary.put("currentUsage", currentUsage);
        summary.put("maxQuota", maxQuota);
        summary.put("primaryBranchEmail", primaryCampus != null ? primaryCampus.getAccount().getEmail() : "admin@school.com");
        summary.put("schoolName", actorCampus.getSchool().getName());

        return ResponseBuilder.build(HttpStatus.OK, "Lấy tóm tắt hạn mức chuyên viên thành công.", summary);
    }

    public String generateProfessionalEmployeeCode(Campus campus, UUID uuid) {
        if (campus == null || uuid == null) return "GLOBAL_CS_UNKNOWN";

        String rawName = campus.getName();
        String nfdNormalizedString = Normalizer.normalize(rawName, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String campusPart = pattern.matcher(nfdNormalizedString).replaceAll("")
                .replace("đ", "d").replace("Đ", "D")
                .replaceAll("\\s+", "")
                .toUpperCase();

        String yearMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMM"));

        String uuidStr = uuid.toString();
        String uuidPart = uuidStr.substring(0, 4).toUpperCase();

        return campusPart + "_CS" + yearMonth + "_" + uuidPart;
    }

    private Map<String, Object> buildCounsellorData(Counsellor counsellor) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", counsellor.getId());
        data.put("name", counsellor.getName());
        data.put("avatar", counsellor.getAvatar());
        data.put("employeeCode", generateProfessionalEmployeeCode(counsellor.getCampus(), counsellor.getEmployeeCode()));
        data.put("campusId", counsellor.getCampus().getId());
        data.put("campusName", counsellor.getCampus().getName());
        data.put("account", buildAccountData(counsellor.getAccount()));
        return data;
    }

    private Map<String, Object> buildAccountData(Account account) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", account.getId());
        data.put("email", account.getEmail());
        data.put("registerDate", account.getRegisterDate());
        data.put("status", account.getStatus());
        data.put("role", account.getRole());
        data.put("firstLogin", account.getFirstLogin());
        return data;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Campus extractActorCampus() {
        Account account = AuthRequestUtil.extractAuthenticatedAccount();
        if (account == null || account.getRole() != Role.SCHOOL) {
            return null;
        }
        return account.getCampus();
    }

    @Override
    @Transactional
    public ResponseEntity<ResponseObject> updateCampusConfig(UpdateCampusConfigRequest request) {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Tài khoản của bạn đang bị hạn chế, không thể thực hiện thao tác này.", null);
        }

        Campus actorCampus = extractActorCampus();

        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản cơ sở trường học hoặc bạn không có quyền truy cập.", null);
        }

        // campus xử lý facility
        // lấy facility của chính cơ sở đang thao tác
        Map<String, Object> currentCampusFacility = (Map<String, Object>) actorCampus.getFacility();

        List<Map<String, Object>> currentItems = new ArrayList<>();

        if (currentCampusFacility != null && currentCampusFacility.get("itemList") != null) {
            currentItems = (List<Map<String, Object>>) currentCampusFacility.get("itemList");
        }

        // lấy facility từ primary chính đã config cơ sở vật chất chung cho các campus
        // ==> để biết bên primary campus có thêm / bớt CSVC nào ko?
        SchoolConfig facilityData = schoolConfigRepo.findBySchoolIdAndKey(actorCampus.getSchool().getId(), "facilityData").orElse(null);

        List<Map<String, Object>> itemList = new ArrayList<>();

        if (facilityData != null && facilityData.getValue() instanceof Map) {
            Map<String, Object> val = (Map<String, Object>) facilityData.getValue();
            itemList = (List<Map<String, Object>>) val.get("itemList");
        }

        // thống nhất dữ liệu lại : facility campus chính, facility hiện có của chính cơ sở đó + request gửi lên để sửa
        List<Map<String, Object>> mergedFinalFacilityItems = SchoolConfigUtil.mergeFacilityItems(
                itemList,
                currentItems,
                request.getItemList()
        );

        Map<String, Object> facilityJson = new HashMap<>();
        facilityJson.put("itemList", mergedFinalFacilityItems);
        facilityJson.put("imageData", request.getImageJsonData());
        actorCampus.setFacility(facilityJson);

        // campus xử lý operating --> policy detail
        SchoolConfig operationSettingsData = schoolConfigRepo.findBySchoolIdAndKey(actorCampus.getSchool().getId(), "operationSettingsData").orElse(null);

        if (operationSettingsData != null) {
            Map<String, Object> mergedOp = SchoolConfigUtil.mergeOperationConfig((Map<String, Object>) operationSettingsData.getValue(), request);

            String finalPolicyStr = SchoolConfigUtil.convertOperationToPolicyString(mergedOp);
            if (request.getPolicyDetail() != null && !request.getPolicyDetail().isBlank()) {
                finalPolicyStr += "\n------------------------------------------\n";
                finalPolicyStr += "📌 LƯU Ý RIÊNG TẠI CƠ SỞ:\n" + request.getPolicyDetail();
            }

            Map<String, Object> policyJsonb = new HashMap<>();
            policyJsonb.put("minCounsellorPerSlot", mergedOp.get("minCounsellorPerSlot"));
            policyJsonb.put("maxCounsellorsPerSlot", mergedOp.get("maxCounsellorsPerSlot"));
            policyJsonb.put("slotDurationInMinutes", mergedOp.get("slotDurationInMinutes"));
            policyJsonb.put("bufferBetweenSlotsMinutes", mergedOp.get("bufferBetweenSlotsMinutes"));
            policyJsonb.put("maxBookingPerSlot", mergedOp.get("maxBookingPerSlot"));
            policyJsonb.put("allowBookingBeforeHours", mergedOp.get("allowBookingBeforeHours"));
            policyJsonb.put("workingConfig", mergedOp.get("workingConfig"));
            policyJsonb.put("fullTextRendered", finalPolicyStr);
            policyJsonb.put("rawCustomNote", request.getPolicyDetail());

            actorCampus.setPolicyDetail(policyJsonb);
        }

        try {

            Optional<TemplateDocx> campusTemplateDocx = templateDocxRepo.findTopByTypeOrderByVersionDesc(CategoryTemplate.CAMPUS_INFO_TEMPLATE);

            if (campusTemplateDocx.isEmpty()) {
                throw new Exception("Campus document template is not available.");
            }

            String templatePath = campusTemplateDocx.get().getFolderName() + "/" + campusTemplateDocx.get().getFileName();

            supabaseStorageService.removeFile(actorCampus.getFolderPath(), actorCampus.getFileName());

            String uuid = UUID.randomUUID().toString();


            List<Map<String, Object>> facilityItemsBuild = mergedFinalFacilityItems.stream()
                    .map(item -> {
                        Map<String, Object> row = new HashMap<>();
                        row.put("code", item.get("facilityCode"));
                        row.put("facilityName", item.get("name"));
                        row.put("category", item.get("category"));
                        row.put("quantity", item.get("value"));
                        row.put("unit", item.get("unit"));
                        return row;
                    })
                    .toList();

            Map<String, Object> campusBuildedData = buildCampusDocxData(actorCampus, facilityItemsBuild);

            String folderName = actorCampus.getFolderPath();
            String fileName = "campus_info_" + uuid + ".docx";

            String campusFileUrl = supabaseStorageService.generateDocFileFromTemplate(
                    campusBuildedData,
                    templatePath,
                    folderName,
                    fileName
            );

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("type", "campus_info");
            payload.put("schoolId", actorCampus.getSchool().getId());
            payload.put("schoolName", actorCampus.getSchool().getName());
            payload.put("campusId", actorCampus.getId());
            payload.put("campusInfoFileUrl", campusFileUrl);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            restTemplate.postForEntity(
                    n8nUrl,
                    entity,
                    String.class
            );

            actorCampus.setFileName(fileName);

        } catch (Exception e) {
            System.out.println("Failed to generate campus docx" + e.getMessage());
        }

        campusRepo.save(actorCampus);

        return ResponseBuilder.build(HttpStatus.OK, "Cập nhật cấu hình cơ sở thành công.", null);
    }

    private Map<String, Object> buildCampusDocxData(Campus campus, List<Map<String, Object>> facilityItems) {
        Map<String, Object> data = new LinkedHashMap<>();

        data.put("name", campus.getName());
        data.put("schoolName", campus.getSchool() != null ? campus.getSchool().getName() : "");
        data.put("phoneNumber", campus.getPhoneNumber());
        data.put("address", campus.getAddress());
        data.put("boardingType", campus.getBoardingType());
        data.put("boardingDescription", mapBoardingDescription(campus.getBoardingType()));
        data.put("facilityItems", facilityItems);

        return data;
    }

    private String mapBoardingDescription(BoardingType type) {

        if (type == null) {
            return "Hiện tại cơ sở chưa cập nhật thông tin về dịch vụ nội trú.";
        }

        return switch (type) {

            case FULL_BOARDING ->
                    "Cơ sở cung cấp dịch vụ nội trú toàn phần, nơi học sinh sinh hoạt tại trường với chỗ ở, bữa ăn và sự chăm sóc toàn diện hằng ngày.";

            case SEMI_BOARDING ->
                    "Cơ sở cung cấp dịch vụ bán trú, cho phép học sinh ở lại trường vào ban ngày để dùng bữa, được hỗ trợ học tập và tham gia các hoạt động ngoại khóa mà không lưu trú qua đêm.";

            case BOTH ->
                    "Cơ sở cung cấp cả dịch vụ nội trú toàn phần và bán trú, mang đến lựa chọn linh hoạt về lưu trú và chăm sóc ban ngày để đáp ứng nhu cầu đa dạng của học sinh.";
        };
    }

    @Override
    public ResponseEntity<ResponseObject> getCampusConfig() {

        Campus actorCampus = extractActorCampus();

        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản cơ sở trường học hoặc bạn không có quyền truy cập.", null);
        }

        SchoolConfig hqFacility = schoolConfigRepo.findBySchoolIdAndKey(actorCampus.getSchool().getId(), "facilityData").orElse(null);

        SchoolConfig hqOperation = schoolConfigRepo.findBySchoolIdAndKey(actorCampus.getSchool().getId(), "operationSettingsData").orElse(null);

        Map<String, Object> result = new HashMap<>();

        Map<String, Object> hqSection = new HashMap<>();

        if (hqFacility != null && hqFacility.getValue() instanceof Map) {
            Map<String, Object> fullFacility = (Map<String, Object>) hqFacility.getValue();
            Map<String, Object> filteredFacility = new HashMap<>();

            // CHỈ lấy những trường cần thiết, KHÔNG lấy overview
            filteredFacility.put("itemList", fullFacility.get("itemList"));
            filteredFacility.put("imageData", fullFacility.get("imageData"));

            hqSection.put("facility", filteredFacility);
        } else {
            hqSection.put("facility", null);
        }

        Object hqWorkingConfig = null;
        if (hqOperation != null && hqOperation.getValue() instanceof Map) {
            Map<String, Object> fullOp = (Map<String, Object>) hqOperation.getValue();
            hqWorkingConfig = fullOp.get("workingConfig");
            Map<String, Object> filteredOp = new HashMap<>();

            filteredOp.put("workingConfig", hqWorkingConfig);
            filteredOp.put("admissionProcesses", fullOp.get("admissionProcesses"));
            filteredOp.put("maxBookingPerSlot", fullOp.get("maxBookingPerSlot"));
            filteredOp.put("minCounsellorPerSlot", fullOp.get("minCounsellorPerSlot"));
            filteredOp.put("maxCounsellorsPerSlot", fullOp.get("maxCounsellorsPerSlot"));
            filteredOp.put("slotDurationInMinutes", fullOp.get("slotDurationInMinutes"));
            filteredOp.put("bufferBetweenSlotsMinutes", fullOp.get("bufferBetweenSlotsMinutes"));
            filteredOp.put("allowBookingBeforeHours", fullOp.get("allowBookingBeforeHours"));

            hqSection.put("operation", filteredOp);
        } else {
            hqSection.put("operation", null);
        }

        result.put("hqDefault", hqSection);

        Map<String, Object> campusUpdateInfo = new HashMap<>();

        Map<String, Object> campusFacilityData = (Map<String, Object>) actorCampus.getFacility();

        if (campusFacilityData != null) {
            campusUpdateInfo.put("itemList", campusFacilityData.get("itemList"));
            campusUpdateInfo.put("imageData", campusFacilityData.get("imageData"));
        }

        Map<String, Object> campusPolicyDb = (Map<String, Object>) actorCampus.getPolicyDetail();
        if (campusPolicyDb != null) {
            campusUpdateInfo.put("minCounsellorPerSlot", campusPolicyDb.get("minCounsellorPerSlot"));
            campusUpdateInfo.put("maxCounsellorsPerSlot", campusPolicyDb.get("maxCounsellorsPerSlot"));
            campusUpdateInfo.put("slotDurationInMinutes", campusPolicyDb.get("slotDurationInMinutes"));
            campusUpdateInfo.put("bufferBetweenSlotsMinutes", campusPolicyDb.get("bufferBetweenSlotsMinutes"));
            campusUpdateInfo.put("maxBookingPerSlot", campusPolicyDb.get("maxBookingPerSlot"));
            campusUpdateInfo.put("allowBookingBeforeHours", campusPolicyDb.get("allowBookingBeforeHours"));
            campusUpdateInfo.put("fullPolicyRendered", campusPolicyDb.get("fullTextRendered"));
            campusUpdateInfo.put("policyDetail", campusPolicyDb.get("rawCustomNote")); // Note riêng của campus
        }
        if (hqWorkingConfig != null) {
            campusUpdateInfo.put("workingConfig", hqWorkingConfig);
        } else if (campusPolicyDb != null) {
            campusUpdateInfo.put("workingConfig", campusPolicyDb.get("workingConfig"));
        }

        result.put("campusCurrent", campusUpdateInfo);

        return ResponseBuilder.build(HttpStatus.OK, "Lấy cấu hình cơ sở và chuẩn HQ thành công.", result);
    }

    @Override
    @Transactional
    public ResponseEntity<ResponseObject> upsertCampusScheduleTemplate(CampusScheduleTemplateRequest request) {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Tài khoản của bạn đang bị hạn chế, không thể thực hiện thao tác này.", null);
        }

        Campus actorCampus = extractActorCampus();

        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản cơ sở trường học hoặc bạn không có quyền truy cập.", null);
        }

        if (request.getDayOfWeek() == null || request.getDayOfWeek().isEmpty()) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Danh sách thứ trong tuần (dayOfWeek) không được để trống.", null);
        }

        if (request.getSessionType() == null || request.getSessionType().isBlank()) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Loại buổi (sessionType) không được để trống.", null);
        }

        final SessionType normalizedSessionType;
        try {
            normalizedSessionType = SessionType.valueOf(request.getSessionType().trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Loại buổi (sessionType) không hợp lệ.", null);
        }

        if (request.getTemplateId() != null && request.getTemplateId() > 0) {

            CampusScheduleTemplate existing = campusScheduleTemplateRepo.findById(request.getTemplateId()).orElse(null);

            if (existing == null) {
                return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Không tìm thấy khung lịch (template).", null);
            }

            if (!existing.getCampus().getId().equals(actorCampus.getId())) {
                return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Bạn không được phép sửa khung lịch của cơ sở khác.", null);
            }

            List<CounsellorSlot> linkedToTemplate = counsellorSlotRepo.findByCampusScheduleTemplate_Id(request.getTemplateId());
            String templateBlockReason = CounsellorSlotValidation.reasonTemplateUpdateBlocked(linkedToTemplate);
            if (templateBlockReason != null) {
                return ResponseBuilder.build(HttpStatus.CONFLICT, templateBlockReason, null);
            }

            if (request.getDayOfWeek().size() != 1) {
                return ResponseBuilder.build(HttpStatus.BAD_REQUEST,
                        "Khi cập nhật khung lịch, chỉ được gửi đúng một thứ trong dayOfWeek. ",
                        null);
            }
        }

        if (Boolean.TRUE.equals(request.getExpandToPolicySlots())
                && request.getTemplateId() != null && request.getTemplateId() > 0) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST,
                    "Tách khung theo độ dài một slot chỉ áp dụng khi tạo mới: không được gửi kèm templateId.", null);
        }

        SchoolConfig hqSchoolConfig = schoolConfigRepo.findBySchoolIdAndKey(actorCampus.getSchool().getId(), "operationSettingsData").orElse(null);
        Map<String, Object> effectiveOperation = SchoolConfigUtil.getEffectiveOperationSettingsMap(hqSchoolConfig, actorCampus);
        Map<String, Object> workingConfig = (Map<String, Object>) effectiveOperation.get("workingConfig");
        if (workingConfig == null) {
            workingConfig = SchoolConfigUtil.getWorkingConfig(hqSchoolConfig);
        }
        Map<String, Integer> numericPolicy = SchoolConfigUtil.getNumericPolicyFromOperationMap(effectiveOperation);
        Integer slotDurationMinutes = numericPolicy.get("slotDurationInMinutes");
        int bufferBetweenSlotsMinutes = numericPolicy.getOrDefault("bufferBetweenSlotsMinutes", 0);

        LocalTime[] window = SchoolConfigUtil.resolveShiftTimeWindowForSessionType(workingConfig, normalizedSessionType.name());
        if (window == null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST,
                    "Không tìm thấy ca làm việc trong cấu hình vận hành trường khớp với buổi " + normalizedSessionType.name()
                            + ". Hãy cấu hình workShifts trong operationSettingsData (campus chính / HQ).", null);
        }
        DateTimeFormatter hm = DateTimeFormatter.ofPattern("HH:mm");
        String startStr = window[0].format(hm);
        String endStr = window[1].format(hm);

        boolean expandToPolicySlots = Boolean.TRUE.equals(request.getExpandToPolicySlots());

        try {
            for (String day : request.getDayOfWeek()) {

                if (expandToPolicySlots) {
                    if (slotDurationMinutes == null || slotDurationMinutes <= 0) {
                        throw new IllegalArgumentException("Cần cấu hình slotDurationInMinutes lớn hơn 0 trong vận hành (HQ hoặc campus) để tách khung theo độ dài một slot.");
                    }
                    List<String[]> windows;
                    try {
                        LocalTime rangeStart = LocalTime.parse(startStr);
                        LocalTime rangeEnd = LocalTime.parse(endStr);
                        windows = SchoolConfigUtil.splitRangeIntoPolicySlotWindows(
                                rangeStart, rangeEnd, slotDurationMinutes, bufferBetweenSlotsMinutes);
                    } catch (IllegalArgumentException | java.time.format.DateTimeParseException |
                             IllegalStateException e) {
                        throw new IllegalArgumentException(e.getMessage());
                    }

                    for (String[] w : windows) {
                        String error = CampusScheduleTemplateValidation.validateCampusScheduleTemplate(
                                null,
                                w[0],
                                w[1],
                                normalizedSessionType.name(),
                                day,
                                workingConfig,
                                campusScheduleTemplateRepo,
                                actorCampus,
                                slotDurationMinutes,
                                bufferBetweenSlotsMinutes);
                        if (error != null) {
                            throw new IllegalArgumentException(error);
                        }
                        saveSingleTemplate(null, day, actorCampus, w[0], w[1], normalizedSessionType);
                        campusScheduleTemplateRepo.flush();
                    }
                } else {

                    String error = CampusScheduleTemplateValidation.validateCampusScheduleTemplate(
                            request.getTemplateId(),
                            startStr,
                            endStr,
                            normalizedSessionType.name(),
                            day,
                            workingConfig,
                            campusScheduleTemplateRepo,
                            actorCampus,
                            slotDurationMinutes,
                            bufferBetweenSlotsMinutes);

                    if (error != null) {
                        throw new IllegalArgumentException(error);
                    }

                    saveSingleTemplate(request.getTemplateId(), day, actorCampus, startStr, endStr, normalizedSessionType);
                }
            }
        } catch (IllegalArgumentException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, e.getMessage(), null);
        }

        return ResponseBuilder.build(HttpStatus.OK, "Xử lý khung lịch (template) thành công.", null);
    }

    private void saveSingleTemplate(Integer templateId, String day, Campus campus, String startTime, String endTime, SessionType sessionType) {
        CampusScheduleTemplate template;

        boolean isUpdate = templateId != null && templateId > 0;

        if (isUpdate) {
            template = campusScheduleTemplateRepo.findById(templateId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khung lịch (template)."));
        } else {

            template = new CampusScheduleTemplate();
            template.setCampus(campus);
            template.setCreatedDate(LocalDate.now());
        }

        template.setDayOfWeek(day.toUpperCase());
        template.setStartTime(LocalTime.parse(startTime));
        template.setEndTime(LocalTime.parse(endTime));
        template.setSessionType(sessionType);
        template.setUpdatedDate(LocalDate.now());
        template.setActive(true);
        template.setCreatedDate(LocalDate.now());

        campusScheduleTemplateRepo.save(template);
    }

    @Override
    public ResponseEntity<ResponseObject> viewCampusScheduleTemplateByEachCampus() {

        Campus actorCampus = extractActorCampus();

        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản cơ sở trường học hoặc bạn không có quyền truy cập.", null);
        }

        List<CampusScheduleTemplate> templates = campusScheduleTemplateRepo.findByCampusIdAndActiveTrueOrderByStartTimeAsc(actorCampus.getId());

        List<String> daysOfWeekOrder = List.of("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN");

        Map<String, List<Map<String, Object>>> groupedTemplates = new LinkedHashMap<>();

        for (String day : daysOfWeekOrder) {
            groupedTemplates.put(day, new ArrayList<>());
        }

        for (CampusScheduleTemplate t : templates) {
            String day = t.getDayOfWeek().toUpperCase();

            if (groupedTemplates.containsKey(day)) {
                groupedTemplates.get(day).add(buildTemplateData(t));
            }
        }

        return ResponseBuilder.build(HttpStatus.OK, "Lấy danh sách khung lịch tư vấn theo cơ sở thành công.", groupedTemplates);
    }

    private Map<String, Object> buildTemplateData(CampusScheduleTemplate template) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", template.getId());
        data.put("dayOfWeek", template.getDayOfWeek());
        data.put("startTime", template.getStartTime().toString());
        data.put("endTime", template.getEndTime().toString());
        data.put("sessionType", template.getSessionType());
        data.put("active", template.isActive());
        data.put("campusId", template.getCampus().getId());
        return data;
    }

    @Override
    @Transactional
    public ResponseEntity<ResponseObject> syncCounsellorIntoSlots(AssignCounsellorIntoSlotsRequest request) {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Tài khoản của bạn đang bị hạn chế, không thể thực hiện thao tác này.", null);
        }

        Campus actorCampus = extractActorCampus();

        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản cơ sở trường học hoặc bạn không có quyền truy cập.", null);
        }

        SchoolConfig operatingSystemsConfig = schoolConfigRepo.findBySchoolIdAndKey(
                actorCampus.getSchool().getId(), "operationSettingsData").orElse(null);

        Map<String, Object> effectiveOperationSettings = SchoolConfigUtil.getEffectiveOperationSettingsMap(operatingSystemsConfig, actorCampus);

        String actionInput = CounsellorSlotValidation.normalizeCounsellorSlotSyncAction(request.getAction());
        if (actionInput == null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST,
                    "Tham số action là bắt buộc và phải là ASSIGN (gán) hoặc UNASSIGN (hủy gán).", null);
        }
        boolean isAssign = "ASSIGN".equals(actionInput);
        List<Integer> unassignSlotIds = resolveUnassignSlotIds(request);
        AdmissionCampaign matchedCampaign = null;
        AssignCounsellorIntoSlotsRequest effectiveRequest = request;

        if (isAssign) {
            if (request.getCampaignId() == null || request.getCampaignId() <= 0) {
                return ResponseBuilder.build(HttpStatus.BAD_REQUEST,
                        "Gán lịch: vui lòng gửi campaignId (chiến dịch tuyển sinh đang thao tác).", null);
            }

            matchedCampaign = admissionCampaignRepo.findById(request.getCampaignId()).orElse(null);
            if (matchedCampaign == null) {
                return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Không tìm thấy chiến dịch tuyển sinh với mã đã gửi.", null);
            }
            if (!matchedCampaign.getSchool().getId().equals(actorCampus.getSchool().getId())) {
                return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Chiến dịch không thuộc trường của cơ sở này.", null);
            }
            if (matchedCampaign.getStatus() != Status.OPEN_ADMISSION_CAMPAIGN) {
                return ResponseBuilder.build(HttpStatus.BAD_REQUEST,
                        "Chỉ được gán lịch khi chiến dịch đang ở trạng thái MỞ.", null);
            }
            if (matchedCampaign.getStartDate() == null || matchedCampaign.getEndDate() == null) {
                return ResponseBuilder.build(HttpStatus.BAD_REQUEST,
                        "Chiến dịch chưa cấu hình đủ khoảng ngày (startDate / endDate).", null);
            }

            // ASSIGN: luôn lấy khoảng ngày theo campaign, FE không cần gửi startDate/endDate.
            effectiveRequest = AssignCounsellorIntoSlotsRequest.builder()
                    .templateIds(request.getTemplateIds())
                    .counsellorIds(request.getCounsellorIds())
                    .slotIds(request.getSlotIds())
                    .campaignId(request.getCampaignId())
                    .action(request.getAction())
                    .startDate(matchedCampaign.getStartDate())
                    .endDate(matchedCampaign.getEndDate())
                    .build();
        } else {
            if (unassignSlotIds.isEmpty() && (request.getStartDate() == null || request.getEndDate() == null)) {
                return ResponseBuilder.build(HttpStatus.BAD_REQUEST,
                        "Hủy gán: gửi slotIds (id lịch gán), hoặc điền đủ startDate và endDate khi không dùng slotIds.", null);
            }
        }

        if (!isAssign && !unassignSlotIds.isEmpty()) {
            try {
                unassignCounsellorSlotsByIds(request, actorCampus, unassignSlotIds);
            } catch (IllegalArgumentException e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return ResponseBuilder.build(HttpStatus.BAD_REQUEST, e.getMessage(), null);
            }
            counsellorSlotRepo.flush();
            clearPersistenceContextBeforeSnapshot();
            // Post-filter: loại bỏ những slot vừa bị xóa khỏi snapshot (tránh JPA L1 cache trả cũ)
            List<Map<String, Object>> snapshotById = loadAssignedSlotsSnapshot(actorCampus.getId())
                    .stream()
                    .filter(s -> !unassignSlotIds.contains((Integer) s.get("slotId")))
                    .collect(Collectors.toList());
            Map<String, Object> bodyById = new LinkedHashMap<>();
            bodyById.put("action", actionInput);
            bodyById.put("removedSlotIds", List.copyOf(unassignSlotIds));
            bodyById.put("slots", snapshotById);
            return ResponseBuilder.build(HttpStatus.OK, "Hủy gán chuyên viên tư vấn thành công.", bodyById);
        }

        if (!isAssign && isRangeBulkUnassignRequest(request)) {
            return rangeBulkUnassignCounsellorSlots(request, actorCampus);
        }

        List<Integer> templateIdList = resolveAssignTemplateIds(request);
        if (templateIdList.isEmpty()) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Cần chỉ định templateIds (danh sách không rỗng).", null);
        }

        if (request.getCounsellorIds() == null || request.getCounsellorIds().isEmpty()) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Danh sách chuyên viên tư vấn (counsellorIds) không được để trống.", null);
        }

        List<CounsellorSlot> allCurrentSlots = counsellorSlotRepo.findByCampusScheduleTemplate_Campus_Id(actorCampus.getId());

        List<Counsellor> counsellors = counsellorRepo.findAllById(request.getCounsellorIds());

        for (Integer tid : templateIdList) {
            CampusScheduleTemplate template = campusScheduleTemplateRepo.findById(tid).orElse(null);
            if (template == null) {
                return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Không tìm thấy khung lịch (template) với mã: " + tid + ".", null);
            }
            if (!template.getCampus().getId().equals(actorCampus.getId())) {
                return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Khung lịch không thuộc cơ sở này (templateId=" + tid + ").", null);
            }

            Integer assignmentCampaignId = isAssign && matchedCampaign != null ? matchedCampaign.getId() : null;
            String error = CounsellorSlotValidation.validateAssignRequest(
                    effectiveOperationSettings, effectiveRequest, actorCampus, template, counsellors, allCurrentSlots, assignmentCampaignId);
            if (error != null) {
                return ResponseBuilder.build(HttpStatus.BAD_REQUEST, error, null);
            }
        }

        List<SchoolHoliday> holidayList = mergeSchoolHolidaysForCampus(actorCampus.getSchool().getId(), actorCampus.getId());

        if (isAssign && holidayList.isEmpty()) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST,
                    "Chưa có ngày nghỉ lễ nào được cấu hình. Vui lòng thiết lập lịch nghỉ trước khi gán tư vấn viên.", null);
        }

        try {
            for (Integer tid : templateIdList) {
                CampusScheduleTemplate template = campusScheduleTemplateRepo.findById(tid).orElse(null);
                if (template == null) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Không tìm thấy khung lịch (template) với mã: " + tid + ".", null);
                }

                for (Counsellor counsellor : counsellors) {

                    List<CounsellorSlot> counsellorSlots = allCurrentSlots.stream()
                            .filter(s -> s.getCounsellor() != null && s.getCounsellor().getId().equals(counsellor.getId()))
                            .toList();

                    if (isAssign) {
                        handleAssignAction(counsellor, template, effectiveRequest, counsellorSlots, matchedCampaign);
                    } else {
                        handleUnassignAction(template, request, counsellorSlots, counsellor);
                    }
                }
                counsellorSlotRepo.flush();
                allCurrentSlots = counsellorSlotRepo.findByCampusScheduleTemplate_Campus_Id(actorCampus.getId());
            }
        } catch (IllegalArgumentException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, e.getMessage(), null);
        }

        counsellorSlotRepo.flush();
        clearPersistenceContextBeforeSnapshot();
        List<Map<String, Object>> slotsSnapshot = loadAssignedSlotsSnapshot(actorCampus.getId());
        Map<String, Object> resultBody = new LinkedHashMap<>();
        resultBody.put("action", actionInput);
        if (isAssign) {
            Map<String, Object> campaignData = new LinkedHashMap<>();
            campaignData.put("campaignId", matchedCampaign.getId());
            campaignData.put("campaignName", matchedCampaign.getName());
            campaignData.put("startDate", matchedCampaign.getStartDate());
            campaignData.put("endDate", matchedCampaign.getEndDate());
            resultBody.put("matchedCampaign", campaignData);
        }
        resultBody.put("slots", slotsSnapshot);

        // ── Notify counsellors ─────────────────────────────────────────────────
        try {
            List<Counsellor> affectedCounsellors = counsellorRepo.findAllById(
                    request.getCounsellorIds() != null ? request.getCounsellorIds() : List.of());
            List<Account> counsellorAccounts = affectedCounsellors.stream()
                    .map(Counsellor::getAccount)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (!counsellorAccounts.isEmpty()) {
                NotificationEventType slotEvent = isAssign
                        ? NotificationEventType.COUNSELLOR_SLOT_ASSIGNED
                        : NotificationEventType.COUNSELLOR_SLOT_UNASSIGNED;

                Map<String, Object> ctx = new HashMap<>();
                ctx.put("campusName", actorCampus.getName());
                ctx.put("packageName", matchedCampaign != null ? matchedCampaign.getName() : "");
                ctx.put("specificRecipients", counsellorAccounts);

                Account actorAccount = AuthRequestUtil.extractAuthenticatedAccount();
                notificationService.publish(slotEvent, actorAccount, ctx);
            }
        } catch (Exception ignored) {
            // notification lỗi không block nghiệp vụ chính
        }

        return ResponseBuilder.build(HttpStatus.OK, (isAssign ? "Gán" : "Hủy gán") + " chuyên viên tư vấn thành công.", resultBody);
    }

    private void clearPersistenceContextBeforeSnapshot() {
        entityManager.flush();
        entityManager.clear();
    }

    private List<Map<String, Object>> loadAssignedSlotsSnapshot(Integer campusId) {
        List<CounsellorSlot> slots = counsellorSlotRepo.findByCampusScheduleTemplate_Campus_Id(campusId);
        return slots.stream()
                .filter(s -> s.getStatus() != Status.SLOT_UNASSIGNED && s.getStatus() != Status.DISABLED)
                .map(this::buildManagementSlotData)
                .toList();
    }

    private static List<Integer> resolveAssignTemplateIds(AssignCounsellorIntoSlotsRequest request) {
        if (request.getTemplateIds() == null || request.getTemplateIds().isEmpty()) {
            return List.of();
        }
        return request.getTemplateIds().stream().filter(id -> id != null).distinct().toList();
    }

    private static List<Integer> resolveUnassignSlotIds(AssignCounsellorIntoSlotsRequest request) {
        if (request.getSlotIds() == null || request.getSlotIds().isEmpty()) {
            return List.of();
        }
        return request.getSlotIds().stream().filter(Objects::nonNull).distinct().toList();
    }

    private void unassignCounsellorSlotsByIds(AssignCounsellorIntoSlotsRequest request, Campus actorCampus, List<Integer> slotIds) {

        List<Integer> allowedCounsellorIds = (request.getCounsellorIds() != null)
                ? request.getCounsellorIds().stream().filter(Objects::nonNull).distinct().toList()
                : List.of();

        for (Integer sid : slotIds) {
            CounsellorSlot slot = counsellorSlotRepo.findById(sid).orElseThrow(() -> new IllegalArgumentException(
                    "Không tìm thấy lịch gán với slotId=" + sid + "."
            ));

            if (!slot.getCampusScheduleTemplate().getCampus().getId().equals(actorCampus.getId())) {
                throw new IllegalArgumentException("slotId=" + sid + " không thuộc cơ sở của tài khoản hiện tại.");
            }
            if (!allowedCounsellorIds.isEmpty() && !allowedCounsellorIds.contains(slot.getCounsellor().getId())) {
                throw new IllegalArgumentException("slotId=" + sid + " không khớp danh sách counsellorIds.");
            }
            CounsellorSlotValidation.assertNoBlockingConsultationForUnassignDelete(slot);
            safeDeleteSlot(slot);
        }
    }

    private static boolean isRangeBulkUnassignRequest(AssignCounsellorIntoSlotsRequest request) {
        if (request.getCounsellorIds() == null || request.getCounsellorIds().size() != 1) {
            return false;
        }
        if (request.getTemplateIds() != null && !request.getTemplateIds().isEmpty()) {
            return false;
        }
        if (request.getStartDate() == null || request.getEndDate() == null) {
            return false;
        }
        return !request.getStartDate().isAfter(request.getEndDate());
    }

    private ResponseEntity<ResponseObject> rangeBulkUnassignCounsellorSlots(AssignCounsellorIntoSlotsRequest request, Campus actorCampus) {

        Integer counsellorId = request.getCounsellorIds().get(0);
        LocalDate rangeStart = request.getStartDate();
        LocalDate rangeEnd = request.getEndDate();

        Counsellor counsellor = counsellorRepo.findById(counsellorId).orElse(null);
        if (counsellor == null || counsellor.getCampus() == null || !counsellor.getCampus().getId().equals(actorCampus.getId())) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Chuyên viên không tồn tại hoặc không thuộc cơ sở này.", null);
        }

        List<CounsellorSlot> slots = counsellorSlotRepo.findByCampusScheduleTemplate_Campus_IdAndCounsellor_Id(actorCampus.getId(), counsellorId);
        List<CounsellorSlot> inRange = slots.stream()
                .filter(s -> s.getStatus() != Status.SLOT_UNASSIGNED)
                .filter(s -> rangesOverlap(s.getStartDate(), s.getEndDate(), rangeStart, rangeEnd))
                .toList();

        if (inRange.isEmpty()) {
            return ResponseBuilder.build(HttpStatus.OK, "Không có lịch gán nào trong khoảng ngày đã chọn.", Map.of(
                    "deletedSlotIds", List.of(),
                    "blockedSlotIds", List.of(),
                    "blockedAppointmentDates", List.of(),
                    "totalInRange", 0,
                    "deletedCount", 0,
                    "blockedCount", 0
            ));
        }

        List<Integer> deletedIds = new ArrayList<>();
        List<Integer> blockedIds = new ArrayList<>();
        TreeSet<LocalDate> blockedApptDates = new TreeSet<>();

        for (CounsellorSlot slot : inRange) {
            if (CounsellorSlotValidation.hasBlockingConsultationRequests(slot)) {
                blockedIds.add(slot.getId());
                List<ConsultationOfflineRequest> reqs = slot.getConsultationOfflineRequests();
                if (reqs != null) {
                    for (ConsultationOfflineRequest r : reqs) {
                        if (CounsellorSlotValidation.BLOCKING_CONSULTATION_STATUSES_FOR_UNASSIGN.contains(r.getStatus())
                                && r.getAppointmentDate() != null) {
                            blockedApptDates.add(r.getAppointmentDate());
                        }
                    }
                }
            } else {
                safeDeleteSlot(slot);
                deletedIds.add(slot.getId());
            }
        }
        counsellorSlotRepo.flush();
        clearPersistenceContextBeforeSnapshot();

        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String datesStr = blockedApptDates.stream().map(d -> d.format(df)).collect(Collectors.joining(", "));

        int total = inRange.size();
        int deletedCount = deletedIds.size();
        int blockedCount = blockedIds.size();

        String message;
        if (blockedCount == 0) {
            message = String.format("Đã xóa %d lịch gán trong khoảng ngày đã chọn.", deletedCount);
        } else if (deletedCount == 0) {
            message = String.format(
                    "Có %d lịch gán trong khoảng này; không thể xóa do có lịch hẹn với phụ huynh%s.",
                    total,
                    datesStr.isEmpty() ? "" : " (các ngày: " + datesStr + ")");
        } else {
            message = String.format(
                    "Bạn có %d lịch gán trong khoảng này. %d lịch đã được xóa thành công. %d lịch không thể xóa do có lịch hẹn với phụ huynh%s.",
                    total,
                    deletedCount,
                    blockedCount,
                    datesStr.isEmpty() ? "." : " (các ngày: " + datesStr + ").");
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("counsellorId", counsellorId);
        body.put("rangeStart", rangeStart.toString());
        body.put("rangeEnd", rangeEnd.toString());
        body.put("totalInRange", total);
        body.put("deletedCount", deletedCount);
        body.put("blockedCount", blockedCount);
        body.put("deletedSlotIds", deletedIds);
        body.put("blockedSlotIds", blockedIds);
        body.put("blockedAppointmentDates", blockedApptDates.stream().map(LocalDate::toString).toList());
        body.put("slots", loadAssignedSlotsSnapshot(actorCampus.getId()));

        return ResponseBuilder.build(HttpStatus.OK, message, body);
    }

    private static boolean rangesOverlap(LocalDate aStart, LocalDate aEnd, LocalDate bStart, LocalDate bEnd) {
        return !aStart.isAfter(bEnd) && !aEnd.isBefore(bStart);
    }

    private void handleAssignAction(
            Counsellor counsellor,
            CampusScheduleTemplate template,
            AssignCounsellorIntoSlotsRequest request,
            List<CounsellorSlot> existingSlots,
            AdmissionCampaign assignCampaign) {

        for (CounsellorSlot slot : existingSlots) {
            boolean sameCampaign = assignCampaign == null
                    || slot.getAdmissionCampaign() == null
                    || slot.getAdmissionCampaign().getId().equals(assignCampaign.getId());
            boolean sameWindow = slot.getCampusScheduleTemplate().getId().equals(template.getId())
                    && slot.getStartDate().equals(request.getStartDate())
                    && slot.getEndDate().equals(request.getEndDate())
                    && sameCampaign;
            if (sameWindow) {
                if (slot.getStatus() == Status.SLOT_UNASSIGNED) {
                    if (assignCampaign != null && slot.getAdmissionCampaign() == null) {
                        slot.setAdmissionCampaign(assignCampaign);
                    }
                    slot.setStatus(Status.AVAILABLE);
                    counsellorSlotRepo.save(slot);
                    return;
                }
                return;
            }
        }

        for (CounsellorSlot slot : existingSlots) {
            if (slot.getStatus() == Status.SLOT_UNASSIGNED) {
                continue;
            }
            if (assignCampaign != null && slot.getAdmissionCampaign() != null
                    && !slot.getAdmissionCampaign().getId().equals(assignCampaign.getId())) {
                continue;
            }
            boolean isDateOverlap = request.getStartDate().isBefore(slot.getEndDate().plusDays(1)) && request.getEndDate().isAfter(slot.getStartDate().minusDays(1));

            boolean isDayOfWeekSame = slot.getCampusScheduleTemplate().getDayOfWeek().equalsIgnoreCase(template.getDayOfWeek());

            boolean isTimeOverlap = template.getStartTime().isBefore(slot.getCampusScheduleTemplate().getEndTime()) && template.getEndTime().isAfter(slot.getCampusScheduleTemplate().getStartTime());

            if (isDateOverlap && isDayOfWeekSame && isTimeOverlap) {
                throw new IllegalArgumentException("Chuyên viên " + counsellor.getName() + " đã có lịch trùng khoảng thời gian hoặc khung giờ này trong cùng chiến dịch.");
            }
        }
        counsellorSlotRepo.save(CounsellorSlot.builder()
                .campusScheduleTemplate(template)
                .counsellor(counsellor)
                .admissionCampaign(assignCampaign)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(Status.AVAILABLE)
                .build());
    }

    private void handleUnassignAction(CampusScheduleTemplate template, AssignCounsellorIntoSlotsRequest request, List<CounsellorSlot> existingSlots, Counsellor counsellor) {

        CounsellorSlot targetSlot = existingSlots.stream()
                .filter(s -> s.getStatus() != Status.SLOT_UNASSIGNED)
                .filter(s -> s.getCampusScheduleTemplate().getId().equals(template.getId())
                        && s.getStartDate().equals(request.getStartDate())
                        && s.getEndDate().equals(request.getEndDate()))
                .filter(s -> request.getCampaignId() == null
                        || (s.getAdmissionCampaign() != null && request.getCampaignId().equals(s.getAdmissionCampaign().getId())))
                .findFirst()
                .orElse(null);

        if (targetSlot == null) {
            throw new IllegalArgumentException(
                    "Không tìm thấy lịch gán của " + counsellor.getName() + " cho khung lịch này trong khoảng ngày đã chọn"
                            + (request.getCampaignId() != null ? " (campaignId=" + request.getCampaignId() + ")." : "."));
        }
        CounsellorSlotValidation.assertNoBlockingConsultationForUnassignDelete(targetSlot);
        safeDeleteSlot(targetSlot);
    }

    private void safeDeleteSlot(CounsellorSlot slot) {
        // Step 1: Nullify FK trên consultation → giữ lịch sử, tránh FK violation
        List<ConsultationOfflineRequest> linked = consultationOfflineRequestRepo.findByCounsellorSlotId(slot.getId());
        if (!linked.isEmpty()) {
            linked.forEach(c -> c.setCounsellorSlot(null));
            consultationOfflineRequestRepo.saveAll(linked);
            consultationOfflineRequestRepo.flush();
        }
        // Step 2: Tách slot khỏi Counsellor.counsellorSlotList (EAGER + CascadeType.ALL)
        // Nếu không làm bước này, Hibernate flush Counsellor thấy list vẫn còn slot
        // → re-persist lại slot vừa delete → DB không thay đổi
        Counsellor counsellor = slot.getCounsellor();
        if (counsellor != null && counsellor.getCounsellorSlotList() != null) {
            counsellor.getCounsellorSlotList().remove(slot);
        }
        // Step 3: Delete slot
        counsellorSlotRepo.delete(slot);
        counsellorSlotRepo.flush();
    }

    //phần gộp nghỉ toàn trường + nghỉ theo cơ sở xử lý ở đây.
    private List<SchoolHoliday> mergeSchoolHolidaysForCampus(Integer schoolId, Integer campusId) {
        List<SchoolHoliday> global = schoolHolidayRepo.findBySchoolIdAndCampusIsNull(schoolId);
        List<SchoolHoliday> local = schoolHolidayRepo.findBySchoolIdAndCampusId(schoolId, campusId);
        Map<Integer, SchoolHoliday> merged = new LinkedHashMap<>();
        for (SchoolHoliday h : global) {
            merged.put(h.getId(), h);
        }
        for (SchoolHoliday h : local) {
            merged.put(h.getId(), h);
        }
        return new ArrayList<>(merged.values());
    }

    @Override
    public ResponseEntity<ResponseObject> getAvailableSlots(LocalDate targetDate, Integer campaignId) {

        Campus actorCampus = extractActorCampus();
        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản cơ sở trường học hoặc bạn không có quyền truy cập.", null);
        }

        AdmissionCampaign filterCampaign = null;
        if (campaignId != null && campaignId > 0) {
            filterCampaign = admissionCampaignRepo.findById(campaignId).orElse(null);
            if (filterCampaign == null) {
                return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Không tìm thấy chiến dịch tuyển sinh.", null);
            }
            if (!filterCampaign.getSchool().getId().equals(actorCampus.getSchool().getId())) {
                return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Chiến dịch không thuộc trường của cơ sở này.", null);
            }
            if (filterCampaign.getStatus() != Status.OPEN_ADMISSION_CAMPAIGN) {
                return ResponseBuilder.build(HttpStatus.BAD_REQUEST,
                        "Chỉ hiển thị lịch đặt khi chiến dịch đang MỞ. Chiến dịch này không ở trạng thái MỞ.", null);
            }
            if (filterCampaign.getStartDate() != null && filterCampaign.getEndDate() != null
                    && (targetDate.isBefore(filterCampaign.getStartDate()) || targetDate.isAfter(filterCampaign.getEndDate()))) {
                return ResponseBuilder.build(HttpStatus.BAD_REQUEST, String.format(
                        "Ngày xem lịch (%s) nằm ngoài khoảng chiến dịch (%s → %s).",
                        targetDate, filterCampaign.getStartDate(), filterCampaign.getEndDate()), null);
            }
        }

        String dayOfWeek = targetDate.getDayOfWeek().name().substring(0, 3);

        SchoolConfig hqOp = schoolConfigRepo.findBySchoolIdAndKey(actorCampus.getSchool().getId(), "operationSettingsData").orElse(null);
        Map<String, Object> effectiveOperation = SchoolConfigUtil.getEffectiveOperationSettingsMap(hqOp, actorCampus);
        List<SchoolHoliday> holidays = mergeSchoolHolidaysForCampus(actorCampus.getSchool().getId(), actorCampus.getId());

        List<CounsellorSlot> assignedSlots = counsellorSlotRepo.findByStartDateLessThanEqualAndEndDateGreaterThanEqualAndCampusScheduleTemplate_DayOfWeekAndCampusScheduleTemplate_Campus_IdAndCampusScheduleTemplate_ActiveTrue(targetDate, // khớp với StartDateLessThanEqual
                targetDate, dayOfWeek, actorCampus.getId());

        if (filterCampaign != null) {
            final int cid = filterCampaign.getId();
            assignedSlots = assignedSlots.stream()
                    .filter(s -> s.getAdmissionCampaign() != null && s.getAdmissionCampaign().getId().equals(cid))
                    .toList();
        }

        Map<String, List<Map<String, Object>>> groupedByTime = new LinkedHashMap<>();

        for (CounsellorSlot slot : assignedSlots) {
            if (slot.getStatus() != null && slot.getStatus() != Status.AVAILABLE) {
                continue;
            }
            if (!isSlotAvailable(slot, targetDate)) {
                continue;
            }
            CampusScheduleTemplate t = slot.getCampusScheduleTemplate();
            String policyError = SchoolConfigUtil.validateSlotAvailability(
                    targetDate,
                    t.getStartTime(),
                    t.getEndTime(),
                    t.getSessionType().getValue(),
                    effectiveOperation,
                    holidays);
            if (policyError != null) {
                continue;
            }

            String startTimeKey = t.getStartTime().toString();

            groupedByTime.putIfAbsent(startTimeKey, new ArrayList<>());

            Map<String, Object> slotData = buildCounsellorSlotData(slot);
            groupedByTime.get(startTimeKey).add(slotData);
        }

        return ResponseBuilder.build(HttpStatus.OK, "Lấy các khung giờ có chuyên viên rảnh theo ngày thành công.", groupedByTime);
    }

    private Map<String, Object> buildCounsellorSlotData(CounsellorSlot slot) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("slotId", slot.getId());

        CampusScheduleTemplate template = slot.getCampusScheduleTemplate();
        data.put("startTime", template.getStartTime().toString());
        data.put("endTime", template.getEndTime().toString());
        data.put("dayOfWeek", template.getDayOfWeek());

        Map<String, Object> counsellorData = new LinkedHashMap<>();
        counsellorData.put("id", slot.getCounsellor().getId());
        counsellorData.put("name", slot.getCounsellor().getName());
        counsellorData.put("avatar", slot.getCounsellor().getAvatar());

        data.put("counsellor", counsellorData);

        if (slot.getAdmissionCampaign() != null) {
            Map<String, Object> campaignData = new LinkedHashMap<>();
            campaignData.put("campaignId", slot.getAdmissionCampaign().getId());
            campaignData.put("campaignName", slot.getAdmissionCampaign().getName());
            campaignData.put("campaignStartDate", slot.getAdmissionCampaign().getStartDate() != null
                    ? slot.getAdmissionCampaign().getStartDate().toString() : null);
            campaignData.put("campaignEndDate", slot.getAdmissionCampaign().getEndDate() != null
                    ? slot.getAdmissionCampaign().getEndDate().toString() : null);
            data.put("campaign", campaignData);
        }

        return data;
    }

    private boolean isSlotAvailable(CounsellorSlot slot, LocalDate targetDate) {
        return slot.getConsultationOfflineRequests().stream().noneMatch(req ->
                req.getAppointmentDate() != null
                        && req.getAppointmentDate().equals(targetDate)
                        && CounsellorSlotValidation.BLOCKING_CONSULTATION_STATUSES_FOR_UNASSIGN.contains(req.getStatus()));
    }

    @Override
    public ResponseEntity<ResponseObject> getAssignedSlots(Integer counsellorId) {

        Campus actorCampus = extractActorCampus();
        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản cơ sở trường học hoặc bạn không có quyền truy cập.", null);
        }

        List<CounsellorSlot> slots = (counsellorId != null) ? counsellorSlotRepo.findByCampusScheduleTemplate_Campus_IdAndCounsellor_Id(actorCampus.getId(), counsellorId) : counsellorSlotRepo.findByCampusScheduleTemplate_Campus_Id(actorCampus.getId());

        List<Map<String, Object>> responseList = slots.stream()
                .filter(s -> s.getStatus() != Status.SLOT_UNASSIGNED)
                .map(this::buildManagementSlotData)
                .toList();

        return ResponseBuilder.build(HttpStatus.OK, "Lấy danh sách lịch gán tư vấn thành công.", responseList);
    }

    private Map<String, Object> buildManagementSlotData(CounsellorSlot slot) {
        Map<String, Object> data = new LinkedHashMap<>();

        data.put("slotId", slot.getId());

        data.put("startDate", slot.getStartDate().toString());
        data.put("endDate", slot.getEndDate().toString());

        CampusScheduleTemplate template = slot.getCampusScheduleTemplate();
        Map<String, Object> templateData = new LinkedHashMap<>();
        templateData.put("templateId", template.getId());
        templateData.put("dayOfWeek", template.getDayOfWeek());
        templateData.put("time", template.getStartTime() + " - " + template.getEndTime());
        data.put("schedule", templateData);

        Map<String, Object> counsellorData = new LinkedHashMap<>();
        counsellorData.put("id", slot.getCounsellor().getId());
        counsellorData.put("name", slot.getCounsellor().getName());
        counsellorData.put("email", slot.getCounsellor().getAccount().getEmail());
        data.put("counsellor", counsellorData);

        if (slot.getAdmissionCampaign() != null) {
            Map<String, Object> campaignData = new LinkedHashMap<>();
            campaignData.put("campaignId", slot.getAdmissionCampaign().getId());
            campaignData.put("campaignName", slot.getAdmissionCampaign().getName());
            data.put("campaign", campaignData);
        }

        return data;
    }

    @Override
    public ResponseEntity<ResponseObject> getCounsellorAvailableList() {

        Campus actorCampus = extractActorCampus();
        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản cơ sở trường học hoặc bạn không có quyền truy cập.", null);
        }

        List<Counsellor> counsellorList = counsellorRepo.findByCampus_IdAndAccount_Status(actorCampus.getId(), Status.ACCOUNT_ACTIVE);

        List<Map<String, Object>> responseList = counsellorList.stream().map(this::buildCounsellor).toList();

        return ResponseBuilder.build(HttpStatus.OK, "Lấy danh sách chuyên viên tư vấn khả dụng thành công.", responseList);
    }

    private Map<String, Object> buildCounsellor(Counsellor counsellor) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", counsellor.getId());
        data.put("name", counsellor.getName());
        data.put("email", (counsellor.getAccount() != null) ? counsellor.getAccount().getEmail() : "Chưa có tài khoản");
        return data;
    }

    @Override
    public ResponseEntity<Resource> exportCounsellorList() throws IOException {

        Campus actorCampus = extractActorCampus();

        if (actorCampus == null) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        List<Counsellor> counsellors = counsellorRepo.findAll();

        Path path = Files.createTempFile("export_counsellors_", ".xlsx");

        String[] headers = {
                "ID",
                "Mã Nhân Viên",
                "Họ Tên",
                "Email",
                "Trạng Thái",
                "Tên Trường",
                "Cơ Sở (Campus)"
        };

        ExcelUtil.exportToExcel(path, "Counsellors", headers, counsellors, (counsellor, row) -> {
            Account acc = counsellor.getAccount();
            Campus campus = counsellor.getCampus();
            School school = (campus != null) ? campus.getSchool() : null;

            row.createCell(0).setCellValue(counsellor.getId());
            row.createCell(1).setCellValue(String.valueOf(counsellor.getEmployeeCode()));
            row.createCell(2).setCellValue(counsellor.getName());
            row.createCell(3).setCellValue(acc != null ? acc.getEmail() : "Không có");
            row.createCell(4).setCellValue(acc != null ? acc.getStatus().toString() : "Không có");

            row.createCell(5).setCellValue(school != null ? school.getName() : "Không xác định");
            row.createCell(6).setCellValue(campus != null ? campus.getName() : "Không xác định");
        });

        return buildFileResponse(path, "Danh_Sach_Tu_Van_Vien.xlsx");
    }

    @Override
    public ResponseEntity<Resource> exportCampusScheduleMatrix() throws IOException {

        Campus actorCampus = extractActorCampus();

        if (actorCampus == null) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        List<CampusScheduleTemplate> templates = campusScheduleTemplateRepo
                .findByCampusIdAndActiveTrueOrderByStartTimeAsc(actorCampus.getId());

        List<String> daysOfWeek = List.of("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN");

        Map<String, Map<String, String>> matrix = new LinkedHashMap<>();

        for (CampusScheduleTemplate t : templates) {
            String timeSlot = t.getStartTime().toString() + " - " + t.getEndTime().toString();
            String day = t.getDayOfWeek().toUpperCase();

            matrix.putIfAbsent(timeSlot, new HashMap<>());

            matrix.get(timeSlot).put(day, t.getSessionType().toString());
        }


        Path path = Files.createTempFile("schedule_matrix_", ".xlsx");

        String[] headers = new String[8];
        headers[0] = "Khung giờ";
        for (int i = 0; i < daysOfWeek.size(); i++) {
            headers[i + 1] = translateDayOfWeek(daysOfWeek.get(i));
        }

        List<Map.Entry<String, Map<String, String>>> rowData = new ArrayList<>(matrix.entrySet());

        ExcelUtil.exportToExcel(path, "Thoi_Khoa_Bieu", headers, rowData, (entry, row) -> {
            String timeSlot = entry.getKey();
            Map<String, String> dayValues = entry.getValue();

            row.createCell(0).setCellValue(timeSlot);

            for (int i = 0; i < daysOfWeek.size(); i++) {
                String day = daysOfWeek.get(i);
                String sessionValue = dayValues.getOrDefault(day, "-"); // "-" nếu trống
                row.createCell(i + 1).setCellValue(sessionValue);
            }
        });

        return buildFileResponse(path, "Thoi_Khoa_Bieu_" + actorCampus.getName() + ".xlsx");
    }

    @Override
    public ResponseEntity<ResponseObject> getDocuments() {
        Campus campus = extractActorCampus();
        String folderPath = campus.getSchool().getFolderPath();

        try {
            StorageTreeNode result = supabaseStorageService.getStorageTree(folderPath);
            return ResponseBuilder.build(HttpStatus.OK, "Lấy danh sách tài liệu thành công.", result);
        } catch (Exception ex) {
            return ResponseBuilder.build(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), null);
        }
    }

    @Override
    public ResponseEntity<ResponseObject> getChatHistoryWithAdmin(Long cursorId) {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Tài khoản của bạn đang bị hạn chế, không thể thực hiện thao tác này.", null);
        }

        Campus campus = extractActorCampus();

        Account accAdmin = accountRepo.findByRole(Role.ADMIN).get(0);

        if (accAdmin == null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Không tìm thấy tài khoản quản trị viên hoặc tài khoản đã bị xóa.", null);
        }

        Optional<Conversation> existingConversation = conversationRepo.findByCampusIdAndAccAdminId(campus.getId(), accAdmin.getId());

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

            return ResponseBuilder.build(HttpStatus.OK, "Lấy lịch sử trò chuyện thành công.", buildHistoryMessages(existingConversation.get(), accAdmin.getEmail(), campus.getAccount().getEmail(), messages, hasMore, nextCursorId));

        }

        Conversation conversation = Conversation.builder()
                .campusId(campus.getId())
                .accAdminId(accAdmin.getId())
//                 .status(Status.CONVERSATION_ACTIVE)
                .createdDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .build();

//        conversationRepo.save(conversation);

        return ResponseBuilder.build(HttpStatus.OK, "Lấy lịch sử trò chuyện thành công.", buildHistoryMessages(conversation, accAdmin.getEmail(), campus.getAccount().getEmail(), messages, hasMore, nextCursorId));
    }

    @Override
    public ResponseEntity<ResponseObject> createConversationWithAdmin() {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Tài khoản của bạn đang bị hạn chế, không thể thực hiện thao tác này.", null);
        }

        Campus campus = extractActorCampus();

        if (campus == null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Không tìm thấy tài khoản cơ sở hoặc tài khoản đã bị xóa.", null);
        }

        Account accAdmin = accountRepo.findByRole(Role.ADMIN).get(0);

        if (accAdmin == null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Không tìm thấy tài khoản quản trị viên hoặc tài khoản đã bị xóa.", null);
        }

        Conversation conversation = Conversation.builder()
                .campusId(campus.getId())
                .accAdminId(accAdmin.getId())
                .status(Status.CONVERSATION_ACTIVE)
                .createdDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .build();

        conversationRepo.save(conversation);

        return ResponseBuilder.build(HttpStatus.OK, "Tạo cuộc trò chuyện với quản trị viên thành công.", conversation.getId());
    }

    @Override
    public ResponseEntity<ResponseObject> getConversation() {

        Campus campus = extractActorCampus();

        if (campus == null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST,
                    "Không tìm thấy tài khoản cơ sở hoặc tài khoản đã bị xóa.", null);
        }

        List<Account> admins = accountRepo.findByRole(Role.ADMIN);
        if (admins == null || admins.isEmpty()) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST,
                    "Không tìm thấy tài khoản quản trị viên hoặc tài khoản đã bị xóa.", null);
        }

        Account accAdmin = admins.get(0);

        Optional<Conversation> conversationOpt =
                conversationRepo.findByCampusIdAndAccAdminId(campus.getId(), accAdmin.getId());

        if (conversationOpt.isEmpty()) {
            Map<String, Object> data = new HashMap<>();
            data.put("conversationId", null);
            data.put("hasNewMessage", false);
            data.put("unreadCount", 0);
            return ResponseBuilder.build(HttpStatus.OK, "Lấy thông tin cuộc trò chuyện thành công.", data);
        }

        Conversation conversation = conversationOpt.get();

        Long unreadCount = chatMessageRepo
                .countByConversationIdAndReceiverNameAndStatusNot(
                        conversation.getId(),
                        campus.getAccount().getEmail(),
                        Status.MESSAGE_READ
                );

        Map<String, Object> data = new HashMap<>();
        data.put("conversationId", conversation.getId());
        data.put("hasNewMessage", unreadCount > 0);
        data.put("unreadCount", unreadCount);

        return ResponseBuilder.build(HttpStatus.OK, "Lấy thông tin cuộc trò chuyện thành công.", data);
    }

    @Override
    public ResponseEntity<ResponseObject> chatWithChatbotForSchool(ChatMessageForChatBot messageForChatBot) {

        Campus actorCampus = extractActorCampus();

        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản cơ sở trường học hoặc bạn không có quyền truy cập.", null);
        }

        Optional<SchoolSubscription> activeSubOpt = schoolSubscriptionRepo.findBySchoolIdAndIsSelected(actorCampus.getSchool().getId(), true).stream().findFirst(); // tại 1 thời điểm chỉ có 1 gói đc active

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
        payload.put("schoolId", actorCampus.getSchool().getId());
        payload.put("sessionId", actorCampus.getAccount().getEmail());

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

    private Map<String, Object> buildHistoryMessages(Conversation conversation, String emailAdmin, String campusEmail, List<ChatMessage> messages, boolean hasMore, Long nextCursorId) {

        Map<String, Object> response = new HashMap<>();

        response.put("conversationId", conversation.getId());
        response.put("accAdminId", conversation.getAccAdminId());
        response.put("emailAdmin", emailAdmin);
        response.put("campusId", conversation.getCampusId());
        campusRepo.findById(conversation.getCampusId())
                .ifPresentOrElse(campus -> {
                    response.put("campusName", campus.getName());
                    response.put("schoolName", campus.getSchool().getName());
                }, () -> {
                    response.put("campusName", null);
                    response.put("schoolName", null);
                });

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

    private String translateDayOfWeek(String day) {
        if (day == null) return "";
        return switch (day.toUpperCase()) {
            case "MON" -> "Thứ Hai";
            case "TUE" -> "Thứ Ba";
            case "WED" -> "Thứ Tư";
            case "THU" -> "Thứ Năm";
            case "FRI" -> "Thứ Sáu";
            case "SAT" -> "Thứ Bảy";
            case "SUN" -> "Chủ Nhật";
            default -> day;
        };
    }

    @Override
    public ResponseEntity<ResponseObject> getConsultationStats(String period, LocalDate from, LocalDate to) {

        Campus actorCampus = extractActorCampus();
        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND,
                    "Không tìm thấy tài khoản cơ sở trường học hoặc bạn không có quyền truy cập.", null);
        }

        LocalDate[] range = resolveConsultationDateRange(period, from, to);
        if (range == null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST,
                    "Period không hợp lệ. Giá trị hợp lệ: THIS_WEEK, THIS_MONTH, THIS_QUARTER, THIS_YEAR, CUSTOM. "
                            + "Khi chọn CUSTOM cần truyền thêm from và to.", null);
        }
        LocalDate dateFrom = range[0];
        LocalDate dateTo = range[1];
        String normalizedPeriod = (period != null && !period.isBlank()) ? period.trim().toUpperCase() : "CUSTOM";
        boolean isPrimaryBranch = Boolean.TRUE.equals(actorCampus.getIsPrimaryBranch());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("period", normalizedPeriod);
        result.put("from", dateFrom.toString());
        result.put("to", dateTo.toString());
        result.put("isPrimaryBranch", isPrimaryBranch);

        if (isPrimaryBranch) {
            List<Campus> allCampuses = campusRepo.findAllBySchoolId(actorCampus.getSchool().getId());
            List<Integer> campusIds = allCampuses.stream().map(Campus::getId).collect(Collectors.toList());

            List<ConsultationOfflineRequest> allRequests =
                    consultationOfflineRequestRepo.findByCampusIdInAndAppointmentDateBetween(campusIds, dateFrom, dateTo);

            result.put("cards", buildConsultationCards(allRequests));

            result.put("trend", buildConsultationTrend(allRequests, dateFrom, dateTo, normalizedPeriod));

            result.put("byDayOfWeek", buildConsultationByDayOfWeek(allRequests));

            Integer schoolId = actorCampus.getSchool().getId();
            long totalCampuses = campusRepo.countBySchoolId(schoolId);
            long totalCounsellors = counsellorRepo.countByCampusSchoolId(schoolId);
            // Đếm active counsellors toàn trường
            long activeCounsellorsSchool = allCampuses.stream()
                    .mapToLong(c -> counsellorRepo.findByCampus_IdAndAccount_Status(
                            c.getId(), com.sp26se041.edubridgehcm.enums.Status.ACTIVE).size())
                    .sum();

            Map<String, Object> schoolSummary = new LinkedHashMap<>();
            schoolSummary.put("totalCampuses", totalCampuses);
            schoolSummary.put("totalCounsellors", totalCounsellors);
            schoolSummary.put("activeCounsellors", activeCounsellorsSchool);
            schoolSummary.put("totalConsultationsInPeriod", (long) allRequests.size());
            result.put("schoolSummary", schoolSummary);

            // 5. So sánh từng cơ sở (bar chart)
            Map<Integer, List<ConsultationOfflineRequest>> byCampusId = allRequests.stream()
                    .collect(Collectors.groupingBy(r -> r.getCampus().getId()));

            List<Map<String, Object>> byCampusList = new ArrayList<>();
            for (Campus c : allCampuses) {
                List<ConsultationOfflineRequest> campusReqs =
                        byCampusId.getOrDefault(c.getId(), Collections.emptyList());
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("campusId", c.getId());
                entry.put("campusName", c.getName());
                entry.put("isPrimaryBranch", Boolean.TRUE.equals(c.getIsPrimaryBranch()));
                entry.put("counsellorCount", counsellorRepo.countByCampusId(c.getId()));
                entry.put("activeCounsellorCount",
                        (long) counsellorRepo.findByCampus_IdAndAccount_Status(
                                c.getId(), com.sp26se041.edubridgehcm.enums.Status.ACTIVE).size());
                entry.putAll(buildConsultationCards(campusReqs));
                byCampusList.add(entry);
            }
            result.put("byCampus", byCampusList);

        } else {
            // ── Campus chi nhánh: chỉ dữ liệu của cơ sở đó ──
            List<ConsultationOfflineRequest> requests =
                    consultationOfflineRequestRepo.findByCampusIdAndAppointmentDateBetween(
                            actorCampus.getId(), dateFrom, dateTo);

            result.put("cards", buildConsultationCards(requests));
            result.put("trend", buildConsultationTrend(requests, dateFrom, dateTo, normalizedPeriod));
            result.put("byDayOfWeek", buildConsultationByDayOfWeek(requests));
        }

        return ResponseBuilder.build(HttpStatus.OK, "Lấy thống kê tư vấn thành công.", result);
    }

    private LocalDate[] resolveConsultationDateRange(String period, LocalDate from, LocalDate to) {
        if (period == null || period.isBlank()) {
            if (from != null && to != null) return new LocalDate[]{from, to};
            return null;
        }
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        return switch (period.trim().toUpperCase()) {
            case "THIS_WEEK" -> {
                LocalDate start = today.with(java.time.DayOfWeek.MONDAY);
                LocalDate end = today.with(java.time.DayOfWeek.SUNDAY);
                yield new LocalDate[]{start, end};
            }
            case "THIS_MONTH" -> {
                LocalDate start = today.withDayOfMonth(1);
                LocalDate end = today.withDayOfMonth(today.lengthOfMonth());
                yield new LocalDate[]{start, end};
            }
            case "THIS_QUARTER" -> {
                int q = (today.getMonthValue() - 1) / 3;
                LocalDate start = today.withMonth(q * 3 + 1).withDayOfMonth(1);
                LocalDate end = start.plusMonths(2).withDayOfMonth(start.plusMonths(2).lengthOfMonth());
                yield new LocalDate[]{start, end};
            }
            case "THIS_YEAR" -> {
                LocalDate start = today.withDayOfYear(1);
                LocalDate end = today.withDayOfYear(today.lengthOfYear());
                yield new LocalDate[]{start, end};
            }
            case "CUSTOM" -> {
                if (from == null || to == null || from.isAfter(to)) yield null;
                yield new LocalDate[]{from, to};
            }
            default -> null;
        };
    }

    private Map<String, Object> buildConsultationCards(List<ConsultationOfflineRequest> requests) {
        long pending = 0, confirmed = 0, inProgress = 0, completed = 0, cancelled = 0, noShow = 0;
        for (ConsultationOfflineRequest r : requests) {
            switch (r.getStatus()) {
                case CONSULTATION_PENDING -> pending++;
                case CONSULTATION_CONFIRMED -> confirmed++;
                case CONSULTATION_IN_PROGRESS -> inProgress++;
                case CONSULTATION_COMPLETED -> completed++;
                case CONSULTATION_CANCELLED -> cancelled++;
                case CONSULTATION_NO_SHOW -> noShow++;
                default -> {
                }
            }
        }
        long total = requests.size();
        long finalized = completed + cancelled + noShow;
        long responded = total - pending;

        Map<String, Object> cards = new LinkedHashMap<>();
        cards.put("total", total);
        cards.put("pending", pending);
        cards.put("confirmed", confirmed);
        cards.put("inProgress", inProgress);
        cards.put("completed", completed);
        cards.put("cancelled", cancelled);
        cards.put("noShow", noShow);
        // Rates
        cards.put("completionRate", pct(completed, finalized));   // % hoàn thành
        cards.put("cancellationRate", pct(cancelled, responded));   // % huỷ
        return cards;
    }

    private List<Map<String, Object>> buildConsultationTrend(
            List<ConsultationOfflineRequest> requests,
            LocalDate dateFrom, LocalDate dateTo, String normalizedPeriod) {

        boolean byMonth = "THIS_YEAR".equals(normalizedPeriod)
                || ("CUSTOM".equals(normalizedPeriod) && !dateFrom.plusMonths(3).isAfter(dateTo));
        boolean byWeek = "THIS_QUARTER".equals(normalizedPeriod);

        List<Map<String, Object>> trend = new ArrayList<>();

        if (byMonth) {
            // Group by yyyy-MM
            Map<String, List<ConsultationOfflineRequest>> grouped = requests.stream()
                    .collect(Collectors.groupingBy(
                            r -> r.getAppointmentDate().format(DateTimeFormatter.ofPattern("yyyy-MM"))));

            LocalDate cursor = dateFrom.withDayOfMonth(1);
            while (!cursor.isAfter(dateTo)) {
                String key = cursor.format(DateTimeFormatter.ofPattern("yyyy-MM"));
                String label = cursor.format(DateTimeFormatter.ofPattern("MM/yyyy"));
                trend.add(buildTrendEntry(label, grouped.getOrDefault(key, Collections.emptyList())));
                cursor = cursor.plusMonths(1);
            }

        } else if (byWeek) {
            // Weekly buckets (Monday → Sunday)
            LocalDate weekStart = dateFrom.with(java.time.DayOfWeek.MONDAY);
            if (weekStart.isAfter(dateFrom)) weekStart = weekStart.minusWeeks(1);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");

            while (!weekStart.isAfter(dateTo)) {
                LocalDate weekEnd = weekStart.plusDays(6);
                String label = weekStart.format(fmt) + " - " + weekEnd.format(fmt);
                final LocalDate ws = weekStart;
                final LocalDate we = weekEnd;
                List<ConsultationOfflineRequest> group = requests.stream()
                        .filter(r -> !r.getAppointmentDate().isBefore(ws)
                                && !r.getAppointmentDate().isAfter(we))
                        .collect(Collectors.toList());
                trend.add(buildTrendEntry(label, group));
                weekStart = weekStart.plusWeeks(1);
            }

        } else {
            // Daily (THIS_WEEK / THIS_MONTH)
            Map<LocalDate, List<ConsultationOfflineRequest>> grouped = requests.stream()
                    .collect(Collectors.groupingBy(ConsultationOfflineRequest::getAppointmentDate));

            LocalDate cursor = dateFrom;
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");
            while (!cursor.isAfter(dateTo)) {
                trend.add(buildTrendEntry(cursor.format(fmt),
                        grouped.getOrDefault(cursor, Collections.emptyList())));
                cursor = cursor.plusDays(1);
            }
        }

        return trend;
    }

    private Map<String, Object> buildTrendEntry(String label, List<ConsultationOfflineRequest> group) {
        long completed = group.stream().filter(r -> r.getStatus() == Status.CONSULTATION_COMPLETED).count();
        long cancelled = group.stream().filter(r -> r.getStatus() == Status.CONSULTATION_CANCELLED).count();
        long noShow = group.stream().filter(r -> r.getStatus() == Status.CONSULTATION_NO_SHOW).count();
        long pending = group.stream().filter(r -> r.getStatus() == Status.CONSULTATION_PENDING).count();
        long confirmed = group.stream().filter(r -> r.getStatus() == Status.CONSULTATION_CONFIRMED).count();
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("label", label);
        entry.put("total", (long) group.size());
        entry.put("completed", completed);
        entry.put("confirmed", confirmed);
        entry.put("pending", pending);
        entry.put("cancelled", cancelled);
        entry.put("noShow", noShow);
        return entry;
    }

    private List<Map<String, Object>> buildConsultationByDayOfWeek(List<ConsultationOfflineRequest> requests) {
        Map<java.time.DayOfWeek, Long> countMap = requests.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getAppointmentDate().getDayOfWeek(),
                        Collectors.counting()));

        List<Map<String, Object>> result = new ArrayList<>();
        for (java.time.DayOfWeek dow : java.time.DayOfWeek.values()) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("day", dow.name().substring(0, 3));
            entry.put("label", translateDayOfWeek(dow.name().substring(0, 3)));
            entry.put("total", countMap.getOrDefault(dow, 0L));
            result.add(entry);
        }
        return result;
    }

    private double pct(long numerator, long denominator) {
        if (denominator == 0) return 0.0;
        return BigDecimal.valueOf(numerator)
                .divide(BigDecimal.valueOf(denominator), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private ResponseEntity<Resource> buildFileResponse(Path path, String fileName) throws IOException {
        Resource resource = new UrlResource(path.toUri());
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                .replace("+", "%20");
        String contentDisposition = "attachment; filename=\""
                + fileName.replaceAll("[^\\x20-\\x7E]", "_")
                + "\"; filename*=UTF-8''" + encodedFileName;
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(resource);
    }

    @Override
    @Transactional
    public ResponseEntity<ResponseObject> processApplicant(ProcessApplicantRequest request) {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Tài khoản của bạn đang bị hạn chế, không thể thực hiện thao tác này.", null);
        }

        Campus actorCampus = extractActorCampus();
        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản cơ sở trường học hoặc bạn không có quyền truy cập.", null);
        }

        AdmissionReservationForm form = admissionReservationFormRepo.findById(request.getFormId()).orElse(null);

        if (form == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Không tìm thấy đơn đăng ký tuyển sinh", null);
        }

        // Ownership check qua admissionCampaign (phase 1: offering chưa được chọn → không dùng offering)
        AdmissionCampaign campaign = form.getAdmissionCampaign();
        if (campaign == null || !actorCampus.getSchool().getId().equals(campaign.getSchool().getId())) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Đơn này không thuộc trường của bạn.", null);
        }

        if (form.getStatus() != Status.RESERVATION_PENDING) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Đơn đăng ký này đã được xử lý hoặc không hợp lệ.", null);
        }

        String action = request.getAction() != null ? request.getAction().toUpperCase() : "";
        String successMessage;

        switch (action) {
            case "APPROVE":
                if (request.getCheckedDocuments() == null || request.getCheckedDocuments().isEmpty()) {
                    return ResponseBuilder.build(HttpStatus.BAD_REQUEST,
                            "Vui lòng xác nhận đã kiểm tra hồ sơ trước khi phê duyệt.", null);
                }

                ResponseEntity<ResponseObject> docValidation = validateCheckedDocuments(
                        campaign.getSchool().getId(), null, request.getCheckedDocuments());
                if (docValidation != null) return docValidation;


                form.setStatus(Status.RESERVATION_APPROVAL);
                successMessage = "Phê duyệt đơn đăng ký thành công.";
                break;

            case "REJECT":
                if (request.getRejectReason() == null || request.getRejectReason().isBlank()) {
                    return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Vui lòng cung cấp lý do từ chối.", null);
                }
                form.setStatus(Status.RESERVATION_REJECTED);
                form.setRejectReason(request.getRejectReason());

                // Hoàn lại quota campaign cho PH khác nộp
                if (campaign.getRemainingQuota() != null) {
                    campaign.setRemainingQuota(campaign.getRemainingQuota() + 1);
                    admissionCampaignRepo.save(campaign);
                }
                successMessage = "Từ chối đơn đăng ký thành công.";
                break;

            default:
                return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Hành động không hợp lệ. Chỉ chấp nhận APPROVE hoặc REJECT.", null);
        }

        Account actor = AuthRequestUtil.extractAuthenticatedAccount();

        if (actor != null) {
            form.setVerifiedBy(actor.getEmail());
        }

        form.setUpdatedTime(LocalDateTime.now());
        admissionReservationFormRepo.save(form);

        return ResponseBuilder.build(HttpStatus.OK, successMessage, null);
    }


    private ResponseEntity<ResponseObject> validateCheckedDocuments(
            int schoolId, String methodName, List<String> checkedDocuments) {

        SchoolConfig docConfig = schoolConfigRepo
                .findBySchoolIdAndKey(schoolId, "documentRequirementsData").orElse(null);
        if (docConfig == null || !(docConfig.getValue() instanceof Map<?, ?> docMap)) {
            // Trường chưa cấu hình document → bỏ qua validate
            return null;
        }

        List<String> requiredCodes = new ArrayList<>();

        List<?> mandatoryAll = docMap.get("mandatoryAll") instanceof List<?> m ? m : List.of();
        for (Object item : mandatoryAll) {
            if (item instanceof Map<?, ?> doc && Boolean.TRUE.equals(doc.get("required"))) {
                Object code = doc.get("code");
                if (code != null) requiredCodes.add(code.toString());
            }
        }

        if (methodName != null) {
            List<?> byMethod = docMap.get("byMethod") instanceof List<?> b ? b : List.of();
            for (Object item : byMethod) {
                if (!(item instanceof Map<?, ?> methodEntry)) continue;
                if (!methodName.equalsIgnoreCase(Objects.toString(methodEntry.get("methodCode"), null))) continue;
                List<?> docs = methodEntry.get("documents") instanceof List<?> d ? d : List.of();
                for (Object docItem : docs) {
                    if (docItem instanceof Map<?, ?> doc && Boolean.TRUE.equals(doc.get("required"))) {
                        Object code = doc.get("code");
                        if (code != null) requiredCodes.add(code.toString());
                    }
                }
                break;
            }
        }

        if (requiredCodes.isEmpty()) return null;

        List<String> checked = checkedDocuments != null
                ? checkedDocuments.stream().map(String::toUpperCase).toList()
                : List.of();

        List<String> missing = requiredCodes.stream()
                .filter(code -> !checked.contains(code.toUpperCase()))
                .toList();

        if (!missing.isEmpty()) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST,
                    "Chưa xác nhận đủ hồ sơ bắt buộc. Còn thiếu: " + String.join(", ", missing), null);
        }
        return null;
    }

    @Override
    @Transactional
    public ResponseEntity<ResponseObject> confirmPaymentDeposit(ConfirmPaymentDepositRequest request) {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Tài khoản của bạn đang bị hạn chế, không thể thực hiện thao tác này.", null);
        }

        Campus actorCampus = extractActorCampus();
        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản cơ sở trường học hoặc bạn không có quyền truy cập.", null);
        }

        AdmissionReservationForm form = admissionReservationFormRepo.findById(request.getFormId()).orElse(null);
        if (form == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Không tìm thấy đơn đăng ký tuyển sinh.", null);
        }

        AdmissionCampaign campaign = form.getAdmissionCampaign();
        if (campaign == null || !actorCampus.getSchool().getId().equals(campaign.getSchool().getId())) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Đơn này không thuộc trường của bạn.", null);
        }

        if (form.getStatus() != Status.RESERVATION_PAYMENT_PENDING) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST,
                    "Đơn chưa ở trạng thái chờ xác nhận thanh toán (PAYMENT_PENDING).", null);
        }

        // Fix 2: null check offering
        CampusProgramOffering offering = form.getCampusProgramOffering();
        if (offering == null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Đơn chưa được gắn gói tuyển sinh.", null);
        }

        String action = request.getAction() != null ? request.getAction().toUpperCase() : "";

        switch (action) {
            case "CONFIRM":

                form.setStatus(Status.RESERVATION_DEPOSITED);

                if (offering.getRemainingQuota() > 0) {
                    offering.setRemainingQuota(offering.getRemainingQuota() - 1);
                } else {
                    return ResponseBuilder.build(HttpStatus.BAD_REQUEST,
                            "Gói tuyển sinh không còn chỗ trống.", null);
                }

                campaign = form.getAdmissionCampaign();

                if (campaign != null && campaign.getRemainingQuota() != null) {
                    campaign.setRemainingQuota(campaign.getRemainingQuota() - 1);
                    // sau khi confirm deposited
                    // quota -1
                    admissionCampaignRepo.save(campaign);
                }

                int activeOfferingCount = admissionReservationFormRepo
                        .countByCampusProgramOfferingIdAndStatusIn(offering.getId(), Status.activeOfferingStatuses());
                Status newAppStatus = deriveApplicationStatusByWindowAndQuota(
                        offering.getOpenDate(), offering.getCloseDate(),
                        offering.getQuota(), offering.getRemainingQuota(), activeOfferingCount);
                offering.setApplicationStatus(newAppStatus);
                campusProgramOfferingRepo.save(offering);

                Account confirmActor = AuthRequestUtil.extractAuthenticatedAccount();
                form.setPaymentConfirmedBy(confirmActor != null ? confirmActor.getEmail() : null);
                break;

            case "REJECT_PAYMENT":

                if (request.getRejectReason() == null || request.getRejectReason().isBlank()) {
                    return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Vui lòng cung cấp lý do từ chối thanh toán.", null);
                }
                form.setStatus(Status.RESERVATION_PAYMENT_REJECTED);
                form.setRejectReason(request.getRejectReason());
                form.setPaymentProofUrl(null);
                break;

            default:
                return ResponseBuilder.build(HttpStatus.BAD_REQUEST,
                        "Hành động không hợp lệ. Chỉ chấp nhận CONFIRM hoặc REJECT_PAYMENT.", null);
        }

        form.setUpdatedTime(LocalDateTime.now());
        admissionReservationFormRepo.save(form);

        return ResponseBuilder.build(HttpStatus.OK,
                action.equals("CONFIRM") ? "Xác nhận đặt cọc thành công." : "Từ chối thanh toán, phụ huynh có thể upload lại.", null);
    }

    @Override
    public ResponseEntity<ResponseObject> getAdmissionReservationForms(String status) {

        Campus actorCampus = extractActorCampus();
        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản cơ sở trường học hoặc bạn không có quyền truy cập.", null);
        }

        List<AdmissionReservationForm> forms;

        int schoolId = actorCampus.getSchool().getId();

        if (status != null && !status.isBlank()) {
            Status filterStatus;
            try {
                filterStatus = Status.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseBuilder.build(HttpStatus.BAD_REQUEST,
                        "Trạng thái không hợp lệ: " + status, null);
            }
            forms = admissionReservationFormRepo.findByAdmissionCampaign_School_IdAndStatus(schoolId, filterStatus);
        } else {
            forms = admissionReservationFormRepo.findByAdmissionCampaign_School_Id(schoolId);
        }

        forms = forms.stream()
                .filter(f -> !"RESERVATION_TEMPLATE".equals(f.getType()))
                .toList();

        List<Map<String, Object>> result = buildAdmissionReservationForms(forms);

        return ResponseBuilder.build(HttpStatus.OK, "Lấy danh sách đơn thành công.", result);
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
        map.put("createdTime", form.getCreatedTime());
        map.put("updatedTime", form.getUpdatedTime());
        map.put("cancelReason", form.getCancelReason());
        map.put("rejectReason", form.getRejectReason());

        map.put("paymentProofUrl", form.getPaymentProofUrl());

        CampusProgramOffering offering = form.getCampusProgramOffering();

        if (offering == null) {
            map.put("campusProgramOfferingId", "N/A");
            map.put("programName", "N/A");
        } else {
            map.put("campusProgramOfferingId", offering.getId());
            map.put("programName", offering.getProgram().getName());
        }

        AdmissionCampaign admissionCampaign = form.getAdmissionCampaign();
        if (admissionCampaign == null) {
            map.put("schoolName", "N/A");
        } else {
            map.put("schoolName", admissionCampaign.getSchool().getName());
        }

        StudentProfile student = form.getStudentProfile();
        map.put("studentProfileId", student.getId());
        map.put("studentName", student.getStudentName());
        map.put("studentCode", student.getStudentCode());
        map.put("gender", student.getGender());
        map.put("transcriptImages", form.getTranscriptImages());

        AdmissionCampaign campaign = form.getAdmissionCampaign();
        String methodCode = offering != null ? offering.getAdmissionMethod() : null;

        Map<String, String> submittedMap = new HashMap<>();
        if (form.getProfileMetadata() instanceof List<?> metaList) {
            for (Object item : metaList) {
                if (!(item instanceof Map<?, ?> meta)) continue;
                Object key = meta.get("key");
                Object url = meta.get("imageUrl");
                if (key != null) submittedMap.put(key.toString(), url != null ? url.toString() : null);
            }
        }

        List<Map<String, Object>> submittedDocuments = new ArrayList<>();
        if (campaign != null) {
            int schoolId = campaign.getSchool().getId();
            Map<String, Object> docMap = resolveSchoolDocumentRequirements(schoolId);
            if (docMap != null) {

                List<?> mandatoryAll = docMap.get("mandatoryAll") instanceof List<?> m ? m : List.of();
                for (Object item : mandatoryAll) {
                    if (!(item instanceof Map<?, ?> doc)) continue;
                    String code = doc.get("code") != null ? doc.get("code").toString() : null;
                    Map<String, Object> entry = new HashMap<>();
                    entry.put("key", code);
                    entry.put("name", doc.get("name"));
                    entry.put("required", doc.get("required"));
                    entry.put("imageUrl", submittedMap.getOrDefault(code, null));
                    entry.put("submitted", submittedMap.containsKey(code));
                    submittedDocuments.add(entry);
                }

                List<?> byMethod = docMap.get("byMethod") instanceof List<?> b ? b : List.of();
                for (Object item : byMethod) {
                    if (!(item instanceof Map<?, ?> methodEntry)) continue;
                    if (methodCode == null || !Objects.equals(methodEntry.get("methodCode"), methodCode)) continue;
                    List<?> docs = methodEntry.get("documents") instanceof List<?> d ? d : List.of();
                    for (Object docItem : docs) {
                        if (!(docItem instanceof Map<?, ?> doc)) continue;
                        String code = doc.get("code") != null ? doc.get("code").toString() : null;
                        Map<String, Object> entry = new HashMap<>();
                        entry.put("key", code);
                        entry.put("name", doc.get("name"));
                        entry.put("required", doc.get("required"));
                        entry.put("imageUrl", submittedMap.getOrDefault(code, null));
                        entry.put("submitted", submittedMap.containsKey(code));
                        submittedDocuments.add(entry);
                    }
                    break;
                }
            }
        }
        map.put("submittedDocuments", submittedDocuments);
        map.put("admissionMethod", methodCode);

        Parent parent = student.getParent();
        map.put("parentProfileId", parent.getId());
        map.put("parentName", parent.getName());
        map.put("parentPhone", parent.getPhone());
        map.put("parentEmail", parent.getAccount().getEmail());
        map.put("identityCard", parent.getIdCardNumber());
        map.put("address", parent.getCurrentAddress());

        return map;
    }

    private Map<String, Object> resolveSchoolDocumentRequirements(int schoolId) {
        Optional<SchoolConfig> docConfigOpt = schoolConfigRepo.findBySchoolIdAndKey(schoolId, "documentRequirementsData");
        if (docConfigOpt.isEmpty() || !(docConfigOpt.get().getValue() instanceof Map<?, ?> rawMap)) return null;

        Map<String, Object> docMap = new HashMap<>((Map<String, Object>) rawMap);

        List<?> mandatoryAll = List.of();
        Object systemDocReq = platformConfigRepo.findByKey("admissionSettingsData")
                .map(c -> ((Map<String, Object>) c.getValue()).get("documentRequirementsData"))
                .orElse(null);
        if (systemDocReq instanceof Map<?, ?> systemMap && systemMap.get("mandatoryAll") instanceof List<?> m) {
            mandatoryAll = m;
        }
        docMap.put("mandatoryAll", mandatoryAll);
        return docMap;
    }
}
