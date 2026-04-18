package com.sp26se041.edubridgehcm.services.implementors;

import com.sp26se041.edubridgehcm.enums.BoardingType;
import com.sp26se041.edubridgehcm.enums.CategoryTemplate;
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
import com.sp26se041.edubridgehcm.models.SchoolHoliday;
import com.sp26se041.edubridgehcm.models.TemplateDocx;
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
import com.sp26se041.edubridgehcm.repositories.SchoolHolidayRepo;
import com.sp26se041.edubridgehcm.repositories.TemplateDocxRepo;
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
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

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
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class CampusServiceImpl implements CampusService {

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

    private final CampusResourceQuotaRepo campusResourceQuotaRepo;

    private final SupabaseStorageService supabaseStorageService;

    private final ConversationRepo conversationRepo;

    private final ChatMessageRepo chatMessageRepo;

    private final SchoolHolidayRepo schoolHolidayRepo;

    private final TemplateDocxRepo templateDocxRepo;

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

        String error = CampusProgramOfferingValidation.validateCreateCampusProgramOffering(request, actorCampus, admissionCampaignRepo, programRepo, campusProgramOfferingRepo, campusRepo);

        if (error != null) {

            if (error.contains("đã tồn tại suất tuyển sinh")) {
                return ResponseBuilder.build(HttpStatus.CONFLICT, error, null);
            }

            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, error, null);
        }

        // Lấy lại các entity đã validate để tạo offering
        AdmissionCampaign campaign = admissionCampaignRepo.findById(request.getAdmissionCampaignId()).orElse(null);

        if (campaign == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Không tìm thấy chiến dịch tuyển sinh.", null);
        }

        Program program = programRepo.findByIdAndCurriculum_School_Id(request.getProgramId(), actorCampus.getSchool().getId());

        if (program == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Không tìm thấy chương trình đào tạo.", null);
        }

        BigDecimal basePrice = program.getBaseTuitionFee();

        float adjustmentPercent = (request.getPriceAdjustmentPercentage() != null) ? request.getPriceAdjustmentPercentage() : 0.0f;

        // 3. Calculate Final Tuition
        // Formula: Final = Base * (1 + % / 100)
        BigDecimal multiplier = BigDecimal.valueOf(1 + (adjustmentPercent / 100));
        BigDecimal finalTuition = basePrice.multiply(multiplier).setScale(0, RoundingMode.HALF_UP); // Rounding for VND/Currency

        campusProgramOfferingRepo.save(CampusProgramOffering.builder().campus(actorCampus).admissionCampaign(campaign).program(program).quota(request.getQuota()).remainingQuota(request.getQuota()).learningMode(request.getLearningMode()).priceAdjustmentPercentage(adjustmentPercent).finalTuitionFee(finalTuition).applicationStatus(Status.OPEN).openDate((request.getOpenDate() != null) ? request.getOpenDate() : campaign.getStartDate()).closeDate((request.getCloseDate() != null) ? request.getCloseDate() : campaign.getEndDate()).status(Status.OPEN_ADMISSION_CAMPAIGN).build());

        return ResponseBuilder.build(HttpStatus.OK, "Tạo chương trình tuyển sinh tại cơ sở thành công.", null);
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
    @Transactional
    public ResponseEntity<ResponseObject> updateCampusProgramOffering(UpdateCampusProgramOfferingRequest request) {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Tài khoản của bạn đang bị hạn chế, không thể thực hiện thao tác này.", null);
        }

        Campus actorCampus = extractActorCampus();
        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản cơ sở trường học hoặc bạn không có quyền truy cập.", null);
        }

        if (request.getCampusId() != null && !request.getCampusId().equals(actorCampus.getId())) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Bạn không có quyền cập nhật chương trình tuyển sinh của cơ sở khác.", null);
        }

        CampusProgramOffering offering = campusProgramOfferingRepo.findById(request.getId()).orElse(null);

        if (offering == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Không tìm thấy chương trình tuyển sinh cơ sở (offering).", null);
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
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Chỉ được cập nhật khi chương trình đang ở trạng thái tạm dừng (PAUSED).", null);
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

        return ResponseBuilder.build(HttpStatus.OK, "Cập nhật chương trình tuyển sinh tại cơ sở thành công.", null);
    }

    @Override
    @Transactional
    public ResponseEntity<ResponseObject> closeCampusProgramOffering(Integer offeringId) {

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
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Bạn không có quyền đóng chương trình tuyển sinh của cơ sở khác.", null);
        }

        if (offering.getStatus() == Status.CLOSED) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Chương trình này đã được đóng trước đó.", null);
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

        return ResponseBuilder.build(HttpStatus.OK, (formCount >= offering.getQuota()) ? "Chương trình đã đạt chỉ tiêu và được đóng tự động." : "Chương trình đã được quản trị viên đóng thành công.", null);
    }

    @Override
    @Transactional
    public ResponseEntity<ResponseObject> changeCampusProgramOfferingStatus(int offeringId, Status targetStatus) {

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

        if (offering.getStatus().equals(Status.CLOSED) || offering.getApplicationStatus().equals(Status.FULL)) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Chương trình đã đóng hoặc đã đủ chỉ tiêu, không thể đổi trạng thái.", null);
        }

        if (targetStatus.equals(Status.PAUSED)) {

            if (offering.getApplicationStatus().equals(Status.PAUSED)) {
                return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Chương trình đã ở trạng thái tạm dừng, không thể tạm dừng lại.", null);
            }

            offering.setApplicationStatus(Status.PAUSED);

        } else if (targetStatus == Status.OPEN) {

            if (offering.getApplicationStatus() != Status.PAUSED) {
                return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Chỉ có thể mở lại chương trình khi đang ở trạng thái tạm dừng (PAUSED).", null);
            }

            int formCount = admissionReservationFormRepo.countByCampusProgramOfferingId(offeringId);

            if (formCount >= offering.getQuota()) {
                return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Không thể mở lại vì đã đủ chỉ tiêu.", null);
            }

            offering.setApplicationStatus(Status.OPEN);

        } else {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Chỉ hỗ trợ chuyển sang trạng thái mở (OPEN) hoặc tạm dừng (PAUSED).", null);
        }

        campusProgramOfferingRepo.save(offering);

        return ResponseBuilder.build(HttpStatus.OK, "Cập nhật trạng thái chương trình thành công.", null);
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

        Counsellor counsellor = counsellorRepo.save(Counsellor.builder().account(account).campus(actorCampus).avatar(request.getAvatar()).employeeCode(UUID.randomUUID()).build());

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

        if (actorCampus == null) return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Không tìm thấy cơ sở trường học.", null);

        // 1. Lấy thông tin Quota hiện tại của Campus phụ
        var quotaOpt = campusResourceQuotaRepo.findByCampusIdAndResourceType(actorCampus.getId(), ResourceType.COUNSELLOR);
        int maxQuota = quotaOpt.map(CampusResourceQuota::getMaxQuota).orElse(0);
        long currentUsage = counsellorRepo.countByCampusId(actorCampus.getId());

        // 2. Tìm thông tin Campus chính (để lấy Email nhận)
        Campus primaryCampus = campusRepo.findAllBySchoolId(actorCampus.getSchool().getId()).stream()
                .filter(Campus::getIsPrimaryBranch)
                .findFirst()
                .orElse(null);

        // 3. Đóng gói dữ liệu trả về cho FE
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
            filteredOp.put("academicCalendar", fullOp.get("academicCalendar"));
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

        if (request.getTemplateId() != null && request.getTemplateId() > 0) {

            CampusScheduleTemplate existing = campusScheduleTemplateRepo.findById(request.getTemplateId()).orElse(null);

            if (existing == null) {
                return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Không tìm thấy khung lịch (template).", null);
            }

            if (!existing.getCampus().getId().equals(actorCampus.getId())) {
                return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Bạn không được phép sửa khung lịch của cơ sở khác.", null);
            }

            if (request.getDayOfWeek().size() != 1) {
                return ResponseBuilder.build(HttpStatus.BAD_REQUEST,
                        "Khi cập nhật khung lịch, chỉ được gửi đúng một thứ trong dayOfWeek. "
                                + "Để thêm khung cho nhiều ngày, hãy tạo khung mới (không gửi templateId) hoặc gọi API lần lượt cho từng ngày.",
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

        LocalTime[] window = SchoolConfigUtil.resolveShiftTimeWindowForSessionType(workingConfig, request.getSessionType());
        if (window == null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST,
                    "Không tìm thấy ca làm việc trong cấu hình vận hành trường khớp với buổi " + request.getSessionType()
                            + ". Hãy cấu hình workShifts trong operationSettingsData (campus chính / HQ).", null);
        }
        DateTimeFormatter hm = DateTimeFormatter.ofPattern("HH:mm");
        String startStr = window[0].format(hm);
        String endStr = window[1].format(hm);

        boolean expandToPolicySlots = Boolean.TRUE.equals(request.getExpandToPolicySlots());

        for (String day : request.getDayOfWeek()) {

            if (expandToPolicySlots) {
                if (slotDurationMinutes == null || slotDurationMinutes <= 0) {
                    return ResponseBuilder.build(HttpStatus.BAD_REQUEST,
                            "Cần cấu hình slotDurationInMinutes lớn hơn 0 trong vận hành (HQ hoặc campus) để tách khung theo độ dài một slot.", null);
                }
                List<String[]> windows;
                try {
                    LocalTime rangeStart = LocalTime.parse(startStr);
                    LocalTime rangeEnd = LocalTime.parse(endStr);
                    windows = SchoolConfigUtil.splitRangeIntoPolicySlotWindows(
                            rangeStart, rangeEnd, slotDurationMinutes, bufferBetweenSlotsMinutes);
                } catch (IllegalArgumentException | java.time.format.DateTimeParseException | IllegalStateException e) {
                    return ResponseBuilder.build(HttpStatus.BAD_REQUEST, e.getMessage(), null);
                }

                for (String[] w : windows) {
                    String error = CampusScheduleTemplateValidation.validateCampusScheduleTemplate(
                            null,
                            w[0],
                            w[1],
                            request.getSessionType(),
                            day,
                            workingConfig,
                            campusScheduleTemplateRepo,
                            actorCampus,
                            slotDurationMinutes,
                            bufferBetweenSlotsMinutes);
                    if (error != null) {
                        return ResponseBuilder.build(HttpStatus.BAD_REQUEST, error, null);
                    }
                    saveSingleTemplate(null, day, actorCampus, w[0], w[1], request.getSessionType());
                    campusScheduleTemplateRepo.flush();
                }
            } else {

                String error = CampusScheduleTemplateValidation.validateCampusScheduleTemplate(
                        request.getTemplateId(),
                        startStr,
                        endStr,
                        request.getSessionType(),
                        day,
                        workingConfig,
                        campusScheduleTemplateRepo,
                        actorCampus,
                        slotDurationMinutes,
                        bufferBetweenSlotsMinutes);

                if (error != null) {
                    return ResponseBuilder.build(HttpStatus.BAD_REQUEST, error, null);
                }

                saveSingleTemplate(request.getTemplateId(), day, actorCampus, startStr, endStr, request.getSessionType());
            }
        }

        return ResponseBuilder.build(HttpStatus.OK, "Xử lý khung lịch (template) thành công.", null);
    }

    private void saveSingleTemplate(Integer templateId, String day, Campus campus, String startTime, String endTime, String sessionType) {
        CampusScheduleTemplate template;

        boolean isUpdate = templateId != null && templateId > 0;

        if (isUpdate) {
            template = campusScheduleTemplateRepo.findById(templateId).get();
        } else {

            template = new CampusScheduleTemplate();
            template.setCampus(campus);
            template.setCreatedDate(LocalDate.now());
        }

        template.setDayOfWeek(day.toUpperCase());
        template.setStartTime(LocalTime.parse(startTime));
        template.setEndTime(LocalTime.parse(endTime));
        template.setSessionType(SessionType.valueOf(sessionType));
        template.setUpdatedDate(LocalDate.now());
        template.setActive(true);

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

        if (isAssign) {
            if (request.getStartDate() == null && request.getEndDate() == null) {
                Optional<SchoolConfigUtil.AcademicAssignmentDateRange> semesterRange =
                        SchoolConfigUtil.resolveAssignmentDateRangeFromAcademicCalendar(effectiveOperationSettings);
                if (semesterRange.isEmpty()) {
                    return ResponseBuilder.build(HttpStatus.BAD_REQUEST,
                            "Vui lòng nhập Từ ngày và Đến ngày, hoặc cấu hình đủ phạm vi học kỳ (academicCalendar.term1/term2).", null);
                }
                SchoolConfigUtil.AcademicAssignmentDateRange r = semesterRange.get();
                request.setStartDate(r.start());
                request.setEndDate(r.end());
            } else if (request.getStartDate() == null || request.getEndDate() == null) {
                return ResponseBuilder.build(HttpStatus.BAD_REQUEST,
                        "Ngày bắt đầu và ngày kết thúc phải cùng điền hoặc cùng để trống khi gán.", null);
            }
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
                return ResponseBuilder.build(HttpStatus.BAD_REQUEST, e.getMessage(), null);
            }
            counsellorSlotRepo.flush();
            clearPersistenceContextBeforeSnapshot();
            List<Map<String, Object>> snapshotById = loadAssignedSlotsSnapshot(actorCampus.getId());
            Map<String, Object> bodyById = new LinkedHashMap<>();
            bodyById.put("action", actionInput);
            bodyById.put("removedSlotIds", List.copyOf(unassignSlotIds));
            bodyById.put("slots", snapshotById);
            return ResponseBuilder.build(HttpStatus.OK, "Hủy gán chuyên viên tư vấn thành công.", bodyById);
        }

        List<Integer> templateIdList = resolveAssignTemplateIds(request);
        if (templateIdList.isEmpty()) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Cần chỉ định templateIds (danh sách không rỗng).", null);
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

            String error = CounsellorSlotValidation.validateAssignRequest(effectiveOperationSettings, request, actorCampus, template, counsellors, allCurrentSlots);
            if (error != null) {
                return ResponseBuilder.build(HttpStatus.BAD_REQUEST, error, null);
            }
        }

        List<SchoolHoliday> holidayList = mergeSchoolHolidaysForCampus(actorCampus.getSchool().getId(), actorCampus.getId());

        if (isAssign && request.getStartDate() != null && request.getEndDate() != null) {
            String holidayBlock = SchoolConfigUtil.validateAssignmentRangeAgainstBlockingHolidays(
                    request.getStartDate(), request.getEndDate(), holidayList, actorCampus.getId());
            if (holidayBlock != null) {
                return ResponseBuilder.build(HttpStatus.BAD_REQUEST, holidayBlock, null);
            }
        }

        try {
            for (Integer tid : templateIdList) {
                CampusScheduleTemplate template = campusScheduleTemplateRepo.findById(tid).orElseThrow();

                for (Counsellor counsellor : counsellors) {

                    List<CounsellorSlot> counsellorSlots = allCurrentSlots.stream()
                            .filter(s -> s.getCounsellor().getId().equals(counsellor.getId()))
                            .toList();

                    if (isAssign) {
                        handleAssignAction(counsellor, template, request, counsellorSlots);
                    } else {
                        handleUnassignAction(template, request, counsellorSlots, counsellor);
                    }
                }
                counsellorSlotRepo.flush();
                allCurrentSlots = counsellorSlotRepo.findByCampusScheduleTemplate_Campus_Id(actorCampus.getId());
            }
        } catch (IllegalArgumentException e) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, e.getMessage(), null);
        }

        counsellorSlotRepo.flush();
        clearPersistenceContextBeforeSnapshot();
        List<Map<String, Object>> slotsSnapshot = loadAssignedSlotsSnapshot(actorCampus.getId());
        Map<String, Object> resultBody = new LinkedHashMap<>();
        resultBody.put("action", actionInput);
        resultBody.put("slots", slotsSnapshot);

        return ResponseBuilder.build(HttpStatus.OK, (isAssign ? "Gán" : "Hủy gán") + " chuyên viên tư vấn thành công.", resultBody);
    }

    /** Tránh snapshot sau DELETE vẫn đọc entity cũ trong persistence context (first-level cache). */
    private void clearPersistenceContextBeforeSnapshot() {
        entityManager.flush();
        entityManager.clear();
    }

    /** Danh sách gán hiện tại của campus — cùng cấu trúc phần tử với GET /counsellor/slots/assigned. */
    private List<Map<String, Object>> loadAssignedSlotsSnapshot(Integer campusId) {
        List<CounsellorSlot> slots = counsellorSlotRepo.findByCampusScheduleTemplate_Campus_Id(campusId);
        return slots.stream()
                .filter(s -> s.getStatus() != Status.SLOT_UNASSIGNED)
                .map(this::buildManagementSlotData)
                .toList();
    }

    /** Danh sách id khung lịch (bỏ trùng, bỏ null). */
    private static List<Integer> resolveAssignTemplateIds(AssignCounsellorIntoSlotsRequest request) {
        if (request.getTemplateIds() == null || request.getTemplateIds().isEmpty()) {
            return List.of();
        }
        return request.getTemplateIds().stream().filter(id -> id != null).distinct().toList();
    }

    /** Id bản ghi counsellor_slot khi UNASSIGN theo slotId (bỏ trùng, bỏ null). */
    private static List<Integer> resolveUnassignSlotIds(AssignCounsellorIntoSlotsRequest request) {
        if (request.getSlotIds() == null || request.getSlotIds().isEmpty()) {
            return List.of();
        }
        return request.getSlotIds().stream().filter(Objects::nonNull).distinct().toList();
    }

    /** Hủy gán: xóa từng bản ghi theo khóa chính (khớp GET assigned → slotId). */
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
            CounsellorSlotValidation.validateNoActiveConsultation(slot);
            slot.setStatus(Status.SLOT_UNASSIGNED);
            counsellorSlotRepo.save(slot);
            counsellorSlotRepo.flush();
        }
    }

    private void handleAssignAction(Counsellor counsellor, CampusScheduleTemplate template, AssignCounsellorIntoSlotsRequest request, List<CounsellorSlot> existingSlots) {

        for (CounsellorSlot slot : existingSlots) {
            boolean sameWindow = slot.getCampusScheduleTemplate().getId().equals(template.getId())
                    && slot.getStartDate().equals(request.getStartDate())
                    && slot.getEndDate().equals(request.getEndDate());
            if (sameWindow) {
                if (slot.getStatus() == Status.SLOT_UNASSIGNED) {
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
            boolean isDateOverlap = request.getStartDate().isBefore(slot.getEndDate().plusDays(1)) && request.getEndDate().isAfter(slot.getStartDate().minusDays(1));

            boolean isDayOfWeekSame = slot.getCampusScheduleTemplate().getDayOfWeek().equalsIgnoreCase(template.getDayOfWeek());

            boolean isTimeOverlap = template.getStartTime().isBefore(slot.getCampusScheduleTemplate().getEndTime()) && template.getEndTime().isAfter(slot.getCampusScheduleTemplate().getStartTime());

            if (isDateOverlap && isDayOfWeekSame && isTimeOverlap) {
                throw new IllegalArgumentException("Chuyên viên " + counsellor.getName() + " đã có lịch trùng khoảng thời gian hoặc khung giờ này.");
            }
        }
        counsellorSlotRepo.save(CounsellorSlot.builder()
                .campusScheduleTemplate(template)
                .counsellor(counsellor)
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
                .findFirst()
                .orElse(null);

        if (targetSlot == null) {
            throw new IllegalArgumentException(
                    "Không tìm thấy lịch gán của " + counsellor.getName() + " cho khung lịch này trong khoảng ngày đã chọn.");
        }
        CounsellorSlotValidation.validateNoActiveConsultation(targetSlot);
        targetSlot.setStatus(Status.SLOT_UNASSIGNED);
        counsellorSlotRepo.save(targetSlot);
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
    public ResponseEntity<ResponseObject> getAvailableSlots(LocalDate targetDate) {

        Campus actorCampus = extractActorCampus();
        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản cơ sở trường học hoặc bạn không có quyền truy cập.", null);
        }

        String dayOfWeek = targetDate.getDayOfWeek().name().substring(0, 3);

        SchoolConfig hqOp = schoolConfigRepo.findBySchoolIdAndKey(actorCampus.getSchool().getId(), "operationSettingsData").orElse(null);
        Map<String, Object> effectiveOperation = SchoolConfigUtil.getEffectiveOperationSettingsMap(hqOp, actorCampus);
        List<SchoolHoliday> holidays = mergeSchoolHolidaysForCampus(actorCampus.getSchool().getId(), actorCampus.getId());

        List<CounsellorSlot> assignedSlots = counsellorSlotRepo.findByStartDateLessThanEqualAndEndDateGreaterThanEqualAndCampusScheduleTemplate_DayOfWeekAndCampusScheduleTemplate_Campus_IdAndCampusScheduleTemplate_ActiveTrue(targetDate, // khớp với StartDateLessThanEqual
                targetDate, dayOfWeek, actorCampus.getId());

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
