package com.sp26se041.edubridgehcm.services.implementors;

import com.sp26se041.edubridgehcm.enums.ResourceType;
import com.sp26se041.edubridgehcm.enums.Role;
import com.sp26se041.edubridgehcm.enums.SessionType;
import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.models.Account;
import com.sp26se041.edubridgehcm.models.AdmissionCampaign;
import com.sp26se041.edubridgehcm.models.Campus;
import com.sp26se041.edubridgehcm.models.CampusProgramOffering;
import com.sp26se041.edubridgehcm.models.CampusResourceQuota;
import com.sp26se041.edubridgehcm.models.CampusScheduleTemplate;
import com.sp26se041.edubridgehcm.models.ChatMessage;
import com.sp26se041.edubridgehcm.models.Conversation;
import com.sp26se041.edubridgehcm.models.Counsellor;
import com.sp26se041.edubridgehcm.models.CounsellorSlot;
import com.sp26se041.edubridgehcm.models.Program;
import com.sp26se041.edubridgehcm.models.School;
import com.sp26se041.edubridgehcm.models.SchoolConfig;
import com.sp26se041.edubridgehcm.repositories.AccountRepo;
import com.sp26se041.edubridgehcm.repositories.AdmissionCampaignRepo;
import com.sp26se041.edubridgehcm.repositories.AdmissionReservationFormRepo;
import com.sp26se041.edubridgehcm.repositories.CampusProgramOfferingRepo;
import com.sp26se041.edubridgehcm.repositories.CampusRepo;
import com.sp26se041.edubridgehcm.repositories.CampusResourceQuotaRepo;
import com.sp26se041.edubridgehcm.repositories.CampusScheduleTemplateRepo;
import com.sp26se041.edubridgehcm.repositories.ChatMessageRepo;
import com.sp26se041.edubridgehcm.repositories.ConversationRepo;
import com.sp26se041.edubridgehcm.repositories.CounsellorRepo;
import com.sp26se041.edubridgehcm.repositories.CounsellorSlotRepo;
import com.sp26se041.edubridgehcm.repositories.ProgramRepo;
import com.sp26se041.edubridgehcm.repositories.SchoolConfigRepo;
import com.sp26se041.edubridgehcm.requests.AssignCounsellorIntoSlotsRequest;
import com.sp26se041.edubridgehcm.requests.CampusScheduleTemplateRequest;
import com.sp26se041.edubridgehcm.requests.CreateAccountCounsellorRequest;
import com.sp26se041.edubridgehcm.requests.CreateCampusProgramOfferingRequest;
import com.sp26se041.edubridgehcm.requests.UpdateCampusConfigRequest;
import com.sp26se041.edubridgehcm.requests.UpdateCampusProgramOfferingRequest;
import com.sp26se041.edubridgehcm.responses.PageResponse;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.responses.StorageTreeNode;
import com.sp26se041.edubridgehcm.services.CampusService;
import com.sp26se041.edubridgehcm.services.SupabaseStorageService;
import com.sp26se041.edubridgehcm.utils.AccountRestrictionUtil;
import com.sp26se041.edubridgehcm.utils.AuthRequestUtil;
import com.sp26se041.edubridgehcm.utils.ExcelUtil;
import com.sp26se041.edubridgehcm.utils.PaginationUtil;
import com.sp26se041.edubridgehcm.utils.ResourceCheckerUtil;
import com.sp26se041.edubridgehcm.utils.ResponseBuilder;
import com.sp26se041.edubridgehcm.utils.SchoolConfigUtil;
import com.sp26se041.edubridgehcm.validations.campus.CampusProgramOfferingValidation;
import com.sp26se041.edubridgehcm.validations.campus.CampusScheduleTemplateValidation;
import com.sp26se041.edubridgehcm.validations.campus.CounsellorSlotValidation;
import com.sp26se041.edubridgehcm.validations.campus.CounsellorValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class CampusServiceImpl implements CampusService {

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

    private final CampusResourceQuotaRepo campusResourceQuotaRepo;

    private final SupabaseStorageService supabaseStorageService;

    private final ConversationRepo conversationRepo;

    private final ChatMessageRepo chatMessageRepo;

    @Override
    @Transactional
    public ResponseEntity<ResponseObject> createCampusProgramOffering(CreateCampusProgramOfferingRequest request) {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Your account is restricted", null);
        }

        Campus actorCampus = extractActorCampus();
        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "No school campus account found", null);
        }

        if (request.getCampusId() != null && !request.getCampusId().equals(actorCampus.getId())) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "You do not have permission to create a program offering for another campus.", null);
        }

        String error = CampusProgramOfferingValidation.validateCreateCampusProgramOffering(request, actorCampus, admissionCampaignRepo, programRepo, campusProgramOfferingRepo, campusRepo);

        if (error != null) {

            if (error.contains("already has the same program offering")) {
                return ResponseBuilder.build(HttpStatus.CONFLICT, error, null);
            }

            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, error, null);
        }

        // Lấy lại các entity đã validate để tạo offering
        AdmissionCampaign campaign = admissionCampaignRepo.findById(request.getAdmissionCampaignId()).orElse(null);

        if (campaign == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Campaign not found", null);
        }

        Program program = programRepo.findByIdAndCurriculum_School_Id(request.getProgramId(), actorCampus.getSchool().getId());

        if (program == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Program not found", null);
        }

        BigDecimal basePrice = program.getBaseTuitionFee();

        float adjustmentPercent = (request.getPriceAdjustmentPercentage() != null) ? request.getPriceAdjustmentPercentage() : 0.0f;

        // 3. Calculate Final Tuition
        // Formula: Final = Base * (1 + % / 100)
        BigDecimal multiplier = BigDecimal.valueOf(1 + (adjustmentPercent / 100));
        BigDecimal finalTuition = basePrice.multiply(multiplier).setScale(0, RoundingMode.HALF_UP); // Rounding for VND/Currency

        campusProgramOfferingRepo.save(CampusProgramOffering.builder().campus(actorCampus).admissionCampaign(campaign).program(program).quota(request.getQuota()).remainingQuota(request.getQuota()).learningMode(request.getLearningMode()).priceAdjustmentPercentage(adjustmentPercent).finalTuitionFee(finalTuition).applicationStatus(Status.OPEN).openDate((request.getOpenDate() != null) ? request.getOpenDate() : campaign.getStartDate()).closeDate((request.getCloseDate() != null) ? request.getCloseDate() : campaign.getEndDate()).status(Status.OPEN_ADMISSION_CAMPAIGN).build());

        return ResponseBuilder.build(HttpStatus.OK, "Create campus offering successfully", null);
    }

    @Override
    public ResponseEntity<ResponseObject> viewCampusProgramOfferingList(Integer campusId, int page, int pageSize) {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Your account is restricted", null);
        }

        Campus actorCampus = extractActorCampus();
        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "No school campus account found", null);
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
                    return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Target campus is out of your school scope", null);
                }
                offeringPage = campusProgramOfferingRepo.findByCampusIdOrderByIdDesc(campusId, pageable);
            }
        } else {
            if (campusId != null && !campusId.equals(actorCampus.getId())) {
                return ResponseBuilder.build(HttpStatus.FORBIDDEN, "You are only authorized to view your own campus data.", null);
            }
            offeringPage = campusProgramOfferingRepo.findByCampusIdOrderByIdDesc(actorCampus.getId(), pageable);
        }

        PageResponse<Map<String, Object>> pageResponse = PaginationUtil.buildPageResponse(offeringPage, this::buildOfferingData);

        return ResponseBuilder.build(HttpStatus.OK, "View campus offering list successfully", pageResponse);
    }

    @Override
    @Transactional
    public ResponseEntity<ResponseObject> updateCampusProgramOffering(UpdateCampusProgramOfferingRequest request) {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Your account is restricted", null);
        }

        Campus actorCampus = extractActorCampus();
        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "No school campus account found", null);
        }

        if (request.getCampusId() != null && !request.getCampusId().equals(actorCampus.getId())) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "You do not have permission to update a program offering for another campus.", null);
        }

        CampusProgramOffering offering = campusProgramOfferingRepo.findById(request.getId()).orElse(null);

        if (offering == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Offering not found", null);
        }

        int usedQuota = Math.max(0, offering.getQuota() - offering.getRemainingQuota());

        AdmissionCampaign targetCampaign = offering.getAdmissionCampaign();

        if (request.getAdmissionCampaignId() != null && !request.getAdmissionCampaignId().equals(targetCampaign.getId())) {
            targetCampaign = admissionCampaignRepo.findById(request.getAdmissionCampaignId()).orElse(null);
        }

        Campus targetCampus = offering.getCampus();

        Program targetProgram = offering.getProgram();

        if (request.getProgramId() != null && !request.getProgramId().equals(targetProgram.getId())) {
            targetProgram = programRepo.findByIdAndCurriculum_School_Id(request.getProgramId(), actorCampus.getSchool().getId());
        }

        String error = CampusProgramOfferingValidation.validateUpdateCampusProgramOffering(request, actorCampus, offering, targetCampaign, targetCampus, targetProgram, usedQuota, offering.getApplicationStatus(), request.getQuota() != null ? request.getQuota() : offering.getQuota(), request.getOpenDate() != null ? request.getOpenDate() : offering.getOpenDate(), request.getCloseDate() != null ? request.getCloseDate() : offering.getCloseDate(), request.getLearningMode() != null ? request.getLearningMode() : offering.getLearningMode(), campusProgramOfferingRepo);

        if (error != null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, error, null);
        }

        // Only allow update if status is PAUSED
        if (offering.getApplicationStatus() != Status.PAUSED) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Updates are only allowed when the program is paused.", null);
        }

        int targetRemainingQuota = request.getQuota() != null ? request.getQuota() : offering.getQuota() - usedQuota;

        assert targetCampaign != null;
        offering.setAdmissionCampaign(targetCampaign);

        offering.setCampus(targetCampus);
        offering.setProgram(targetProgram);
        offering.setLearningMode(request.getLearningMode() != null ? request.getLearningMode() : offering.getLearningMode());
        offering.setQuota(request.getQuota() != null ? request.getQuota() : offering.getQuota());
        offering.setRemainingQuota(targetRemainingQuota);
        offering.setFinalTuitionFee(request.getTuitionFee() != null ? request.getTuitionFee() : offering.getFinalTuitionFee());
        offering.setOpenDate(request.getOpenDate() != null ? request.getOpenDate() : offering.getOpenDate());
        offering.setCloseDate(request.getCloseDate() != null ? request.getCloseDate() : offering.getCloseDate());

        campusProgramOfferingRepo.save(offering);

        return ResponseBuilder.build(HttpStatus.OK, "Update campus offering successfully", null);
    }

    @Override
    @Transactional
    public ResponseEntity<ResponseObject> closeCampusProgramOffering(Integer offeringId) {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Your account is restricted", null);
        }

        Campus actorCampus = extractActorCampus();
        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "No school campus account found", null);
        }

        CampusProgramOffering offering = campusProgramOfferingRepo.findById(offeringId).orElse(null);

        if (offering == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Offering not found", null);
        }

        if (!offering.getCampus().getId().equals(actorCampus.getId())) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "You do not have the right to close programs at other institutions.", null);
        }

        if (offering.getStatus() == Status.CLOSED) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "This program was previously closed.", null);
        }

        int formCount = admissionReservationFormRepo.countByCampusProgramOfferingId(offeringId);

        if (formCount >= offering.getQuota()) {
            // Nếu đóng khi đã đủ hoặc vượt chỉ tiêu -> Hiển thị là FULL (Hết chỗ)
            offering.setApplicationStatus(Status.FULL);
        } else {
            // Nếu đóng khi chưa đủ chỉ tiêu (do Admin chủ động hoặc hết hạn) -> Hiển thị là CLOSED (Đóng)
            offering.setApplicationStatus(Status.CLOSED);
        }

        offering.setStatus(Status.CLOSED);
        offering.setRemainingQuota(0);
        campusProgramOfferingRepo.save(offering);

        return ResponseBuilder.build(HttpStatus.OK, (formCount >= offering.getQuota()) ? "Chương trình đã đạt chỉ tiêu và được đóng tự động." : "Chương trình đã được Admin chủ động đóng thành công.", null);
    }

    @Override
    @Transactional
    public ResponseEntity<ResponseObject> changeCampusProgramOfferingStatus(int offeringId, Status targetStatus) {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Your account is restricted", null);
        }

        Campus actorCampus = extractActorCampus();
        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "No school campus account found", null);
        }

        CampusProgramOffering offering = campusProgramOfferingRepo.findById(offeringId).orElse(null);

        if (offering == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Offering not found", null);
        }

        if (!offering.getCampus().getId().equals(actorCampus.getId())) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Bạn không có quyền cập nhật trạng thái chương trình của cơ sở khác", null);
        }

        if (offering.getStatus().equals(Status.CLOSED) || offering.getApplicationStatus().equals(Status.FULL)) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "The program has closed or reached its quota; it cannot change its status.", null);
        }

        if (targetStatus.equals(Status.PAUSED)) {

            if (offering.getApplicationStatus().equals(Status.PAUSED)) {
                return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "The program is in PAUSED", null);
            }

            offering.setApplicationStatus(Status.PAUSED);

        } else if (targetStatus == Status.OPEN) {

            if (offering.getApplicationStatus() != Status.PAUSED) {
                return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Chỉ có thể mở lại chương trình từ trạng thái PAUSED", null);
            }

            int formCount = admissionReservationFormRepo.countByCampusProgramOfferingId(offeringId);

            if (formCount >= offering.getQuota()) {
                return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Không thể mở lại vì đã đủ chỉ tiêu", null);
            }

            offering.setApplicationStatus(Status.OPEN);

        } else {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Chỉ hỗ trợ chuyển trạng thái OPEN/PAUSED", null);
        }

        campusProgramOfferingRepo.save(offering);

        return ResponseBuilder.build(HttpStatus.OK, "Paused is successful.", null);
    }

    private Map<String, Object> buildOfferingData(CampusProgramOffering offering) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", offering.getId());
        data.put("campusId", offering.getCampus().getId());
        data.put("campusName", offering.getCampus().getName());
        data.put("city", offering.getCampus().getCity());
        data.put("district", offering.getCampus().getDistrict());
        data.put("boardingType", offering.getCampus().getBoardingType());
        data.put("latitude", offering.getCampus().getLatitude());
        data.put("longitude", offering.getCampus().getLongitude());
        data.put("campaignId", offering.getAdmissionCampaign().getId());
        data.put("campaignName", offering.getAdmissionCampaign().getName());
        data.put("campaignYear", offering.getAdmissionCampaign().getYear());
        data.put("programId", offering.getProgram().getId());
        data.put("programName", offering.getProgram().getName());
        data.put("curriculumId", offering.getProgram().getCurriculum().getId());
        data.put("curriculumType", offering.getProgram().getCurriculum().getCurriculumType());
        data.put("applicationYear", offering.getProgram().getCurriculum().getApplicationYear());
        data.put("quota", offering.getQuota());
        data.put("remainingQuota", offering.getRemainingQuota());
        data.put("learningMode", offering.getLearningMode());
        data.put("tuitionFee", offering.getFinalTuitionFee());
        data.put("baseTuitionFee", offering.getProgram().getBaseTuitionFee());
        data.put("priceAdjustmentPercentage", offering.getPriceAdjustmentPercentage());
        data.put("applicationStatus", offering.getApplicationStatus());
        data.put("openDate", offering.getOpenDate());
        data.put("closeDate", offering.getCloseDate());
        data.put("status", offering.getStatus());
        data.put("schoolId", offering.getAdmissionCampaign().getSchool().getId());
        return data;
    }

    @Override
    @Transactional
    public ResponseEntity<ResponseObject> createAccountCounsellor(CreateAccountCounsellorRequest request) {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Your account is restricted", null);
        }

        Campus actorCampus = extractActorCampus();
        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "No school campus account found", null);
        }

        String validationError = CounsellorValidation.validateCreateCounsellor(request, accountRepo, campusResourceQuotaRepo, counsellorRepo, actorCampus.getId());

        if (validationError != null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, validationError, null);
        }

        Account account = accountRepo.save(Account.builder().email(normalize(request.getEmail())).role(Role.COUNSELLOR).status(Status.ACCOUNT_ACTIVE).registerDate(LocalDate.now()).firstLogin(true).build());

        Counsellor counsellor = counsellorRepo.save(Counsellor.builder().account(account).campus(actorCampus).avatar(request.getAvatar()).employeeCode(UUID.randomUUID()).build());

        return ResponseBuilder.build(HttpStatus.OK, "Create counsellor successfully", buildCounsellorData(counsellor));
    }

    @Override
    public ResponseEntity<ResponseObject> viewAccountCounsellorList(int page, int size) {

        Campus actorCampus = extractActorCampus();

        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "No school campus account found", null);
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

        return ResponseBuilder.build(HttpStatus.OK, "View counsellor list successfully", data);
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
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Your account is restricted", null);
        }

        Campus actorCampus = extractActorCampus();

        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "No school campus account found", null);
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
        facilityJson.put("overview", request.getOverview());
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

            policyJsonb.put("slotDurationInMinutes", mergedOp.get("slotDurationInMinutes"));

            policyJsonb.put("maxBookingPerSlot", mergedOp.get("maxBookingPerSlot"));

            policyJsonb.put("allowBookingBeforeHours", mergedOp.get("allowBookingBeforeHours"));

            policyJsonb.put("fullTextRendered", finalPolicyStr);

            policyJsonb.put("rawCustomNote", request.getPolicyDetail());

            actorCampus.setPolicyDetail(policyJsonb);
        }

        campusRepo.save(actorCampus);

        return ResponseBuilder.build(HttpStatus.OK, "Campus config updated successfully", null);
    }

    @Override
    public ResponseEntity<ResponseObject> getCampusConfig() {

        Campus actorCampus = extractActorCampus();

        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "No school campus account found", null);
        }


        SchoolConfig hqFacility = schoolConfigRepo.findBySchoolIdAndKey(actorCampus.getSchool().getId(), "facilityData").orElse(null);

        SchoolConfig hqOperation = schoolConfigRepo.findBySchoolIdAndKey(actorCampus.getSchool().getId(), "operationSettingsData").orElse(null);

        Map<String, Object> result = new HashMap<>();

        Map<String, Object> hqSection = new HashMap<>();

        hqSection.put("facility", hqFacility != null ? hqFacility.getValue() : null);
        hqSection.put("operation", hqOperation != null ? hqOperation.getValue() : null);
        result.put("hqDefault", hqSection);

        Map<String, Object> campusSection = new HashMap<>();
        campusSection.put("facilityJson", actorCampus.getFacility());
        campusSection.put("policyDetailRendered", actorCampus.getPolicyDetail());

        result.put("campusCurrent", campusSection);

        return ResponseBuilder.build(HttpStatus.OK, "", result);
    }

    @Override
    @Transactional
    public ResponseEntity<ResponseObject> upsertCampusScheduleTemplate(CampusScheduleTemplateRequest request) {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Your account is restricted", null);
        }

        Campus actorCampus = extractActorCampus();

        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "No school campus account found", null);
        }

        if (request.getTemplateId() != null && request.getTemplateId() > 0) {

            CampusScheduleTemplate existing = campusScheduleTemplateRepo.findById(request.getTemplateId()).orElse(null);

            if (existing == null) {
                return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Template does not exist.", null);
            }

            if (!existing.getCampus().getId().equals(actorCampus.getId())) {
                return ResponseBuilder.build(HttpStatus.FORBIDDEN, "You do not edit another campus template", null);
            }
        }

        Map<String, Object> workingConfig = SchoolConfigUtil.getWorkingConfig(schoolConfigRepo.findBySchoolIdAndKey(actorCampus.getSchool().getId(), "operationSettingsData").orElse(null));

        for (String day : request.getDayOfWeek()) {

            String error = CampusScheduleTemplateValidation.validateCampusScheduleTemplate(request.getTemplateId(), request, day, workingConfig, campusScheduleTemplateRepo, actorCampus);

            if (error != null) {
                return ResponseBuilder.build(HttpStatus.BAD_REQUEST, error, null);
            }

            saveSingleTemplate(request, day, actorCampus);
        }

        return ResponseBuilder.build(HttpStatus.OK, "Templates processed successfully", null);
    }

    private void saveSingleTemplate(CampusScheduleTemplateRequest request, String day, Campus campus) {
        CampusScheduleTemplate template;

        boolean isUpdate = request.getTemplateId() != null && request.getTemplateId() > 0 && request.getDayOfWeek().size() == 1;

        if (isUpdate) {
            template = campusScheduleTemplateRepo.findById(request.getTemplateId()).get();
        } else {

            template = new CampusScheduleTemplate();
            template.setCampus(campus);
            template.setCreatedDate(LocalDate.now());
        }

        template.setDayOfWeek(day.toUpperCase());
        template.setStartTime(LocalTime.parse(request.getStartTime()));
        template.setEndTime(LocalTime.parse(request.getEndTime()));
        template.setSessionType(SessionType.valueOf(request.getSessionType()));
        template.setUpdatedDate(LocalDate.now());
        template.setActive(true);

        campusScheduleTemplateRepo.save(template);
    }

    @Override
    public ResponseEntity<ResponseObject> viewCampusScheduleTemplateByEachCampus() {

        Campus actorCampus = extractActorCampus();

        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "No school campus account found", null);
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

        return ResponseBuilder.build(HttpStatus.OK, "View campus schedule templates successfully", groupedTemplates);
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
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Your account is restricted", null);
        }

        Campus actorCampus = extractActorCampus();
        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "No school campus account found", null);
        }

        CampusScheduleTemplate template = campusScheduleTemplateRepo.findById(request.getTemplateId()).orElse(null);
        if (template == null) return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Template not found", null);

        List<CounsellorSlot> allCurrentSlots = counsellorSlotRepo.findByCampusScheduleTemplate_Campus_Id(actorCampus.getId());

        List<Counsellor> counsellors = counsellorRepo.findAllById(request.getCounsellorIds());

        String error = CounsellorSlotValidation.validateAssignRequest(request, actorCampus, template, counsellors, allCurrentSlots);

        if (error != null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, error, null);
        }

        // 4. Xác định Action
        String actionInput = (request.getAction() != null) ? request.getAction().toUpperCase() : "ASSIGN";

        for (Counsellor counsellor : counsellors) {

            // Lọc ra các slot của riêng counsellor này từ list tổng
            List<CounsellorSlot> counsellorSlots = allCurrentSlots.stream().filter(s -> s.getCounsellor().getId().equals(counsellor.getId())).toList();

            if ("ASSIGN".equals(actionInput)) {
                handleAssignAction(counsellor, template, request, counsellorSlots);
            } else {
                handleUnassignAction(template, request, counsellorSlots);
            }
        }

        return ResponseBuilder.build(HttpStatus.OK, actionInput + " counsellors successful", null);
    }

    private void handleAssignAction(Counsellor counsellor, CampusScheduleTemplate template, AssignCounsellorIntoSlotsRequest request, List<CounsellorSlot> existingSlots) {
        for (CounsellorSlot slot : existingSlots) {
            boolean isDateOverlap = request.getStartDate().isBefore(slot.getEndDate().plusDays(1)) && request.getEndDate().isAfter(slot.getStartDate().minusDays(1));

            boolean isDayOfWeekSame = slot.getCampusScheduleTemplate().getDayOfWeek().equalsIgnoreCase(template.getDayOfWeek());

            boolean isTimeOverlap = template.getStartTime().isBefore(slot.getCampusScheduleTemplate().getEndTime()) && template.getEndTime().isAfter(slot.getCampusScheduleTemplate().getStartTime());

            if (isDateOverlap && isDayOfWeekSame && isTimeOverlap) {
                // Nếu trùng chính xác tuyệt đối (trùng cả template id, start/end date) thì bỏ qua (Idempotent)
                if (slot.getCampusScheduleTemplate().getId().equals(template.getId()) && slot.getStartDate().equals(request.getStartDate()))
                    return;

                throw new IllegalArgumentException("Counsellor " + counsellor.getName() + " is busy during this period.");
            }
        }

        counsellorSlotRepo.save(CounsellorSlot.builder().campusScheduleTemplate(template).counsellor(counsellor).startDate(request.getStartDate()).endDate(request.getEndDate()).build());
    }

    private void handleUnassignAction(CampusScheduleTemplate template, AssignCounsellorIntoSlotsRequest request, List<CounsellorSlot> existingSlots) {

        CounsellorSlot targetSlot = existingSlots.stream().filter(s -> s.getCampusScheduleTemplate().getId().equals(template.getId()) && s.getStartDate().equals(request.getStartDate()) && s.getEndDate().equals(request.getEndDate())).findFirst().orElse(null);

        if (targetSlot != null) {
            // Logic kiểm tra Consultation (giữ nguyên của bạn)
            CounsellorSlotValidation.validateNoActiveConsultation(targetSlot);
            counsellorSlotRepo.delete(targetSlot);
        }
    }

    @Override
    public ResponseEntity<ResponseObject> getAvailableSlots(LocalDate targetDate) {

        Campus actorCampus = extractActorCampus();
        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "No school campus account found", null);
        }

        String dayOfWeek = targetDate.getDayOfWeek().name();

        List<CounsellorSlot> assignedSlots = counsellorSlotRepo.findByStartDateLessThanEqualAndEndDateGreaterThanEqualAndCampusScheduleTemplate_DayOfWeekAndCampusScheduleTemplate_Campus_IdAndCampusScheduleTemplate_ActiveTrue(targetDate, // khớp với StartDateLessThanEqual
                targetDate, dayOfWeek, actorCampus.getId());

        Map<String, List<Map<String, Object>>> groupedByTime = new LinkedHashMap<>();

        for (CounsellorSlot slot : assignedSlots) {
            if (isSlotAvailable(slot, targetDate)) {

                String startTimeKey = slot.getCampusScheduleTemplate().getStartTime().toString();

                groupedByTime.putIfAbsent(startTimeKey, new ArrayList<>());

                Map<String, Object> slotData = buildCounsellorSlotData(slot);
                groupedByTime.get(startTimeKey).add(slotData);
            }
        }

        return ResponseBuilder.build(HttpStatus.OK, "Get slots grouped by time", groupedByTime);
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

        return data;
    }

    private boolean isSlotAvailable(CounsellorSlot slot, LocalDate targetDate) {
        List<Status> activeStatuses = List.of(Status.CONSULTATION_PENDING, Status.CONSULTATION_CONFIRMED, Status.CONSULTATION_IN_PROGRESS);

        // Nếu KHÔNG có request nào trùng ngày targetDate và có status "đang bận" -> Trả về true (Available)
        return slot.getConsultationOfflineRequests().stream().noneMatch(req -> req.getAppointmentDate().equals(targetDate) && activeStatuses.contains(req.getStatus()));
    }

    @Override
    public ResponseEntity<ResponseObject> getAssignedSlots(Integer counsellorId) {

        Campus actorCampus = extractActorCampus();
        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "No school campus account found", null);
        }

        List<CounsellorSlot> slots = (counsellorId != null) ? counsellorSlotRepo.findByCampusScheduleTemplate_Campus_IdAndCounsellor_Id(actorCampus.getId(), counsellorId) : counsellorSlotRepo.findByCampusScheduleTemplate_Campus_Id(actorCampus.getId());

        List<Map<String, Object>> responseList = slots.stream().map(this::buildManagementSlotData).toList();

        return ResponseBuilder.build(HttpStatus.OK, "Get assigned slots successful", responseList);
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

        return data;
    }

    @Override
    public ResponseEntity<ResponseObject> getCounsellorAvailableList() {

        Campus actorCampus = extractActorCampus();
        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "No school campus account found", null);
        }

        List<Counsellor> counsellorList = counsellorRepo.findByCampus_IdAndAccount_Status(actorCampus.getId(), Status.ACCOUNT_ACTIVE);

        List<Map<String, Object>> responseList = counsellorList.stream().map(this::buildCounsellor).toList();

        return ResponseBuilder.build(HttpStatus.OK, "Get assigned slots successful", responseList);
    }

    private Map<String, Object> buildCounsellor(Counsellor counsellor) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", counsellor.getId());
        data.put("name", counsellor.getName());
        data.put("email", (counsellor.getAccount() != null) ? counsellor.getAccount().getEmail() : "No Account");
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
            row.createCell(3).setCellValue(acc != null ? acc.getEmail() : "N/A");
            row.createCell(4).setCellValue(acc != null ? acc.getStatus().toString() : "N/A");

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
            return ResponseBuilder.build(HttpStatus.OK, "View documents successfully", result);
        } catch (Exception ex) {
            return ResponseBuilder.build(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), null);
        }
    }

    @Override
    public ResponseEntity<ResponseObject> getChatHistoryWithAdmin(Long cursorId) {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Your account is restricted", null);
        }

        Campus campus = extractActorCampus();

        Account accAdmin = accountRepo.findByRole(Role.ADMIN).get(0);

        if (accAdmin == null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Account admin not found or be deleted", null);
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

            return ResponseBuilder.build(HttpStatus.OK, "Success", buildHistoryMessages(existingConversation.get(), accAdmin.getEmail(), campus.getAccount().getEmail(), messages, hasMore, nextCursorId));

        }

        Conversation conversation = Conversation.builder()
                .campusId(campus.getId())
                .accAdminId(accAdmin.getId())
