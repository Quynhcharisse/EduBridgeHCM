package com.sp26se041.edubridgehcm.services.implementors;

import com.sp26se041.edubridgehcm.enums.Role;
import com.sp26se041.edubridgehcm.enums.SessionType;
import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.models.Account;
import com.sp26se041.edubridgehcm.models.AdmissionCampaign;
import com.sp26se041.edubridgehcm.models.Campus;
import com.sp26se041.edubridgehcm.models.CampusProgramOffering;
import com.sp26se041.edubridgehcm.models.CampusScheduleTemplate;
import com.sp26se041.edubridgehcm.models.Counsellor;
import com.sp26se041.edubridgehcm.models.Program;
import com.sp26se041.edubridgehcm.models.SchoolConfig;
import com.sp26se041.edubridgehcm.repositories.AccountRepo;
import com.sp26se041.edubridgehcm.repositories.AdmissionCampaignRepo;
import com.sp26se041.edubridgehcm.repositories.AdmissionReservationFormRepo;
import com.sp26se041.edubridgehcm.repositories.CampusProgramOfferingRepo;
import com.sp26se041.edubridgehcm.repositories.CampusRepo;
import com.sp26se041.edubridgehcm.repositories.CampusScheduleTemplateRepo;
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
import com.sp26se041.edubridgehcm.services.CampusService;
import com.sp26se041.edubridgehcm.utils.AccountRestrictionUtil;
import com.sp26se041.edubridgehcm.utils.AuthRequestUtil;
import com.sp26se041.edubridgehcm.utils.PaginationUtil;
import com.sp26se041.edubridgehcm.utils.ResponseBuilder;
import com.sp26se041.edubridgehcm.utils.SchoolConfigUtil;
import com.sp26se041.edubridgehcm.validations.campus.CampusProgramOfferingValidation;
import com.sp26se041.edubridgehcm.validations.campus.CampusScheduleTemplateValidation;
import com.sp26se041.edubridgehcm.validations.campus.CounsellorValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

        Campus targetCampus = resolveTargetCampus(actorCampus, request.getCampusId());

        if (targetCampus == null) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Campus is out of your scope", null);
        }

        campusProgramOfferingRepo.save(CampusProgramOffering.builder().campus(targetCampus).admissionCampaign(campaign).program(program).quota(request.getQuota()).remainingQuota(request.getQuota()).learningMode(request.getLearningMode()).priceAdjustmentPercentage(adjustmentPercent).finalTuitionFee(finalTuition).applicationStatus(Status.OPEN).openDate((request.getOpenDate() != null) ? request.getOpenDate() : campaign.getStartDate()).closeDate((request.getCloseDate() != null) ? request.getCloseDate() : campaign.getEndDate()).status(Status.OPEN_ADMISSION_CAMPAIGN).build());

        return ResponseBuilder.build(HttpStatus.OK, "Create campus offering successfully", null);
    }

    private Campus resolveTargetCampus(Campus actorCampus, Integer requestedCampusId) {
        if (!actorCampus.getIsPrimaryBranch()) {
            if (requestedCampusId != null && !requestedCampusId.equals(actorCampus.getId())) {
                return null;
            }
            return actorCampus;
        }

        Integer targetCampusId = requestedCampusId == null ? actorCampus.getId() : requestedCampusId;
        return campusRepo.findByIdAndSchoolId(targetCampusId, actorCampus.getSchool().getId()).orElse(null);
    }

    @Override
    public ResponseEntity<ResponseObject> viewCampusProgramOfferingList(int campusId, int page, int pageSize) {

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

        Integer requestedCampusId = campusId <= 0 ? null : campusId;

        Page<CampusProgramOffering> offeringPage;
        if (actorCampus.getIsPrimaryBranch()) {
            if (requestedCampusId == null) {
                offeringPage = campusProgramOfferingRepo.findByAdmissionCampaignSchoolIdOrderByIdDesc(actorCampus.getSchool().getId(), pageable);
            } else {
                Campus campus = campusRepo.findByIdAndSchoolId(requestedCampusId, actorCampus.getSchool().getId()).orElse(null);
                if (campus == null) {
                    return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Campus is out of your scope", null);
                }
                offeringPage = campusProgramOfferingRepo.findByCampusIdOrderByIdDesc(campus.getId(), pageable);
            }
        } else {
            if (requestedCampusId != null && !requestedCampusId.equals(actorCampus.getId())) {
                return ResponseBuilder.build(HttpStatus.FORBIDDEN, "You can only view your campus data", null);
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

        if (request.getCampusId() != null && !request.getCampusId().equals(targetCampus.getId())) {
            targetCampus = resolveTargetCampus(actorCampus, request.getCampusId());
        }

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
        assert targetCampus != null;

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
    public ResponseEntity<ResponseObject> closeCampusProgramOffering(int offeringId) {

        Campus actorCampus = extractActorCampus();
        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản cơ sở trường học", null);
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

        Campus actorCampus = extractActorCampus();
        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản cơ sở trường học", null);
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
        data.put("enrollmentYear", offering.getProgram().getCurriculum().getEnrollmentYear());
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

        String validationError = CounsellorValidation.validateCreateCounsellor(request, accountRepo);

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

        Pageable pageable;
        try {
            pageable = PaginationUtil.buildPageRequest(page, size);
        } catch (IllegalArgumentException e) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, e.getMessage(), null);
        }

        Page<Counsellor> counsellorPage = counsellorRepo.findByCampusId(actorCampus.getId(), pageable);

        PageResponse<Map<String, Object>> pageResponse = PaginationUtil.buildPageResponse(counsellorPage, this::buildCounsellorData);

        return ResponseBuilder.build(HttpStatus.OK, "View counsellor list successfully", pageResponse);
    }

    private Map<String, Object> buildCounsellorData(Counsellor counsellor) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", counsellor.getId());
        data.put("name", counsellor.getName());
        data.put("avatar", counsellor.getAvatar());
        data.put("employeeCode", counsellor.getEmployeeCode());
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
    public ResponseEntity<ResponseObject> updateCampusConfig(int campusId, UpdateCampusConfigRequest request) {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Your account is restricted", null);
        }

        Campus actorCampus = extractActorCampus();

        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "No school campus account found", null);
        }

        Campus campus = campusRepo.findById(campusId).orElse(null);

        if (campus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Campus not found", null);
        }

        // campus xử lý facility
        SchoolConfig facilityData = schoolConfigRepo.findBySchoolIdAndKey(campus.getSchool().getId(), "facilityData").orElse(null);

        List<Map<String, Object>> itemList = new ArrayList<>();

        if (facilityData != null && facilityData.getValue() instanceof Map) {
            Map<String, Object> val = (Map<String, Object>) facilityData.getValue();
            itemList = (List<Map<String, Object>>) val.get("itemList");
        }

        List<Map<String, Object>> mergedFacilityItems = SchoolConfigUtil.mergeFacilityItems(itemList, request.getItemList());

        Map<String, Object> facilityJson = new HashMap<>();
        facilityJson.put("overview", request.getOverview());
        facilityJson.put("itemList", mergedFacilityItems);
        facilityJson.put("imageData", request.getImageJsonData());
        campus.setFacility(facilityJson);

        // campus xử lý operating --> policy detail
        SchoolConfig operationSettingsData = schoolConfigRepo.findBySchoolIdAndKey(campus.getSchool().getId(), "operationSettingsData").orElse(null);

        if (operationSettingsData != null) {
            Map<String, Object> mergedOp = SchoolConfigUtil.mergeOperationConfig(
                    (Map<String, Object>) operationSettingsData.getValue(),
                    request
            );

            String finalPolicyStr = SchoolConfigUtil.convertOperationToPolicyString(mergedOp);
            if (request.getPolicyDetail() != null && !request.getPolicyDetail().isBlank()) {
                finalPolicyStr += "\n------------------------------------------\n";
                finalPolicyStr += "📌 LƯU Ý RIÊNG TẠI CƠ SỞ:\n" + request.getPolicyDetail();
            }

            Map<String, Object> policyJsonb = new HashMap<>();

            policyJsonb.put("minCounsellorPerSlot", request.getMinCounsellorPerSlot());

            policyJsonb.put("fullTextRendered", finalPolicyStr);

            policyJsonb.put("rawCustomNote", request.getPolicyDetail());

            campus.setPolicyDetail(finalPolicyStr);
        }

        campusRepo.save(campus);

        return ResponseBuilder.build(HttpStatus.OK, "Campus config updated successfully", null);
    }

    @Override
    public ResponseEntity<ResponseObject> getCampusConfig(int campusId) {

        Campus campus = campusRepo.findById(campusId).orElse(null);

        if (campus == null) {
            return ResponseBuilder.build(HttpStatus.OK, "Campus not found", null);
        }

        SchoolConfig hqFacility = schoolConfigRepo.findBySchoolIdAndKey(campus.getSchool().getId(), "facilityData").orElse(null);

        SchoolConfig hqOperation = schoolConfigRepo.findBySchoolIdAndKey(campus.getSchool().getId(), "operationSettingsData").orElse(null);

        Map<String, Object> result = new HashMap<>();

        Map<String, Object> hqSection = new HashMap<>();

        hqSection.put("facility", hqFacility != null ? hqFacility.getValue() : null);
        hqSection.put("operation", hqOperation != null ? hqOperation.getValue() : null);
        result.put("hqDefault", hqSection);

        Map<String, Object> campusSection = new HashMap<>();
        campusSection.put("facilityJson", campus.getFacility());
        campusSection.put("policyDetailRendered", campus.getPolicyDetail());

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

        Campus campus = campusRepo.findById(request.getCampusId()).orElse(null);
        if (campus == null) return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Target campus not found", null);

        // 3. Security Check: Đảm bảo cùng School
        if (!campus.getSchool().getId().equals(actorCampus.getSchool().getId())) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Permission denied", null);
        }

        Map<String, Object> workingConfig = SchoolConfigUtil.getWorkingConfig(schoolConfigRepo
                .findBySchoolIdAndKey(campus.getSchool().getId(), "operationSettingsData")
                .orElse(null));

        for (String day : request.getDayOfWeek()) {

            String error = CampusScheduleTemplateValidation.validateCampusScheduleTemplate(
                    request.getTemplateId(),
                    request,
                    day,
                    workingConfig,
                    campusScheduleTemplateRepo);

            if (error != null) {
                return ResponseBuilder.build(HttpStatus.BAD_REQUEST, error, null);
            }

            saveSingleTemplate(request, day, campus);
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
    public ResponseEntity<ResponseObject> viewCampusScheduleTemplateByEachCampus(Integer campusId) {

        Campus campus = campusRepo.findById(campusId).orElse(null);

        if (campus == null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Campus not found", null);
        }

        List<CampusScheduleTemplate> templates = campusScheduleTemplateRepo.findByCampusIdAndActiveTrueOrderByStartTimeAsc(campusId);

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
        return null;
    }

    @Override
    public ResponseEntity<ResponseObject> viewAssignCounsellorIntoSlotList() {
        return null;
    }
}
