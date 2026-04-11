package com.sp26se041.edubridgehcm.services.implementors;

import com.sp26se041.edubridgehcm.configurations.VNPayConfig;
import com.sp26se041.edubridgehcm.enums.BoardingType;
import com.sp26se041.edubridgehcm.enums.CurriculumType;
import com.sp26se041.edubridgehcm.enums.FeeUnit;
import com.sp26se041.edubridgehcm.enums.LanguageInstruction;
import com.sp26se041.edubridgehcm.enums.LearningMethod;
import com.sp26se041.edubridgehcm.enums.ProgramCategory;
import com.sp26se041.edubridgehcm.enums.Role;
import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.models.Account;
import com.sp26se041.edubridgehcm.models.AdmissionCampaign;
import com.sp26se041.edubridgehcm.models.Campus;
import com.sp26se041.edubridgehcm.models.CampusProgramOffering;
import com.sp26se041.edubridgehcm.models.CampusScheduleTemplate;
import com.sp26se041.edubridgehcm.models.Counsellor;
import com.sp26se041.edubridgehcm.models.Curriculum;
import com.sp26se041.edubridgehcm.models.OpenDayEvent;
import com.sp26se041.edubridgehcm.models.Parent;
import com.sp26se041.edubridgehcm.models.PaymentTransaction;
import com.sp26se041.edubridgehcm.models.Program;
import com.sp26se041.edubridgehcm.models.School;
import com.sp26se041.edubridgehcm.models.SchoolSubscription;
import com.sp26se041.edubridgehcm.models.Subscription;
import com.sp26se041.edubridgehcm.repositories.AccountRepo;
import com.sp26se041.edubridgehcm.repositories.AdmissionCampaignRepo;
import com.sp26se041.edubridgehcm.repositories.AdmissionReservationFormRepo;
import com.sp26se041.edubridgehcm.repositories.CampusProgramOfferingRepo;
import com.sp26se041.edubridgehcm.repositories.CampusRepo;
import com.sp26se041.edubridgehcm.repositories.CampusScheduleTemplateRepo;
import com.sp26se041.edubridgehcm.repositories.CurriculumRepo;
import com.sp26se041.edubridgehcm.repositories.FavouriteSchoolRepo;
import com.sp26se041.edubridgehcm.repositories.OpenDayEventRepo;
import com.sp26se041.edubridgehcm.repositories.ParentRepo;
import com.sp26se041.edubridgehcm.repositories.PaymentTransactionRepo;
import com.sp26se041.edubridgehcm.repositories.ProgramRepo;
import com.sp26se041.edubridgehcm.repositories.SchoolConfigRepo;
import com.sp26se041.edubridgehcm.repositories.SchoolRepo;
import com.sp26se041.edubridgehcm.repositories.SchoolSubscriptionRepo;
import com.sp26se041.edubridgehcm.repositories.SubscriptionRepo;
import com.sp26se041.edubridgehcm.requests.CreateAdmissionCampaignTemplateRequest;
import com.sp26se041.edubridgehcm.requests.CreateCampusRequest;
import com.sp26se041.edubridgehcm.requests.CreateOpenDayEventRequest;
import com.sp26se041.edubridgehcm.requests.CreateSubscriptionRequest;
import com.sp26se041.edubridgehcm.requests.CurriculumRequest;
import com.sp26se041.edubridgehcm.requests.ProgramRequest;
import com.sp26se041.edubridgehcm.requests.UpdateAdmissionCampaignTemplateRequest;
import com.sp26se041.edubridgehcm.responses.PageResponse;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.SchoolService;
import com.sp26se041.edubridgehcm.utils.AccountRestrictionUtil;
import com.sp26se041.edubridgehcm.utils.AuthRequestUtil;
import com.sp26se041.edubridgehcm.utils.CurriculumNamingUtil;
import com.sp26se041.edubridgehcm.utils.ExcelUtil;
import com.sp26se041.edubridgehcm.utils.PaginationUtil;
import com.sp26se041.edubridgehcm.utils.ResponseBuilder;
import com.sp26se041.edubridgehcm.utils.SchoolUtil;
import com.sp26se041.edubridgehcm.validations.school.AdmissionCampaignValidation;
import com.sp26se041.edubridgehcm.validations.school.CampusValidation;
import com.sp26se041.edubridgehcm.validations.school.CurriculumValidation;
import com.sp26se041.edubridgehcm.validations.school.ProgramValidation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchoolServiceImpl implements SchoolService {

    private final CampusRepo campusRepo;

    private final AdmissionCampaignRepo admissionCampaignRepo;

    private final CampusProgramOfferingRepo campusProgramOfferingRepo;

    private final ProgramRepo programRepo;

    private final AccountRepo accountRepo;

    private final OpenDayEventRepo openDayEventRepo;

    private final CurriculumRepo curriculumRepo;

    private final AdmissionReservationFormRepo admissionReservationFormRepo;

    private final SchoolRepo schoolRepo;

    private final FavouriteSchoolRepo favouriteSchoolRepo;

    private final ParentRepo parentRepo;

    private final CampusScheduleTemplateRepo campusScheduleTemplateRepo;

    private final SubscriptionRepo subscriptionRepo;

    private final SchoolSubscriptionRepo schoolSubscriptionRepo;

    private final PaymentTransactionRepo paymentTransactionRepo;

    private final SchoolConfigRepo schoolConfigRepo;

    @Override
    @Transactional
    public ResponseEntity<ResponseObject> createCampus(CreateCampusRequest request) {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Your account is restricted", null);
        }

        Campus actorCampus = extractActorCampus();

        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "No school campus account found", null);
        }

        if (!actorCampus.getIsPrimaryBranch()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Only primary campus can add new campus", null);
        }

        String error = CampusValidation.validateCreateCampus(request, accountRepo, campusRepo, actorCampus.getSchool().getId());
        if (error != null && !error.isBlank()) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, error, null);
        }

        BoardingType boardingType = parseBoardingType(request.getBoardingType());

        if (boardingType == null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Boarding type is invalid. Accepted values: NONE, FULL_BOARDING, SEMI_BOARDING, BOTH", null);
        }

        Account acc = accountRepo.save(Account.builder().email(normalize(request.getEmail())).role(Role.SCHOOL).status(Status.ACCOUNT_ACTIVE).registerDate(LocalDate.now()).firstLogin(true).isRestricted(false).build());

        Campus campus = campusRepo.save(Campus.builder().school(actorCampus.getSchool()).account(acc).name(normalize(request.getName())).address(normalize(request.getAddress())).phoneNumber(normalize(request.getPhone())).city(normalize(request.getCity())).district(normalize(request.getDistrict())).ward(normalize(request.getWard())).boardingType(boardingType).latitude(request.getLatitude()).longitude(request.getLongitude()).status(Status.ACTIVE).isPrimaryBranch(false).build());

        Map<String, Object> data = new HashMap<>();
        data.put("campus", buildCampusData(campus));
        data.put("account", buildAccountData(acc));

        return ResponseBuilder.build(HttpStatus.OK, "Create campus successfully", data);
    }

    @Override
    public ResponseEntity<ResponseObject> viewCampusList(int page, int pageSize) {
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

        PageResponse<Map<String, Object>> pageResponse;
        if (actorCampus.getIsPrimaryBranch()) {
            Page<Campus> campusPage = campusRepo.findBySchoolIdOrderByIsPrimaryBranchDescIdDesc(actorCampus.getSchool().getId(), pageable);
            pageResponse = PaginationUtil.buildPageResponse(campusPage, this::buildCampusData);
        } else {
            List<Campus> selfCampus = List.of(actorCampus);
            pageResponse = PageResponse.<Map<String, Object>>builder().items(selfCampus.stream().map(this::buildCampusData).toList()).currentPage(0).pageSize(selfCampus.size()).totalItems(selfCampus.size()).totalPages(1).hasNext(false).hasPrevious(false).build();
        }

        return ResponseBuilder.build(HttpStatus.OK, "View campus list successfully", pageResponse);
    }

    private Map<String, Object> buildCampusData(Campus campus) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", campus.getId());
        data.put("name", campus.getName());
        data.put("address", campus.getAddress());
        data.put("city", campus.getCity());
        data.put("district", campus.getDistrict());
        data.put("ward", campus.getWard());
        data.put("latitude", campus.getLatitude());
        data.put("longitude", campus.getLongitude());
        data.put("boardingType", campus.getBoardingType() != null ? campus.getBoardingType().name() : null);
        data.put("boardingTypeLabel", campus.getBoardingType() != null ? campus.getBoardingType().getValue() : null);
        data.put("phoneNumber", campus.getPhoneNumber());
        data.put("status", campus.getStatus());
        data.put("isPrimaryBranch", campus.getIsPrimaryBranch());
        data.put("schoolId", campus.getSchool().getId());

        Account acc = campus.getAccount();
        data.put("account", buildAccountData(acc));
        data.put("imageJson", campus.getImageJson());
        data.put("facility", campus.getFacility());
        data.put("policyDetail", campus.getPolicyDetail());
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

    @Override
    @Transactional
    public ResponseEntity<ResponseObject> createAdmissionCampaignTemplate(CreateAdmissionCampaignTemplateRequest request) {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Your account is restricted", null);
        }

        Campus actorCampus = extractActorCampus();

        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "No school campus account found", null);
        }

        // actor campus co phai la primary campus ko
        if (!actorCampus.getIsPrimaryBranch()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Campus account is invalid", null);
        }

        String error = AdmissionCampaignValidation.validationCreateAdmissionCampaignTemplate(request, actorCampus, admissionCampaignRepo);

        if ((error != null)) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, error, null);
        }

        AdmissionCampaign admissionCampaign = AdmissionCampaign.builder().school(actorCampus.getSchool()).name(normalize(request.getName())).description(normalize(request.getDescription())).year(request.getYear()).startDate(request.getStartDate()).endDate(request.getEndDate()).status(Status.DRAFT_ADMISSION_CAMPAIGN).build();
        admissionCampaignRepo.save(admissionCampaign);

        return ResponseBuilder.build(HttpStatus.CREATED, "Create campaign template successfully", null);
    }

    @Override
    public ResponseEntity<ResponseObject> cloneAdmissionCampaign(int id) {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Your account is restricted", null);
        }

        Campus actorCampus = extractActorCampus();

        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "No school campus account found", null);
        }

        // actor campus co phai la primary campus ko
        if (!actorCampus.getIsPrimaryBranch()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Campus account is invalid", null);
        }

        AdmissionCampaign oldCampaign = admissionCampaignRepo.findById(id).orElse(null);
        if (oldCampaign == null)
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Original campaign not found", null);

        // 1. Tạo một Request giả từ dữ liệu cũ
        CreateAdmissionCampaignTemplateRequest request = new CreateAdmissionCampaignTemplateRequest();
        request.setName(oldCampaign.getName() + " (Revised)");
        request.setDescription(oldCampaign.getDescription());
        request.setYear(oldCampaign.getYear());
        request.setStartDate(oldCampaign.getStartDate());
        request.setEndDate(oldCampaign.getEndDate());

        String error = AdmissionCampaignValidation.validationCreateAdmissionCampaignTemplate(request, actorCampus, admissionCampaignRepo);

        if (error != null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, error, null);
        }

        AdmissionCampaign newCampaign = AdmissionCampaign.builder().school(oldCampaign.getSchool()).name(request.getName()).description(request.getDescription()).year(request.getYear()).startDate(request.getStartDate()).endDate(request.getEndDate()).status(Status.DRAFT_ADMISSION_CAMPAIGN).build();

        admissionCampaignRepo.save(newCampaign);

        return ResponseBuilder.build(HttpStatus.CREATED, "Cloned successfully!", buildCampaignData(newCampaign));
    }

    @Override
    @Transactional
    public ResponseEntity<ResponseObject> updateAdmissionCampaignTemplate(UpdateAdmissionCampaignTemplateRequest request) {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Your account is restricted", null);
        }

        Campus actorCampus = extractActorCampus();

        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "No school campus account found", null);
        }

        // actor campus co phai la primary campus ko
        if (!actorCampus.getIsPrimaryBranch()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Campus account is invalid", null);
        }

        if (request == null || request.getAdmissionCampaignTemplateId() <= 0) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Campaign template id is required", null);
        }

        AdmissionCampaign admissionCampaign = admissionCampaignRepo.findById(request.getAdmissionCampaignTemplateId()).orElse(null);

        if (admissionCampaign == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Campaign template not found", null);
        }

        //Check Scope (Đảm bảo không sửa nhầm trường khác)
        if (!admissionCampaign.getSchool().getId().equals(actorCampus.getSchool().getId())) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Campaign template is out of your school scope", null);
        }

        //Chỉ cho sửa khi trạng thái là DRAFT ==> đang sửa sao cho update
        if (!admissionCampaign.getStatus().equals(Status.DRAFT_ADMISSION_CAMPAIGN)) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Only DRAFT campaigns can be updated. This campaign is already " + admissionCampaign.getStatus(), null);
        }

        //Chỉ cho sửa năm hiện tại
        if (admissionCampaign.getYear() < LocalDate.now().getYear()) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Cannot update past campaigns", null);
        }

        String error = AdmissionCampaignValidation.validationUpdateAdmissionCampaignTemplate(request, admissionCampaignRepo);

        if (error != null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, error, null);
        }

        admissionCampaign.setName(normalize(request.getName()));
        admissionCampaign.setDescription(normalize(request.getDescription()));
        admissionCampaign.setYear(request.getYear());
        admissionCampaign.setStartDate(request.getStartDate());
        admissionCampaign.setEndDate(request.getEndDate());
        admissionCampaignRepo.save(admissionCampaign);

        return ResponseBuilder.build(HttpStatus.OK, "Update campaign template successfully", null);
    }

    @Override
    @Transactional
    public ResponseEntity<ResponseObject> publishAdmissionCampaignStatus(int id) {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Your account is restricted", null);
        }

        //kiểm tra Actor & Quyền (Tương tự Create/Update)
        Campus actorCampus = extractActorCampus();
        if (actorCampus == null || !actorCampus.getIsPrimaryBranch()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Only primary campus can change status", null);
        }

        //Tìm Campaign
        AdmissionCampaign campaign = admissionCampaignRepo.findById(id).filter(c -> c.getSchool().getId().equals(actorCampus.getSchool().getId())).orElse(null);

        if (campaign == null) return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Campaign not found", null);

        if (!campaign.getStatus().equals(Status.DRAFT_ADMISSION_CAMPAIGN)) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Only DRAFT campaigns can be published", null);
        }

        if (admissionCampaignRepo.existsBySchoolIdAndYearAndStatus(actorCampus.getSchool().getId(), campaign.getYear(), Status.OPEN_ADMISSION_CAMPAIGN)) {
            return ResponseBuilder.build(HttpStatus.CONFLICT, "Academic year " + campaign.getYear() + " already has an OPEN campaign. Please close it before publishing a new one.", null);
        }

        // Kiểm tra tính hợp lệ của ngày kết thúc trước khi Publish
        if (LocalDate.now().isAfter(campaign.getEndDate())) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Cannot publish an expired campaign. Update End Date first.", null);
        }

        campaign.setStatus(Status.OPEN_ADMISSION_CAMPAIGN);
        admissionCampaignRepo.save(campaign);

        return ResponseBuilder.build(HttpStatus.OK, "Campaign published! Now Campuses can register offerings.", null);
    }

    @Override
    @Transactional
    public ResponseEntity<ResponseObject> cancelAdmissionCampaign(int id, String reason) {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Your account is restricted", null);
        }

        //kiểm tra Actor & Quyền (Tương tự Create/Update)
        Campus actorCampus = extractActorCampus();
        if (actorCampus == null || !actorCampus.getIsPrimaryBranch()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Only primary campus can change status", null);
        }

        //Tìm Campaign
        AdmissionCampaign campaign = admissionCampaignRepo.findById(id).filter(c -> c.getSchool().getId().equals(actorCampus.getSchool().getId())).orElse(null);

        if (campaign == null) return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Campaign not found", null);

        // 2. Chỉ cho hủy nếu đang OPEN
        if (campaign.getStatus() == Status.CANCELLED_ADMISSION_CAMPAIGN) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Campaign already inactive", null);
        }

        // 3. Kiểm tra xem có hồ sơ nào đang bám vào các Offering của Campaign này không
        // Bạn nên đếm các hồ sơ CHƯA HOÀN THÀNH (Ví dụ: PENDING, PROCESSING)
        long activeProfilesCount = admissionReservationFormRepo.countByCampusProgramOffering_AdmissionCampaign_Id(id);

        if (activeProfilesCount > 0) {
            return ResponseBuilder.build(HttpStatus.PRECONDITION_FAILED, String.format("Cannot cancel campaign. There are %d active registration profiles linked to this campaign. " + "Please Reject or Process all profiles before cancelling to ensure student data integrity.", activeProfilesCount), null);
        }

        campaign.setStatus(Status.CANCELLED_ADMISSION_CAMPAIGN);
        campaign.setReason(normalize(reason));
        admissionCampaignRepo.save(campaign);

        //Hủy toàn bộ Offering con ==> cha bị hủy thì con của nó cũng phải ăn theo
        // 5. Cập nhật toàn bộ Offering con sang trạng thái CANCELLED
        List<CampusProgramOffering> offerings = campusProgramOfferingRepo.findByAdmissionCampaignId(id);
        if (!offerings.isEmpty()) {
            for (CampusProgramOffering offering : offerings) {
                offering.setApplicationStatus(Status.CANCELLED_ADMISSION_CAMPAIGN);
            }
            campusProgramOfferingRepo.saveAll(offerings);
        }

        return ResponseBuilder.build(HttpStatus.OK, "Cancelled successfully", null);
    }

    @Override
    public ResponseEntity<ResponseObject> viewAdmissionCampaignTemplate(int year) {

        Campus actorCampus = extractActorCampus();

        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "No school campus account found", null);
        }

        int schoolId = actorCampus.getSchool().getId();

        if (year > 0) {
            List<AdmissionCampaign> campaigns = admissionCampaignRepo.findBySchoolIdAndYearOrderByStatusAsc(schoolId, year);

            if (campaigns == null) {
                return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Campaign template not found", null);
            }

            List<Map<String, Object>> data = campaigns.stream().map(this::buildCampaignData).toList();

            return ResponseBuilder.build(HttpStatus.OK, "View campaigns for year " + year + " successfully", data);
        }

        List<AdmissionCampaign> campaignList = admissionCampaignRepo.findBySchoolIdOrderByYearDesc(schoolId);

        List<Map<String, Object>> data = campaignList.stream().map(this::buildCampaignData).toList();

        return ResponseBuilder.build(HttpStatus.OK, "View campaign template list successfully", data);
    }

    private Status autoCheckAndExpireStatus(AdmissionCampaign admissionCampaign) {

        if (admissionCampaign.getStatus().equals(Status.OPEN_ADMISSION_CAMPAIGN) && admissionCampaign.getEndDate().isBefore(LocalDate.now())) {
            admissionCampaign.setStatus(Status.EXPIRED);
            admissionCampaignRepo.save(admissionCampaign);
        }

        //Cập nhật tất cả các ngành học (Offerings) của chiến dịch này
        List<CampusProgramOffering> offerings = campusProgramOfferingRepo.findByAdmissionCampaignId(admissionCampaign.getId());
        if (offerings != null && !offerings.isEmpty()) {
            for (CampusProgramOffering offering : offerings) {
                // Chỉ đóng những cái đang mở hoặc tạm dừng, không chạm vào cái đã FULL/CLOSED thủ công
                if (offering.getApplicationStatus() == Status.OPEN_ADMISSION_CAMPAIGN || offering.getApplicationStatus() == Status.PAUSED) {
                    offering.setApplicationStatus(Status.EXPIRED);
                }
            }
            campusProgramOfferingRepo.saveAll(offerings);
        }
        return admissionCampaign.getStatus();
    }

    @Override
    @Transactional
    public ResponseEntity<ResponseObject> upsertCurriculum(CurriculumRequest request) {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Your account is restricted", null);
        }

        Campus actorCampus = extractActorCampus();

        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "No school campus account found", null);
        }

        // actor campus co phai la primary campus ko
        if (!actorCampus.getIsPrimaryBranch()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Campus account is invalid", null);
        }

        String error = CurriculumValidation.validationUpsertCurriculum(request, curriculumRepo, programRepo);

        if (error != null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, error, null);
        }

        Curriculum targetCurriculum;


        boolean isNew = request.getCurriculumId() == null || request.getCurriculumId() <= 0;

        if (isNew) {
            targetCurriculum = buildNewCurriculum(request, actorCampus.getSchool());
        } else {
            Curriculum existingCurriculum = curriculumRepo.findById(request.getCurriculumId()).orElse(null);

            if (existingCurriculum == null) {
                return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Curriculum not found", null);
            }

            if (Status.CUR_DRAFT.equals(existingCurriculum.getCurriculumStatus())) {
                // Nếu là DRAFT, cho phép sửa
                applyRequestToCurriculum(existingCurriculum, request);
                targetCurriculum = existingCurriculum;
            } else {
                // Nếu là ACTIVE, cấm sửa đè -> Tự động CLONE ra bản mới (DRAFT)
                // Bản cũ (ACTIVE) vẫn còn đó cho các Program cũ dùng
                targetCurriculum = evolveFromExisting(existingCurriculum, request);
            }
        }

        targetCurriculum.setCurriculumStatus(Status.CUR_DRAFT);
        curriculumRepo.save(targetCurriculum);
        return ResponseBuilder.build(isNew ? HttpStatus.CREATED : HttpStatus.OK, isNew ? "Created draft successfully" : "Updated draft successfully", null);
    }

    @Override
    @Transactional
    public ResponseEntity<ResponseObject> handleCurriculumAction(int id, String action) {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Your account is restricted", null);
        }

        Campus actorCampus = extractActorCampus();

        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "No school campus account found", null);
        }

        // actor campus co phai la primary campus ko
        if (!actorCampus.getIsPrimaryBranch()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Campus account is invalid", null);
        }

        // 2. Tìm bản ghi muốn kích hoạt
        Curriculum target = curriculumRepo.findById(id).orElse(null);
        if (target == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Curriculum not found", null);
        }

        switch (action.toUpperCase()) {

            case "PUBLISH":
                //Chỉ cho phép Publish nếu đang là DRAFT
                if (!Status.CUR_DRAFT.equals(target.getCurriculumStatus())) {
                    return ResponseBuilder.build(HttpStatus.OK, "Only Draft can be published", null);
                }

                //Nếu bạn đang chuẩn bị PUBLISH một bản nháp mới cho "Khối Tự Nhiên - 2024",
                // hệ thống sẽ lục tìm: "Hệ thống đã có bản nào là Tự Nhiên - 2024 đang chạy (ACTIVE) chưa?"
                // . Nếu có, bản đó lập tức bị coi là "phiên bản cũ" và bị đẩy vào kho ARCHIVED.
                Curriculum currentActive = curriculumRepo.findByGroupCodeAndEnrollmentYearAndCurriculumStatus(target.getGroupCode(), target.getEnrollmentYear(), Status.CUR_ACTIVE);

                //Tìm bản ACTIVE hiện tại của cùng nhóm --> nếu có cho vào bản CUR_ARCHIVED
                if (currentActive != null) {
                    currentActive.setCurriculumStatus(Status.CUR_ARCHIVED);
                    curriculumRepo.save(currentActive);
                }

                target.setCurriculumStatus(Status.CUR_ACTIVE);
                curriculumRepo.save(target);
                return ResponseBuilder.build(HttpStatus.OK, "Published successfully", target.getId());

            case "REVISE":
                //Chỉnh sửa, cập nhật dựa trên bản cũ để tạo bản mới.
                // Chỉ cho phép REVISE nếu đang là ACTIVE
                if (!Status.CUR_ACTIVE.equals(target.getCurriculumStatus())) {
                    return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Only Active curriculum can be revised", null);
                }

                //GIỮ NGUYÊN bản cũ là ACTIVE, chỉ đơn giản là CLONE ra bản DRAFT mới để sửa
                Curriculum newDraft = evolveFromExisting(target, null);

                return ResponseBuilder.build(HttpStatus.OK, "New draft created. Please update your changes.", curriculumRepo.save(newDraft).getId());

            default:
                return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Invalid action: " + action, null);
        }
    }

    private Curriculum buildNewCurriculum(CurriculumRequest request, School school) {
        // tạo mới thì draft
        return Curriculum.builder().name(CurriculumNamingUtil.generateName(request)).groupCode(CurriculumNamingUtil.generateGroupCode(request)).curriculumType(CurriculumType.valueOf(request.getCurriculumType())).methodLearning(LearningMethod.valueOf(request.getMethodLearning())).enrollmentYear(request.getEnrollmentYear()).description(request.getDescription()).subjectsJsonb(buildSubjectsJsonb(request.getSubjectOptions())).school(school).curriculumStatus(Status.CUR_DRAFT).build();
    }

    // bảng update đối vs draft
    private void applyRequestToCurriculum(Curriculum curriculum, CurriculumRequest request) {

        if (request == null) return;

        curriculum.setDescription(request.getDescription());
        curriculum.setSubjectsJsonb(buildSubjectsJsonb(request.getSubjectOptions()));
        curriculum.setMethodLearning(LearningMethod.valueOf(request.getMethodLearning()));

        // Chỉ generate lại tên curriculum từ các trường thành phần, tuyệt đối không lấy từ request.getName()
        // Không setName ở bất kỳ nơi nào khác ngoài đây và buildNewCurriculum
        boolean isIdentityChanging = curriculum.getEnrollmentYear() != request.getEnrollmentYear() || !curriculum.getCurriculumType().name().equals(request.getCurriculumType()) || !curriculum.getGroupCode().equals(CurriculumNamingUtil.generateGroupCode(request));

        if (isIdentityChanging) {
            // Chỉ khi có ý định đổi Identity mới kiểm tra DB
            boolean hasLinkedPrograms = curriculum.getId() != null && programRepo.existsByCurriculumId(curriculum.getId());
            if (!hasLinkedPrograms) {
                // Luôn generate lại tên từ các trường thành phần
                curriculum.setName(CurriculumNamingUtil.generateName(request));
                curriculum.setGroupCode(CurriculumNamingUtil.generateGroupCode(request));
                curriculum.setEnrollmentYear(request.getEnrollmentYear());
                curriculum.setCurriculumType(CurriculumType.valueOf(request.getCurriculumType()));
            }
        }
    }

    private Curriculum evolveFromExisting(Curriculum existing, CurriculumRequest request) {
        Curriculum clone = Curriculum.builder().name(existing.getName()).groupCode(existing.getGroupCode()).description(existing.getDescription()).curriculumType(existing.getCurriculumType()).methodLearning(existing.getMethodLearning()).enrollmentYear(existing.getEnrollmentYear()).subjectsJsonb(existing.getSubjectsJsonb()).school(existing.getSchool()).parent(existing).curriculumStatus(Status.CUR_DRAFT).build();

        if (request != null) {
            applyRequestToCurriculum(clone, request);
        }

        return clone;
    }

    // define cấu trúc subjectsJsonb theo format jsonb
    private List<Map<String, Object>> buildSubjectsJsonb(List<CurriculumRequest.SubjectOptionRequest> request) {
        if (request == null) return Collections.emptyList();

        return request.stream().map(opt -> {
            return Map.<String, Object>of("name", Objects.requireNonNullElse(opt.getName(), ""), "description", Objects.requireNonNullElse(opt.getDescription(), ""), "isMandatory", Boolean.TRUE.equals(opt.getIsMandatory()));
        }).collect(Collectors.toList());
    }

    @Override
    public ResponseEntity<ResponseObject> viewCurriculumList(int page, int pageSize) {

        Campus actorCampus = extractActorCampus();

        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.UNAUTHORIZED, "User session invalid or school not found", null);
        }

        autoArchiveOldCurriculums(actorCampus.getSchool().getId());

        Pageable pageable;
        try {
            pageable = PaginationUtil.buildPageRequest(page, pageSize);
        } catch (IllegalArgumentException e) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, e.getMessage(), null);
        }

        Page<Curriculum> curriculumPage = curriculumRepo.findBySchoolIdOrderByEnrollmentYearDesc(actorCampus.getSchool().getId(), pageable);

        PageResponse<Map<String, Object>> pageResponse = PaginationUtil.buildPageResponse(curriculumPage, this::buildCurriculumData);

        return ResponseBuilder.build(HttpStatus.OK, "View Curriculum list successfully", pageResponse);
    }

    private Map<String, Object> buildCurriculumData(Curriculum curriculum) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", curriculum.getId());
        data.put("name", curriculum.getName());
        data.put("subTypeName", CurriculumNamingUtil.extractSubTypeNameFromName(curriculum.getName()));
        data.put("description", curriculum.getDescription());
        data.put("curriculumType", curriculum.getCurriculumType());
        data.put("methodLearning", curriculum.getMethodLearning());
        data.put("enrollmentYear", curriculum.getEnrollmentYear());
        data.put("groupCode", curriculum.getGroupCode());
        data.put("subjects", curriculum.getSubjectsJsonb());
        data.put("status", curriculum.getCurriculumStatus().name());

        int programCount = (curriculum.getPrograms() != null) ? curriculum.getPrograms().size() : 0;
        data.put("programCount", programCount);
        data.put("canEditIdentity", programCount == 0);

        // Thêm danh sách tên Program để hiển thị Tooltip/Modal
        if (programCount > 0) {
            List<String> linkedProgramNames = curriculum.getPrograms().stream()
                    // Lấy tên Program (thường map từ Graduation Standard hoặc một field name riêng của Program)
                    .map(p -> {
                        Map<String, Object> programData = buildProgramData(p);
                        return (String) programData.get("name");
                    }).collect(Collectors.toList());
            data.put("linkedProgramNames", linkedProgramNames);

        } else {

            data.put("linkedProgramNames", Collections.emptyList());
        }
        return data;
    }

    private void autoArchiveOldCurriculums(int schoolId) {
        int currentYear = LocalDate.now().getYear();

        List<Curriculum> oldCurriculums = curriculumRepo.findAllBySchoolIdAndCurriculumStatusAndEnrollmentYearLessThan(schoolId, Status.CUR_ACTIVE, currentYear);

        if (oldCurriculums != null && !oldCurriculums.isEmpty()) {
            for (Curriculum cur : oldCurriculums) {
                cur.setCurriculumStatus(Status.CUR_ARCHIVED);
            }
            curriculumRepo.saveAll(oldCurriculums);
        }
    }

    @Override
    public ResponseEntity<ResponseObject> upsertProgram(ProgramRequest request) {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Your account is restricted", null);
        }

        Campus actorCampus = extractActorCampus();

        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "No school campus account found", null);
        }

        // actor campus co phai la primary campus ko
        if (!actorCampus.getIsPrimaryBranch()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Campus account is invalid", null);
        }

        String error = ProgramValidation.validationUpsertProgram(request, actorCampus, curriculumRepo, programRepo);

        if (error != null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, error, null);
        }

        boolean isNew = request.getProgramId() == null || request.getProgramId() <= 0;
        Program program = isNew ? new Program() : programRepo.findByIdAndCurriculum_School_Id(request.getProgramId(), actorCampus.getSchool().getId());

        if (!isNew && program == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Program not found in your school scope", null);
        }

        Curriculum curriculum = curriculumRepo.findById(request.getCurriculumId()).orElse(null);
        if (curriculum == null || !curriculum.getSchool().getId().equals(actorCampus.getSchool().getId())) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Curriculum is invalid", null);
        }

        if (curriculum.getCurriculumStatus() == Status.CUR_ARCHIVED) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Cannot use an archived curriculum. Please use the latest active version.", null);
        }

        // Đồng bộ dữ liệu (Gom chung cho cả Create/Update để tránh lặp code)
        program.setCurriculum(curriculum);
        program.setName(normalize(request.getName()));
        program.setLanguageOfInstruction(LanguageInstruction.valueOf(normalize(request.getLanguageOfInstruction())));
        program.setProgramCategory(ProgramCategory.valueOf(normalize(request.getProgramCategory())));
        program.setGraduationStandard(normalize(request.getGraduationStandard()));
        program.setTargetStudentDescription(normalize(request.getTargetStudentDescription()));
        program.setBaseTuitionFee(request.getBaseTuitionFee());
        program.setFeeUnit(FeeUnit.valueOf(request.getFeeUnit()));
        program.setStatus(Status.PRO_DRAFT);

        try {
            programRepo.save(program);
        } catch (DataIntegrityViolationException e) {
            return ResponseBuilder.build(HttpStatus.CONFLICT, "Graduation standard already exists in this curriculum", null);
        }

        return ResponseBuilder.build(isNew ? HttpStatus.CREATED : HttpStatus.OK, isNew ? "Create Program success" : "Update Program success", null);
    }

    @Override
    @Transactional
    public ResponseEntity<ResponseObject> cloneProgram(int id) {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Your account is restricted", null);
        }

        Campus actorCampus = extractActorCampus();

        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "No school campus account found", null);
        }

        // actor campus co phai la primary campus ko
        if (!actorCampus.getIsPrimaryBranch()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Campus account is invalid", null);
        }

        Program oldProgram = programRepo.findById(id).orElse(null);

        if (oldProgram == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "The original program does not exist.", null);
        }

        Program newProgram = new Program();
        newProgram.setName(oldProgram.getName() + " - Cloned (" + LocalDateTime.now().getYear() + ")");
        newProgram.setCurriculum(oldProgram.getCurriculum());
        newProgram.setLanguageOfInstruction(oldProgram.getLanguageOfInstruction());
        newProgram.setProgramCategory(oldProgram.getProgramCategory());
        newProgram.setGraduationStandard(oldProgram.getGraduationStandard());
        newProgram.setTargetStudentDescription(oldProgram.getTargetStudentDescription());

        // Giữ nguyên học phí cũ để Admin tự vào sửa sau
        newProgram.setBaseTuitionFee(oldProgram.getBaseTuitionFee());
        newProgram.setFeeUnit(oldProgram.getFeeUnit());
        newProgram.setStatus(Status.PRO_DRAFT);

        Program savedProgram = programRepo.save(newProgram);

        return ResponseBuilder.build(HttpStatus.CREATED, "A copy has been successfully created. Please update your information.", buildProgramData(savedProgram));
    }

    @Override
    @Transactional
    public ResponseEntity<ResponseObject> handleProgramAction(int id, String action) {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Your account is restricted", null);
        }

        Campus actorCampus = extractActorCampus();

        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "No school campus account found", null);
        }

        // actor campus co phai la primary campus ko
        if (!actorCampus.getIsPrimaryBranch()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Campus account is invalid", null);
        }

        Program program = programRepo.findById(id).orElse(null);

        if (program == null) return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Program not found", null);

        switch (action.toUpperCase()) {
            case "ACTIVATE":

                if (Status.PRO_ACTIVE.equals(program.getStatus())) {
                    return ResponseBuilder.build(HttpStatus.OK, "Program is already Active", null);
                }

                program.setStatus(Status.PRO_ACTIVE);
                programRepo.save(program);
                return ResponseBuilder.build(HttpStatus.OK, "Program activated successfully", null);
            case "DEACTIVATE":

                if (Status.PRO_INACTIVE.equals(program.getStatus())) {
                    return ResponseBuilder.build(HttpStatus.OK, "Program is already Inactive", null);
                }

                program.setStatus(Status.PRO_INACTIVE);

                List<CampusProgramOffering> activeOfferings = campusProgramOfferingRepo.findByProgramIdAndStatus(id, Status.OPEN);
                for (CampusProgramOffering off : activeOfferings) {
                    off.setStatus(Status.CLOSED); // Chặn người mới nộp vào
                    campusProgramOfferingRepo.save(off);
                }

                programRepo.save(program);
                return ResponseBuilder.build(HttpStatus.OK, "Program deactivated successfully", null);

            default:
                return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Invalid action", null);
        }
    }

    @Override
    public ResponseEntity<ResponseObject> viewProgramList(int page, int pageSize) {

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

        Page<Program> programs = programRepo.findByCurriculum_School_Id(actorCampus.getSchool().getId(), pageable);

        PageResponse<Map<String, Object>> pageResponse = PaginationUtil.buildPageResponse(programs, this::buildProgramData);

        return ResponseBuilder.build(HttpStatus.OK, "View program list successfully", pageResponse);
    }

    private Campus extractActorCampus() {
        Account account = AuthRequestUtil.extractAuthenticatedAccount();
        if (account == null || account.getRole() != Role.SCHOOL) {
            return null;
        }
        return account.getCampus();
    }

    private Map<String, Object> buildCampaignData(AdmissionCampaign campaign) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", campaign.getId());
        data.put("name", campaign.getName());
        data.put("description", campaign.getDescription());
        data.put("year", campaign.getYear());
        data.put("startDate", campaign.getStartDate());
        data.put("endDate", campaign.getEndDate());
        data.put("status", autoCheckAndExpireStatus(campaign));
        data.put("schoolId", campaign.getSchool().getId());
        return data;
    }

    private Map<String, Object> buildProgramData(Program program) {
        Map<String, Object> data = new HashMap<>();

        data.put("id", program.getId());
        data.put("name", program.getName());
        data.put("languageOfInstruction", program.getLanguageOfInstruction()); // Thêm cái này
        data.put("programCategory", program.getProgramCategory()); // Thêm cái này
        data.put("graduationStandard", program.getGraduationStandard());
        data.put("targetStudentDescription", program.getTargetStudentDescription());
        data.put("baseTuitionFee", program.getBaseTuitionFee());
        data.put("feeUnit", program.getFeeUnit()); // Rất quan trọng cho FE hiển thị
        data.put("status", program.getStatus());

        Curriculum curriculum = program.getCurriculum();
        Map<String, Object> curriculumData = new HashMap<>();
        curriculumData.put("id", curriculum.getId());
        curriculumData.put("name", curriculum.getName());
        curriculumData.put("type", curriculum.getCurriculumType());
        curriculumData.put("enrollmentYear", curriculum.getEnrollmentYear());
        curriculumData.put("status", curriculum.getCurriculumStatus());
        curriculumData.put("schoolId", curriculum.getSchool() != null ? curriculum.getSchool().getId() : null);
        data.put("curriculum", curriculumData);

        // --- Thông tin Thống kê & Logic (Helper cho FE) ---
        int offeringCount = (program.getCampusProgramOfferingList() != null) ? program.getCampusProgramOfferingList().size() : 0;
        data.put("offeringCount", offeringCount);

        boolean isActive = Status.PRO_ACTIVE.equals(program.getStatus());
        data.put("canEditCore", !isActive && offeringCount == 0);

        return data;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private BoardingType parseBoardingType(String value) {
        String normalized = normalize(value);
        if (normalized == null) {
            return null;
        }

        String enumKey = normalized.toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');

        try {
            return BoardingType.valueOf(enumKey);
        } catch (IllegalArgumentException ignored) {
            for (BoardingType boardingType : BoardingType.values()) {
                if (boardingType.getValue().equalsIgnoreCase(normalized)) {
                    return boardingType;
                }
            }
            return null;
        }
    }

    @Override
    public ResponseEntity<ResponseObject> viewSchoolList() {

        //uu tien cac truong isFeatured len dau, sau do moi tim den Id / rating
        List<School> schools = schoolRepo.findAllByOrderByIsFeaturedDescAverageRatingDesc();

        //Trí sửa
        Set<Integer> favouriteSchoolIds = getFavouriteSchoolIds();

        List<Map<String, Object>> schoolList = schools.stream().map(school -> {
            Map<String, Object> operationConfig = getOperationConfig(school.getId());
            return buildPublicSchoolData(school, favouriteSchoolIds, operationConfig);
        }).toList();

        return ResponseBuilder.build(HttpStatus.OK, "View school list successfully", schoolList);
    }

    @Override
    public ResponseEntity<ResponseObject> viewSchoolDetail(int schoolId) {

        School school = schoolRepo.findById(schoolId).orElse(null);

        if (school == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "School not found", null);
        }

        Set<Integer> favouriteSchoolIds = getFavouriteSchoolIds();

        Map<String, Object> operationConfig = getOperationConfig(schoolId);

        Map<String, Object> data = buildPublicSchoolData(school, favouriteSchoolIds, operationConfig);

        data.put("campusList", school.getCampusList().stream().filter(campus -> Status.ACTIVE.equals(campus.getStatus())).map(this::buildPublicCampusData).toList());

        data.put("curriculumList", school.getCurriculumList().stream().filter(curriculum -> Status.CUR_ACTIVE.equals(curriculum.getCurriculumStatus())).map(this::buildPublicCurriculumData).toList());


        return ResponseBuilder.build(HttpStatus.OK, "View school detail successfully", data);
    }

    private Set<Integer> getFavouriteSchoolIds() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Parent parent = parentRepo.findByAccount_Email(email).orElse(null);

        if (parent == null) {
            return Collections.emptySet();
        }

        return favouriteSchoolRepo.findByParentId(parent.getId()).stream().map(f -> f.getSchool().getId()).collect(Collectors.toSet());
    }

    Map<String, Object> buildPublicSchoolData(School school, Set<Integer> favouriteSchoolIds, Map<String, Object> operationConfig) {

        Map<String, Object> data = new HashMap<>();
        data.put("id", school.getId());
        data.put("name", school.getName());
        data.put("isFavourite", favouriteSchoolIds.contains(school.getId()));
        data.put("description", school.getDescription());
        data.put("totalCampus", school.getCampusList() != null ? school.getCampusList().size() : 0);
        data.put("logoUrl", school.getLogoUrl());
        data.put("websiteUrl", school.getWebsiteUrl());
        data.put("representativeName", school.getRepresentativeName());
        data.put("hotline", school.getHotline());

        String configHotline = (String) operationConfig.get("hotline");
        String configEmail = (String) operationConfig.get("emailSupport");

        data.put("hotline", configHotline);
        data.put("emailSupport", configEmail);
        data.put("averageRating", school.getAverageRating());
        data.put("foundingDate", school.getFoundingDate());
        return data;
    }

    private Map<String, Object> getOperationConfig(int schoolId) {
        return schoolConfigRepo.findBySchoolIdAndKey(schoolId, "operationSettingsData")
                .map(config -> (Map<String, Object>) config.getValue())
                .orElse(new HashMap<>());
    }

    Map<String, Object> buildPublicCampusData(Campus campus) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", campus.getId());
        data.put("name", campus.getName());
        data.put("phoneNumber", campus.getPhoneNumber());
        data.put("address", campus.getAddress());
        data.put("city", campus.getCity());
        data.put("district", campus.getDistrict());
        data.put("ward", campus.getWard());
        data.put("latitude", campus.getLatitude());
        data.put("longitude", campus.getLongitude());
        data.put("boardingType", campus.getBoardingType());
        data.put("status", campus.getStatus());
        data.put("policyDetail", campus.getPolicyDetail());
        data.put("imageJson", campus.getImageJson());
        data.put("facility", campus.getFacility());

        List<String> consultantEmails = campus.getCounsellorList().stream().map(Counsellor::getAccount).filter(acc -> acc != null).filter(acc -> Role.COUNSELLOR.equals(acc.getRole())).filter(acc -> Status.ACCOUNT_ACTIVE.equals(acc.getStatus())).map(Account::getEmail).toList();

        data.put("consultantEmails", consultantEmails);
        return data;
    }

    Map<String, Object> buildPublicCurriculumData(Curriculum curriculum) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", curriculum.getName());
        data.put("description", curriculum.getDescription());
        data.put("curriculumType", curriculum.getCurriculumType());
        data.put("methodLearning", curriculum.getMethodLearning());
        data.put("subjectsJsonb", curriculum.getSubjectsJsonb());
        data.put("enrollmentYear", curriculum.getEnrollmentYear());
        data.put("groupCode", curriculum.getGroupCode());
        data.put("curriculumStatus", curriculum.getCurriculumStatus());
        data.put("programList", buildPublicProgramDataList(curriculum.getPrograms()));
        return data;
    }

    List<Map<String, Object>> buildPublicProgramDataList(List<Program> programList) {

        if (programList == null) return Collections.emptyList();

        return programList.stream().map(program -> {

            Map<String, Object> data = new HashMap<>();
            data.put("name", program.getName());
            data.put("graduationStandard", program.getGraduationStandard());
            data.put("targetStudentDescription", program.getTargetStudentDescription());
            data.put("baseTuitionFee", program.getBaseTuitionFee());
            data.put("isActive", program.getStatus());
            data.put("campusProgramOfferingList", buildPublicCampusProgramOfferingDataList(program.getCampusProgramOfferingList()));
            return data;
        }).toList();
    }

    List<Map<String, Object>> buildPublicCampusProgramOfferingDataList(List<CampusProgramOffering> campusProgramOfferingList) {

        if (campusProgramOfferingList == null) return Collections.emptyList();

        return campusProgramOfferingList.stream().map(campusProgramOffering -> {
            Map<String, Object> data = new HashMap<>();
            data.put("learningMode", campusProgramOffering.getLearningMode());
            data.put("quota", campusProgramOffering.getQuota());
            data.put("tuitionFee", campusProgramOffering.getFinalTuitionFee());
            data.put("openDate", campusProgramOffering.getOpenDate());
            data.put("closeDate", campusProgramOffering.getCloseDate());
            data.put("status", campusProgramOffering.getStatus());
            return data;
        }).toList();
    }

    @Override
    public ResponseEntity<ResponseObject> createOpenDayEvent(CreateOpenDayEventRequest request) {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Your account is restricted", null);
        }

        Campus actorCampus = extractActorCampus();

        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "No school campus account found", null);
        }

        if (request == null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Request body is required", null);
        }

        if (request.getTitle() == null || request.getTitle().isBlank()) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Title is required", null);
        }

        String title = request.getTitle().trim();
        if (title.length() < 5 || title.length() > 255) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Title must be between 5 and 255 characters", null);
        }

        if (request.getDescription() == null || request.getDescription().isBlank()) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Description is required", null);
        }

        String description = request.getDescription().trim();
        if (description.length() < 20 || description.length() > 2000) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Description must be between 20 and 2000 characters", null);
        }

        if (request.getBannerUrl() == null || request.getBannerUrl().isBlank()) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Banner URL is required", null);
        }

        String bannerUrl = request.getBannerUrl().trim();
        if (bannerUrl.length() > 1000) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Banner URL must not exceed 1000 characters", null);
        }

        if (!(bannerUrl.startsWith("http://") || bannerUrl.startsWith("https://"))) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Banner URL must be a valid URL", null);
        }

        if (request.getEventDate() == null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Event date is required", null);
        }

        if (request.getEventDate().isBefore(LocalDate.now())) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Event date must be today or in the future", null);
        }

        if (request.getStartTime() == null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Start time is required", null);
        }

        if (request.getEndTime() == null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "End time is required", null);
        }

        if (request.getCampusId() <= 0) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Campus ID must be greater than 0", null);
        }

        if (!request.getStartTime().isBefore(request.getEndTime())) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Start time must be earlier than end time", null);
        }

        if (request.getEventDate().isEqual(LocalDate.now()) && request.getStartTime().isBefore(LocalTime.now())) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Start time must be later than current time for today's event", null);
        }

        if (Duration.between(request.getStartTime(), request.getEndTime()).toMinutes() < 30) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Event duration must be at least 30 minutes", null);
        }
        boolean isConflict = openDayEventRepo.existsByCampusIdAndEventDateAndStartTimeLessThanAndEndTimeGreaterThan(actorCampus.getId(), request.getEventDate(), request.getEndTime(), request.getStartTime());

        if (isConflict) {
            return ResponseBuilder.build(HttpStatus.CONFLICT, "Open day event time conflicts with another event in this campus", null);
        }
        OpenDayEvent openDayEvent = openDayEventRepo.save(

                OpenDayEvent.builder().title(request.getTitle()).description(request.getDescription()).bannerUrl(request.getBannerUrl()).eventDate(request.getEventDate()).startTime(request.getStartTime()).endTime(request.getEndTime()).status(Status.EVENT_UPCOMING).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).campus(actorCampus).build());
        return ResponseBuilder.build(HttpStatus.OK, "Create open day event successfully", buildOpenDayEvent(openDayEvent));
    }

    @Override
    public ResponseEntity<ResponseObject> viewOpenDayEventList(int currentPage, int pageSize) {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Your account is restricted", null);
        }

        Campus actorCampus = extractActorCampus();
        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "No school campus account found", null);
        }

        Pageable pageable;
        try {
            pageable = PaginationUtil.buildPageRequest(currentPage, pageSize);
        } catch (IllegalArgumentException e) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, e.getMessage(), null);
        }

        Page<OpenDayEvent> openDayEventPage = openDayEventRepo.findByCampusId(actorCampus.getId(), pageable);

        PageResponse<Map<String, Object>> pageResponse = PaginationUtil.buildPageResponse(openDayEventPage, this::buildOpenDayEvent);

        return ResponseBuilder.build(HttpStatus.OK, "View open day event list successfully", pageResponse);
    }

    private Map<String, Object> buildOpenDayEvent(OpenDayEvent openDayEvent) {

        Map<String, Object> data = new HashMap<>();
        data.put("title", openDayEvent.getTitle());
        data.put("description", openDayEvent.getDescription());
        data.put("bannerUrl", openDayEvent.getBannerUrl());
        data.put("eventDate", openDayEvent.getEventDate());
        data.put("startTime", openDayEvent.getStartTime());
        data.put("endTime", openDayEvent.getEndTime());

        data.put("campusName", openDayEvent.getCampus() != null ? openDayEvent.getCampus().getName() : "N/A");
        data.put("campusId", openDayEvent.getCampus() != null ? openDayEvent.getCampus().getId() : null);
        data.put("campusAddress", openDayEvent.getCampus() != null ? openDayEvent.getCampus().getAddress() : "N/A");

        data.put("status", openDayEvent.getStatus());
        data.put("createdAt", openDayEvent.getCreatedAt());
        data.put("updatedAt", openDayEvent.getUpdatedAt());

        return data;
    }

    @Override
    public ResponseEntity<ResponseObject> viewCampusScheduleTemplateListBySchool(int page, int pageSize) {

        Campus actorCampus = extractActorCampus();
        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "No school campus account found", null);
        }

        // actor campus co phai la primary campus ko
        if (!actorCampus.getIsPrimaryBranch()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Campus account is invalid", null);
        }

        Pageable pageable = PageRequest.of(page, pageSize);

        Page<CampusScheduleTemplate> templatePage = campusScheduleTemplateRepo.findAllByActiveTrue(pageable);

        PageResponse<Map<String, Object>> pageResponse = PaginationUtil.buildPageResponse(templatePage, this::buildCampusScheduleTemplateData);

        return ResponseBuilder.build(HttpStatus.OK, "", pageResponse);
    }

    private Map<String, Object> buildCampusScheduleTemplateData(CampusScheduleTemplate scheduleTemplate) {
        Map<String, Object> data = new LinkedHashMap<>();

        data.put("templateId", scheduleTemplate.getId());
        data.put("campusId", scheduleTemplate.getCampus().getId());
        data.put("campusName", scheduleTemplate.getCampus().getName());
        data.put("dayOfWeek", List.of(scheduleTemplate.getDayOfWeek()));
        data.put("startTime", scheduleTemplate.getStartTime().toString());
        data.put("endTime", scheduleTemplate.getEndTime().toString());
        data.put("sessionType", scheduleTemplate.getSessionType().name());
        data.put("active", scheduleTemplate.isActive());

        return data;
    }

    @Override
    public ResponseEntity<Resource> exportCampusList() throws IOException {

        Campus actorCampus = extractActorCampus();

        if (actorCampus == null || actorCampus.getSchool() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (!actorCampus.getIsPrimaryBranch()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Campus> campusList = campusRepo.findAll();

        Path path = Files.createTempFile("export_", ".xlsx");

        String[] headers = {"ID", "Tên Cơ Sở", "Liên Hệ (SĐT)", "Địa chỉ", "Thành phố", "Quận", "Loại Trú (Boarding)", "Loại Nhánh Chính", "Trạng Thái Campus", "Trạng Thái Trường"};

        ExcelUtil.exportToExcel(path, "Campuses", headers, campusList, (campus, row) -> {
            row.createCell(0).setCellValue(campus.getId());
            row.createCell(1).setCellValue(campus.getName());
            row.createCell(2).setCellValue(campus.getPhoneNumber());
            row.createCell(3).setCellValue(campus.getAddress());
            row.createCell(4).setCellValue(campus.getCity());
            row.createCell(5).setCellValue(campus.getDistrict());
            row.createCell(6).setCellValue(campus.getBoardingType() != null ? campus.getBoardingType().name() : "");
            row.createCell(7).setCellValue(Boolean.TRUE.equals(campus.getIsPrimaryBranch()) ? "Chính" : "Phụ");
            row.createCell(8).setCellValue(campus.getStatus() != null ? campus.getStatus().name() : "");
            if (campus.getSchool() != null) {
                row.createCell(11).setCellValue(SchoolUtil.checkSchoolStatus(campus.getSchool()));
            } else {
                row.createCell(11).setCellValue("N/A");
            }
        });

        return buildFileResponse(path, "Danh_Sach_Co_So.xlsx");
    }

    @Override
    public ResponseEntity<Resource> exportAdmissionCampaign(int year) throws IOException {

        Campus actorCampus = extractActorCampus();

        if (actorCampus == null || actorCampus.getSchool() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (!actorCampus.getIsPrimaryBranch()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<AdmissionCampaign> campaigns;
        if (year > 0) {
            campaigns = admissionCampaignRepo.findBySchoolIdAndYear(actorCampus.getSchool().getId(), year);
        } else {
            campaigns = admissionCampaignRepo.findBySchoolIdOrderByYearDesc(actorCampus.getSchool().getId());
        }

        Path path = Files.createTempFile("export_campaigns_", ".xlsx");

        String[] headers = {"ID", "Tên Chiến Dịch", "Năm Học", "Miêu tả", "Ngày Bắt Đầu", "Ngày Kết Thúc", "Trạng Thái"};

        ExcelUtil.exportToExcel(path, "AdmissionCampaigns", headers, campaigns, (campaign, row) -> {
            row.createCell(0).setCellValue(campaign.getId());
            row.createCell(1).setCellValue(campaign.getName());
            row.createCell(2).setCellValue(campaign.getYear());
            row.createCell(3).setCellValue(campaign.getDescription());
            row.createCell(4).setCellValue(campaign.getStartDate() != null ? campaign.getStartDate().toString() : "");
            row.createCell(5).setCellValue(campaign.getEndDate() != null ? campaign.getEndDate().toString() : "");
            row.createCell(6).setCellValue(autoCheckAndExpireStatus(campaign).name());
        });

        String fileName = year > 0 ? "Chien_Dich_Tuyen_Sinh_" + year + ".xlsx" : "Danh_Sach_Chien_Dich_Tuyen_Sinh.xlsx";
        return buildFileResponse(path, fileName);
    }

    @Override
    public ResponseEntity<ResponseObject> createSubscription(CreateSubscriptionRequest request, HttpServletRequest httpRequest) {

        // step 1 : xác thực school - campus chính
        Campus actorCampus = extractActorCampus();

        if (actorCampus == null) return ResponseBuilder.build(HttpStatus.NOT_FOUND, "School account not found", null);

        if (!actorCampus.getIsPrimaryBranch()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Only primary campus can add new campus", null);
        }

        School school = actorCampus.getSchool();

        if (request == null || request.getPackageId() <= 0) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Package id is required", null);
        }

        // step 2: lấy thông tin của gói cước
        Subscription subscription = subscriptionRepo.findById(request.getPackageId()).orElseThrow(() -> new RuntimeException("Service package not found"));

        if (subscription.getPrice() == null || subscription.getPrice() <= 0) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Package price must be greater than 0", null);
        }

        //ktra upgrade vs renew
        List<SchoolSubscription> currentActiveSub = schoolSubscriptionRepo.findBySchoolIdAndIsSelected(school.getId(), true);

        LocalDate calculatedStartDate = LocalDate.now();
        String orderNote = "Payment package " + normalize(subscription.getName());

        if (!currentActiveSub.isEmpty()) {
            // Lấy gói có ngày kết thúc xa nhất trong đám đang active để tính nối đuôi
            SchoolSubscription current = currentActiveSub.stream().max(Comparator.comparing(SchoolSubscription::getEndDate)).get();

            if (current.getSubscription().getId().equals(request.getPackageId())) {
                // Nếu GIA HẠN (Renew) - Cùng loại gói
                if (current.getEndDate().isAfter(LocalDate.now())) {
                    calculatedStartDate = current.getEndDate().plusDays(1);
                    orderNote = "Renew package " + normalize(subscription.getName()) + " from " + calculatedStartDate;
                }
            } else {
                // Nếu NÂNG CẤP (Upgrade) - Khác loại gói
                orderNote = "Upgrade package " + normalize(subscription.getName());
            }
        }

        orderNote = sanitizeOrderInfo(orderNote);

        // step 3 : tạo SchoolSubscription (trạng thái chờ - isSelected = false)
        // giúp định danh loại chuỗi này là License ==> đóng vai trò là Số báo danh cho gói đăng kí đó
        String licenseKey = "LIC-" + VNPayConfig.getRandomNumber(8).toUpperCase();
        SchoolSubscription schoolSubscription = SchoolSubscription.builder()
                .school(school)
                .subscription(subscription)
                .startDate(calculatedStartDate)
                .endDate(calculatedStartDate.plusDays(subscription.getDurationDays()))
                .isSelected(false) // chưa kích hoạt cho đến khi thanh toán xong
                .licenseKey(licenseKey)
                .build();

        schoolSubscription = schoolSubscriptionRepo.save(schoolSubscription);

        // step 4 : cấu hinh vnpay
        String vnp_TxnRef = VNPayConfig.getRandomNumber(8); // mã đơn hàng
        long amount = BigDecimal.valueOf(subscription.getPrice()).multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).longValue();
        if (amount <= 0) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Invalid payment amount", null);
        }

        Map<String, String> vnp_Params = new TreeMap<>();
        vnp_Params.put("vnp_Version", "2.1.1");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", VNPayConfig.vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", orderNote);
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", VNPayConfig.vnp_ReturnUrl); // URL FE / BE nhận kết quả
        vnp_Params.put("vnp_IpAddr", VNPayConfig.getIpAddress(httpRequest));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        LocalDateTime createDate = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        LocalDateTime expireDate = createDate.plusMinutes(15);
        vnp_Params.put("vnp_CreateDate", formatter.format(createDate));
        vnp_Params.put("vnp_ExpireDate", formatter.format(expireDate));

        String queryUrl = buildVnpQueryString(vnp_Params);
        String hashData = buildVnpHashData(vnp_Params);
        String vnp_SecureHash = VNPayConfig.hmacSHA512(VNPayConfig.vnp_HashSecret, hashData);
        String paymentUrl = VNPayConfig.vnp_PayUrl + "?" + queryUrl + "&vnp_SecureHash=" + vnp_SecureHash;

        log.info("VNPay request prepared: tmnCode={}, returnUrl={}, txnRef={}, amount={}, version={}",
                VNPayConfig.vnp_TmnCode, VNPayConfig.vnp_ReturnUrl, vnp_TxnRef, amount, vnp_Params.get("vnp_Version"));
        log.debug("VNPay hashData={}", hashData);
        log.debug("VNPay secureHash={}", vnp_SecureHash);

        paymentTransactionRepo.save(PaymentTransaction.builder().school(school).schoolSubscription(schoolSubscription).vnpTxnRef(vnp_TxnRef).vnpAmount(amount).vnpOrderInfo(orderNote).status(Status.PAYMENT_PENDING).createdAt(LocalDateTime.now()).ipAddress(VNPayConfig.getIpAddress(httpRequest)).build());

        return ResponseBuilder.build(HttpStatus.OK, "Payment URL generated", paymentUrl);
    }

    private String sanitizeOrderInfo(String rawOrderInfo) {
        if (rawOrderInfo == null || rawOrderInfo.isBlank()) {
            return "Payment package";
        }

        String normalizedText = rawOrderInfo.trim().replaceAll("\\s+", " ");
        // VNPay accepts text order info; keep a conservative safe character set.
        normalizedText = normalizedText.replaceAll("[^a-zA-Z0-9 _.,:-]", "");
        if (normalizedText.length() > 255) {
            normalizedText = normalizedText.substring(0, 255);
        }
        return normalizedText;
    }

    private String buildVnpQueryString(Map<String, String> params) {
        StringBuilder query = new StringBuilder();
        boolean first = true;

        for (Map.Entry<String, String> entry : params.entrySet()) {
            String fieldName = entry.getKey();
            String fieldValue = entry.getValue();

            if (fieldValue == null || fieldValue.isBlank()) {
                continue;
            }

            if (!first) {
                query.append('&');
            }

            query.append(urlEncode(fieldName));
            query.append('=');
            query.append(urlEncode(fieldValue));
            first = false;
        }

        return query.toString();
    }

    private String buildVnpHashData(Map<String, String> params) {
        StringBuilder hashData = new StringBuilder();
        boolean first = true;

        for (Map.Entry<String, String> entry : params.entrySet()) {
            String fieldName = entry.getKey();
            String fieldValue = entry.getValue();

            if (fieldValue == null || fieldValue.isBlank()) {
                continue;
            }

            if (!first) {
                hashData.append('&');
            }

            hashData.append(fieldName).append('=').append(urlEncode(fieldValue));
            first = false;
        }

        return hashData.toString();
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.US_ASCII);
    }

    private ResponseEntity<Resource> buildFileResponse(Path path, String fileName) throws IOException {
        Resource resource = new UrlResource(path.toUri());
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"").contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")).body(resource);
    }

    @Override
    public ResponseEntity<ResponseObject> searchNearby(Double lat, Double lng, Double radius) {
        //Hàm này giúp phụ huynh "lọc" ngay lập tức những trường ở quá xa (ngoài bán kính 5-10km).
        // Nó chuyển từ việc hiển thị tất cả sang hiển thị những gì thuộc về bạn.
        List<Campus> campuses = campusRepo.findNearbyCampuses(lat, lng, radius);

        List<Map<String, Object>> data = campuses.stream().map(campus -> {
            Map<String, Object> map = buildPublicCampusData(campus);

            double dist = calculateDistance(lat, lng, campus.getLatitude(), campus.getLongitude());
            map.put("distance", Math.round(dist * 100.0) / 100.0);

            return map;
        }).toList();

        return ResponseBuilder.build(HttpStatus.OK, "Search successfully", data);
    }

    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        //Công thức Haversine
        if (lat1 == 0 || lng1 == 0 || lat2 == 0 || lng2 == 0) return 0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return 6371 * c;
    }

    @Override
    public ResponseEntity<ResponseObject> viewCurrentSubscription() {

        Campus actorCampus = extractActorCampus();

        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "School account not found", null);
        }

        if (!actorCampus.getIsPrimaryBranch()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Only primary campus can add new campus", null);
        }

        School school = actorCampus.getSchool();

        Optional<SchoolSubscription> activeSubOpt = schoolSubscriptionRepo
                .findBySchoolIdAndIsSelected(school.getId(), true)
                .stream()
                .findFirst(); // tại 1 thời điểm chỉ có 1 gói đc active

        if (activeSubOpt.isEmpty()) {
            return ResponseBuilder.build(HttpStatus.OK, "No active subscription found", null);
        }

        Map<String, Object> data = buildCurrentSubscription(activeSubOpt.get());

        return ResponseBuilder.build(HttpStatus.OK, "Fetched current subscription status", data);
    }

    private Map<String, Object> buildCurrentSubscription(SchoolSubscription schoolSub) {
        // tính số ngày còn lại
        long daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), schoolSub.getEndDate());
        boolean isExpired = LocalDate.now().isAfter(schoolSub.getEndDate());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("packageName", schoolSub.getSubscription().getName());
        data.put("licenseKey", schoolSub.getLicenseKey());
        data.put("startDate", schoolSub.getStartDate());
        data.put("endDate", schoolSub.getEndDate());
        data.put("dasRemaining", Math.max(0, daysRemaining));
        data.put("isExpired", isExpired);
        data.put("statusMessage", isExpired ? "Expired" : "Active (Remaining " + daysRemaining + " days)");
        data.put("suggestion", isExpired
                ? "Your package has expired. Please purchase new package to continue the service."
                : "If you purchase the same package '" + schoolSub.getSubscription().getName() + "', time will be accumulated continuously from day to day" + schoolSub.getEndDate() + ".");
        return data;
    }
}