//                 .status(Status.CONVERSATION_ACTIVE)
                .createdDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .build();

//        conversationRepo.save(conversation);

        return ResponseBuilder.build(HttpStatus.OK, "Success", buildHistoryMessages(conversation, accAdmin.getEmail(), campus.getAccount().getEmail(), messages, hasMore, nextCursorId));
    }

    @Override
    public ResponseEntity<ResponseObject> createConversationWithAdmin() {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Your account is restricted", null);
        }

        Campus campus = extractActorCampus();

        if (campus == null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Campus account not found or be deleted", null);
        }

        Account accAdmin = accountRepo.findByRole(Role.ADMIN).get(0);

        if (accAdmin == null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Account admin not found or be deleted", null);
        }

        Conversation conversation = Conversation.builder()
                .campusId(campus.getId())
                .accAdminId(accAdmin.getId())
                .status(Status.CONVERSATION_ACTIVE)
                .createdDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .build();

        conversationRepo.save(conversation);

        return ResponseBuilder.build(HttpStatus.OK, "Create conversation successfully", conversation.getId());
    }

    @Override
    public ResponseEntity<ResponseObject> getConversation() {

        Campus campus = extractActorCampus();

        if (campus == null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST,
                    "Campus account not found or be deleted", null);
        }

        List<Account> admins = accountRepo.findByRole(Role.ADMIN);
        if (admins == null || admins.isEmpty()) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST,
                    "Account admin not found or be deleted", null);
        }

        Account accAdmin = admins.get(0);

        Optional<Conversation> conversationOpt =
                conversationRepo.findByCampusIdAndAccAdminId(campus.getId(), accAdmin.getId());

        if (conversationOpt.isEmpty()) {
            Map<String, Object> data = new HashMap<>();
            data.put("conversationId", null);
            data.put("hasNewMessage", false);
            data.put("unreadCount", 0);
            return ResponseBuilder.build(HttpStatus.OK, "Get conversation success", data);
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

        return ResponseBuilder.build(HttpStatus.OK, "Get conversation success", data);
    }

    private Map<String, Object> buildHistoryMessages(Conversation conversation, String emailAdmin, String campusEmail, List<ChatMessage> messages, boolean hasMore, Long nextCursorId) {

        Map<String, Object> response = new HashMap<>();

        response.put("conversationId", conversation.getId());
        response.put("accAdminId", conversation.getAccAdminId());
        response.put("emailAdmin", emailAdmin);
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

    private ResponseEntity<Resource> buildFileResponse(Path path, String fileName) throws IOException {
        Resource resource = new UrlResource(path.toUri());
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"").contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")).body(resource);
    }
}
