package com.sp26se041.edubridgehcm.services.implementors;

import com.sp26se041.edubridgehcm.enums.BoardingType;
import com.sp26se041.edubridgehcm.enums.Role;
import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.models.Account;
import com.sp26se041.edubridgehcm.models.AdmissionCampaign;
import com.sp26se041.edubridgehcm.models.Campus;
import com.sp26se041.edubridgehcm.models.CampusProgramOffering;
import com.sp26se041.edubridgehcm.models.Counsellor;
import com.sp26se041.edubridgehcm.models.OpenDayEvent;
import com.sp26se041.edubridgehcm.models.Program;
import com.sp26se041.edubridgehcm.repositories.AccountRepo;
import com.sp26se041.edubridgehcm.repositories.AdmissionCampaignRepo;
import com.sp26se041.edubridgehcm.repositories.CampusProgramOfferingRepo;
import com.sp26se041.edubridgehcm.repositories.CampusRepo;
import com.sp26se041.edubridgehcm.repositories.CounsellorRepo;
import com.sp26se041.edubridgehcm.repositories.OpenDayEventRepo;
import com.sp26se041.edubridgehcm.repositories.ProgramRepo;
import com.sp26se041.edubridgehcm.requests.CreateAccountCounsellorRequest;
import com.sp26se041.edubridgehcm.requests.CreateAdmissionCampaignTemplateRequest;
import com.sp26se041.edubridgehcm.requests.CreateCampusProgramOfferingRequest;
import com.sp26se041.edubridgehcm.requests.CreateCampusRequest;
import com.sp26se041.edubridgehcm.requests.CreateOpenDayEventRequest;
import com.sp26se041.edubridgehcm.requests.UpdateAdmissionCampaignTemplateRequest;
import com.sp26se041.edubridgehcm.responses.PageResponse;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.SchoolService;
import com.sp26se041.edubridgehcm.utils.AccountRestrictionUtil;
import com.sp26se041.edubridgehcm.utils.AuthRequestUtil;
import com.sp26se041.edubridgehcm.utils.PaginationUtil;
import com.sp26se041.edubridgehcm.utils.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SchoolServiceImpl implements SchoolService {

    private final CampusRepo campusRepo;

    private final AdmissionCampaignRepo admissionCampaignRepo;

    private final CampusProgramOfferingRepo campusProgramOfferingRepo;

    private final ProgramRepo programRepo;

    private final AccountRepo accountRepo;

    private final CounsellorRepo counsellorRepo;
    private final OpenDayEventRepo openDayEventRepo;

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

        String error = validateCreateCampus(request);
        if (error != null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, error, null);
        }

        BoardingType boardingType = parseBoardingType(request.getBoardingType());

        if (boardingType == null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Boarding type is invalid. Accepted values: NONE, FULL_BOARDING, SEMI_BOARDING, BOTH", null);
        }

        Account acc = accountRepo.save(Account.builder()
                .email(normalize(request.getEmail()))
                .role(Role.SCHOOL)
                .status(Status.ACCOUNT_ACTIVE)
                .registerDate(LocalDate.now())
                .firstLogin(false)
                .isRestricted(false)
                .build());

        Campus campus = campusRepo.save(Campus.builder()
                .school(actorCampus.getSchool())
                .account(acc)
                .name(normalize(request.getName()))
                .address(normalize(request.getAddress()))
                .phoneNumber(normalize(request.getPhone()))
                .city(normalize(request.getCity()))
                .district(normalize(request.getDistrict()))
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .boardingType(boardingType)
                .status(Status.VERIFIED)
                .isPrimaryBranch(false)
                .build());

        Map<String, Object> data = new HashMap<>();
        data.put("campus", buildCampusData(campus));
        data.put("account", buildAccountData(acc));

        return ResponseBuilder.build(HttpStatus.OK, "Create campus successfully", data);
    }

    private String validateCreateCampus(CreateCampusRequest request) {
        if (request == null) {
            return "Request is required";
        }

        if (normalize(request.getEmail()) == null) {
            return "Email is required";
        }

        if (normalize(request.getEmail()).length() > 100) {
            return "Email exceeds 100 characters";
        }

        if (accountRepo.findByEmail(normalize(request.getEmail())).isPresent()) {
            return "Email is already in use";
        }

        if (normalize(request.getName()) == null) {
            return "Name is required";
        }

        if (normalize(request.getName()).length() > 50) {
            return "Name exceeds 50 characters";
        }

        if (normalize(request.getAddress()) == null) {
            return "Address is required";
        }

        if (normalize(request.getAddress()).length() > 250) {
            return "Address exceeds 250 characters";
        }

        if (normalize(request.getPhone()) == null) {
            return "Phone is required";
        }

        if (!normalize(request.getPhone()).matches("^(09|08|07|03)\\d{8}$")) {
            return "Phone must start with 09, 08, 07, or 03 and contain 10 digits";
        }

        if (normalize(request.getCity()) == null) {
            return "City is required";
        }

        if (normalize(request.getDistrict()) == null) {
            return "District is required";
        }

        if (request.getLatitude() == null || request.getLongitude() == null) {
            return "Latitude and longitude are required";
        }

        if (request.getLatitude() < -90 || request.getLatitude() > 90) {
            return "Latitude must be in range [-90, 90]";
        }

        if (request.getLongitude() < -180 || request.getLongitude() > 180) {
            return "Longitude must be in range [-180, 180]";
        }

        if (parseBoardingType(request.getBoardingType()) == null) {
            return "Boarding type is invalid. Accepted values: NONE, FULL_BOARDING, SEMI_BOARDING, BOTH";
        }

        return null;
    }

    @Override
    public ResponseEntity<ResponseObject> viewCampusList() {
        Campus actorCampus = extractActorCampus();

        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "No school campus account found", null);
        }

        List<Campus> campusList = actorCampus.getIsPrimaryBranch()
                ? campusRepo.findBySchoolId(actorCampus.getSchool().getId())
                : List.of(actorCampus);

        List<Map<String, Object>> data = campusList.stream()
                .map(this::buildCampusData)
                .collect(Collectors.toList());

        return ResponseBuilder.build(HttpStatus.OK, "View campus list successfully", data);
    }

    private Map<String, Object> buildCampusData(Campus campus) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", campus.getId());
        data.put("name", campus.getName());
        data.put("address", campus.getAddress());
        data.put("city", campus.getCity());
        data.put("district", campus.getDistrict());
        data.put("latitude", campus.getLatitude());
        data.put("longitude", campus.getLongitude());
        data.put("boardingType", campus.getBoardingType().name());
        data.put("boardingTypeLabel", campus.getBoardingType().getValue());
        data.put("phoneNumber", campus.getPhoneNumber());
        data.put("status", campus.getStatus());
        data.put("isPrimaryBranch", campus.getIsPrimaryBranch());
        data.put("schoolId", campus.getSchool().getId());

        Account acc = campus.getAccount();
        data.put("account", buildAccountData(acc));
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

        String error = validationCreateAdmissionCampaignTemplate(request, actorCampus);

        if (!error.isEmpty()) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, error, null);
        }

        AdmissionCampaign admissionCampaign = AdmissionCampaign.builder()
                .school(actorCampus.getSchool())
                .name(normalize(request.getName()))
                .description(normalize(request.getDescription()))
                .year(request.getYear())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(Status.OPEN)
                .build();
        admissionCampaignRepo.save(admissionCampaign);

        return ResponseBuilder.build(HttpStatus.CREATED, "Create campaign template successfully", null);
    }

    private String validationCreateAdmissionCampaignTemplate(CreateAdmissionCampaignTemplateRequest request, Campus actorCampus) {

        if (request == null) {
            return "Request is required";
        }

        if (normalize(request.getName()) == null) {
            return "Name is required";
        }

        if (normalize(request.getName()).length() > 100) {
            return "Name is too long. Maximum length is 100 characters";
        }

        if (normalize(request.getDescription()) == null) {
            return "Description is required";
        }

        if (request.getYear() <= 0) {
            return "Year is required";
        }

        // 2. Kiểm tra Năm (Year)
        if (request.getYear() < LocalDate.now().getYear()) {
            return "Cannot create a campaign for a past year";
        }

        if (admissionCampaignRepo.existsBySchoolIdAndYear(actorCampus.getSchool().getId(), request.getYear())) {
            return "A campaign template for the year already exists";
        }

        // 3. Kiểm tra Ngày tháng (Dates)
        if (request.getStartDate() == null || request.getEndDate() == null) {
            return "Start date and end date are required";
        }

        // Đồng bộ Năm và Ngày (Bạn đã làm rất tốt bước này)
        if (request.getStartDate().getYear() != request.getYear() ||
                request.getEndDate().getYear() != request.getYear()) {
            return "Start date and end date must be within the year " + request.getYear();
        }

        // Check quá khứ cho StartDate (cho phép lùi 1 ngày)
        if (request.getStartDate().isBefore(LocalDate.now().minusDays(1))) {
            return "Start date cannot be in the past";
        }

        // Check quá khứ cho EndDate
        if (request.getEndDate().isBefore(LocalDate.now())) {
            return "End date must be in the future";
        }

        // Check mối quan hệ End - Start (Nên dùng !isAfter để bắt buộc khác ngày)
        if (!request.getEndDate().isAfter(request.getStartDate())) {
            return "End date must be after start date";
        }

        return "";
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

        //Chỉ cho sửa khi trạng thái là PAUSED ==> đang sửa sao cho update
        //CLOSED: Nghĩa là chiến dịch đã kết thúc hẳn, dữ liệu đã được chốt để làm báo cáo. Nếu cho phép sửa ở trạng thái này sẽ làm mất tính toàn vẹn của lịch sử dữ liệu.
        //PAUSE: Nghĩa là "Tạm nghỉ để bảo trì/chỉnh sửa". Đây chính là trạng thái sinh ra để dành cho việc thay đổi cấu hình mà không làm ảnh hưởng đến tiến trình chung.
        if (!admissionCampaign.getStatus().equals(Status.PAUSED)) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "The campaign is currently active (OPEN). Please switch to PAUSED before updating the information.", null);
        }

        String error = validationUpdateAdmissionCampaignTemplate(request);

        if (!error.isEmpty()) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, error, null);
        }

        admissionCampaign.setName(normalize(request.getName()));
        admissionCampaign.setDescription(normalize(request.getDescription()));
        admissionCampaign.setStartDate(request.getStartDate());
        admissionCampaign.setEndDate(request.getEndDate());
        admissionCampaignRepo.save(admissionCampaign);

        return ResponseBuilder.build(HttpStatus.OK, "Update campaign template successfully", null);
    }

    private String validationUpdateAdmissionCampaignTemplate(UpdateAdmissionCampaignTemplateRequest request) {

        if (request == null) {
            return "Request is required";
        }

        if (normalize(request.getName()) == null) {
            return "Name is required";
        }

        if (normalize(request.getName()).length() > 100) {
            return "Name is too long. Maximum length is 100 characters";
        }

        String description = normalize(request.getDescription());
        if (description == null) {
            return "Description is required";
        }

        // 2. Kiểm tra Null cho ngày tháng (BẮT BUỘC để tránh crash 500)
        if (request.getStartDate() == null || request.getEndDate() == null) {
            return "Start date and end date are required";
        }

        // 3. Logic thời gian
        // StartDate cho phép lùi 1 ngày
        if (request.getStartDate().isBefore(LocalDate.now().minusDays(1))) {
            return "Start date cannot be in the past";
        }

        // EndDate phải từ hôm nay trở đi
        if (request.getEndDate().isBefore(LocalDate.now())) {
            return "End date must be a future date";
        }

        // End phải sau Start (Dùng !isAfter để đảm bảo không trùng ngày)
        if (!request.getEndDate().isAfter(request.getStartDate())) {
            return "End date must be after start date";
        }

        return "";
    }

    @Override
    @Transactional
    public ResponseEntity<ResponseObject> changeAdmissionCampaignStatus(Integer id, Status targetStatus) {

        //kiểm tra Actor & Quyền (Tương tự Create/Update)
        Campus actorCampus = extractActorCampus();
        if (actorCampus == null || !actorCampus.getIsPrimaryBranch()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Only primary campus can change status", null);
        }

        //Tìm Campaign
        AdmissionCampaign campaign = admissionCampaignRepo.findById(id).orElse(null);
        if (campaign == null) return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Campaign not found", null);

        if (campaign.getStatus().equals(targetStatus)) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Campaign is already in status " + targetStatus.name(), null);
        }

        if (campaign.getStatus().equals(Status.CLOSED)) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Cannot change status of a closed campaign", null);
        }

        if (campaign.getStatus().equals(Status.EXPIRED) && targetStatus.equals(Status.OPEN)) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Cannot open an expired campaign. Please switch to PAUSED to update the end date first.", null);
        }

        if (targetStatus.equals(Status.OPEN)) {
            if (LocalDate.now().isAfter(campaign.getEndDate())) {
                return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "The end date is in the past. Please update the campaign duration before opening.", null);
            }
        }

        campaign.setStatus(targetStatus);
        admissionCampaignRepo.save(campaign);

        return ResponseBuilder.build(HttpStatus.OK, "Status updated to " + targetStatus + " successfully", null);
    }

    @Override
    public ResponseEntity<ResponseObject> viewAdmissionCampaignTemplate(int year) {

        Campus actorCampus = extractActorCampus();

        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "No school campus account found", null);
        }

        int schoolId = actorCampus.getSchool().getId();

        if (year > 0) {
            AdmissionCampaign campaign = admissionCampaignRepo
                    .findFirstBySchoolIdAndYearOrderByIdDesc(schoolId, year)
                    .orElse(null);

            if (campaign == null) {
                return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Campaign template not found", null);
            }

            return ResponseBuilder.build(HttpStatus.OK, "View campaign template successfully", buildCampaignData(campaign));
        }

        List<AdmissionCampaign> campaignList = admissionCampaignRepo.findBySchoolIdOrderByYearDesc(schoolId);

        List<Map<String, Object>> data = campaignList.stream()
                .map(this::buildCampaignData)
                .toList();

        return ResponseBuilder.build(HttpStatus.OK, "View campaign template list successfully", data);
    }

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

        if (request == null || request.getAdmissionCampaignId() == null || request.getProgramId() == null || request.getLearningMode() == null || request.getQuota() <= 0) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Campaign, program, learning mode and quota are required", null);
        }

        AdmissionCampaign campaign = admissionCampaignRepo.findById(request.getAdmissionCampaignId()).orElse(null);
        if (campaign == null || !campaign.getSchool().getId().equals(actorCampus.getSchool().getId())) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Campaign is out of your school scope", null);
        }

        Program program = programRepo.findById(request.getProgramId()).orElse(null);
        if (program == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Program not found", null);
        }

        Campus targetCampus = resolveTargetCampus(actorCampus, request.getCampusId());
        if (targetCampus == null) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Campus is out of your scope", null);
        }

        BigDecimal tuitionFee = request.getTuitionFee();

        if (tuitionFee == null || tuitionFee.signum() < 0) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Tuition fee must be >= 0", null);
        }

        LocalDate openDate = request.getOpenDate();
        LocalDate closeDate = request.getCloseDate();
        if (openDate != null && closeDate != null && closeDate.isBefore(openDate)) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Close date must be after or equal to open date", null);
        }

        Status applicationStatus = parseApplicationStatus(request.getApplicationStatus());
        if (applicationStatus == null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Application status must be OPEN, PAUSED, FULL, or CLOSED", null);
        }

        CampusProgramOffering offering = campusProgramOfferingRepo.save(CampusProgramOffering.builder()
                .campus(targetCampus)
                .admissionCampaign(campaign)
                .program(program)
                .quota(request.getQuota())
                .remainingQuota(request.getQuota())
                .learningMode(request.getLearningMode())
                .priceAdjustmentPercentage(0)
                .tuitionFee(tuitionFee)
                .applicationStatus(applicationStatus)
                .openDate(openDate)
                .closeDate(closeDate)
                .status(Status.OPEN)
                .build());

        return ResponseBuilder.build(HttpStatus.OK, "Create campus offering successfully", buildOfferingData(offering));
    }

    @Override
    public ResponseEntity<ResponseObject> viewCampusProgramOfferingList(int campusId) {

        Campus actorCampus = extractActorCampus();

        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "No school campus account found", null);
        }

        Integer requestedCampusId = campusId <= 0 ? null : campusId;

        List<CampusProgramOffering> offeringList;
        if (actorCampus.getIsPrimaryBranch()) {
            if (requestedCampusId == null) {
                offeringList = campusProgramOfferingRepo.findByAdmissionCampaignSchoolId(actorCampus.getSchool().getId());
            } else {
                Campus campus = campusRepo.findByIdAndSchoolId(requestedCampusId, actorCampus.getSchool().getId()).orElse(null);
                if (campus == null) {
                    return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Campus is out of your scope", null);
                }
                offeringList = campusProgramOfferingRepo.findByCampusId(campus.getId());
            }
        } else {
            if (requestedCampusId != null && !requestedCampusId.equals(actorCampus.getId())) {
                return ResponseBuilder.build(HttpStatus.FORBIDDEN, "You can only view your campus data", null);
            }
            offeringList = campusProgramOfferingRepo.findByCampusId(actorCampus.getId());
        }

        List<Map<String, Object>> payload = offeringList.stream()
                .map(this::buildOfferingData)
                .collect(Collectors.toList());

        return ResponseBuilder.build(HttpStatus.OK, "View campus offering list successfully", payload);
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

        if (request.getEventDate().isEqual(LocalDate.now())
                && request.getStartTime().isBefore(LocalTime.now())) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Start time must be later than current time for today's event", null);
        }

        if (Duration.between(request.getStartTime(), request.getEndTime()).toMinutes() < 30) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Event duration must be at least 30 minutes", null);
        }

        OpenDayEvent openDayEvent =  openDayEventRepo.save(
                OpenDayEvent.builder()
                        .title(request.getTitle())
                        .description(request.getDescription())
                        .bannerUrl(request.getBannerUrl())
                        .eventDate(request.getEventDate())
                        .startTime(request.getStartTime())
                        .endTime(request.getEndTime())
                        .status(Status.EVENT_UPCOMING)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .campus(actorCampus)
                        .build()
        );

        return ResponseBuilder.build(HttpStatus.OK, "Create open day event successfully", buildOpenDayEvent(openDayEvent));
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

        data.put("status",  openDayEvent.getStatus());
        data.put("createdAt", openDayEvent.getCreatedAt());
        data.put("updatedAt", openDayEvent.getUpdatedAt());

        return data;
    }

    private Campus extractActorCampus() {
        Account account = AuthRequestUtil.extractAuthenticatedAccount();
        if (account == null || account.getRole() != Role.SCHOOL) {
            return null;
        }
        return account.getCampus();
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

    private Map<String, Object> buildCampaignData(AdmissionCampaign campaign) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", campaign.getId());
        data.put("name", campaign.getName());
        data.put("description", campaign.getDescription());
        data.put("year", campaign.getYear());
        data.put("startDate", campaign.getStartDate());
        data.put("endDate", campaign.getEndDate());
        data.put("status", campaign.getStatus());
        data.put("schoolId", campaign.getSchool().getId());
        return data;
    }

    private Map<String, Object> buildOfferingData(CampusProgramOffering offering) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", offering.getId());
        data.put("campusId", offering.getCampus().getId());
        data.put("campusName", offering.getCampus().getName());
        data.put("campaignId", offering.getAdmissionCampaign().getId());
        data.put("campaignName", offering.getAdmissionCampaign().getName());
        data.put("programId", offering.getProgram().getId());
        data.put("quota", offering.getQuota());
        data.put("remainingQuota", offering.getRemainingQuota());
        data.put("learningMode", offering.getLearningMode());
        data.put("tuitionFee", offering.getTuitionFee());
        data.put("applicationStatus", offering.getApplicationStatus());
        data.put("openDate", offering.getOpenDate());
        data.put("closeDate", offering.getCloseDate());
        data.put("status", offering.getStatus());
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

        String enumKey = normalized.toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');

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

    private Status parseApplicationStatus(String value) {
        String normalized = normalize(value);
        if (normalized == null) {
            return Status.OPEN;
        }

        String enumKey = normalized.toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');

        Status parsed;
        try {
            parsed = Status.valueOf(enumKey);
        } catch (IllegalArgumentException ignored) {
            return null;
        }

        if (parsed != Status.OPEN && parsed != Status.PAUSED
                && parsed != Status.FULL && parsed != Status.CLOSED) {
            return null;
        }

        return parsed;
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

        String validationError = validateCreateCounsellor(request);

        if (validationError != null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, validationError, null);
        }

        Account account = accountRepo.save(Account.builder()
                .email(normalize(request.getEmail()))
                .role(Role.COUNSELLOR)
                .status(Status.ACCOUNT_ACTIVE)
                .registerDate(LocalDate.now())
                .firstLogin(true)
                .build());

        Counsellor counsellor = counsellorRepo.save(Counsellor.builder()
                .account(account)
                .campus(actorCampus)
                .employeeCode(UUID.randomUUID())
                .build());

        return ResponseBuilder.build(HttpStatus.OK, "Create counsellor successfully", buildCounsellorData(counsellor));
    }

    private String validateCreateCounsellor(CreateAccountCounsellorRequest request) {

        String email = normalize(request.getEmail());
        if (email == null) {
            return "Email is required";
        }

        if (email.length() > 100) {
            return "Email exceeds 100 characters";
        }

        if (!isValidEmail(email)) {
            return "Email is invalid";
        }

        if (accountRepo.findByEmail(email).isPresent()) {
            return "Email is already in use";
        }

        return null;
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
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

        Page<Counsellor> counsellorPage =
                counsellorRepo.findByCampusId(actorCampus.getId(), pageable);

        PageResponse<Map<String, Object>> pageResponse =
                PaginationUtil.buildPageResponse(counsellorPage, this::buildCounsellorData);

        return ResponseBuilder.build(HttpStatus.OK, "View counsellor list successfully", pageResponse);
    }

    private Map<String, Object> buildCounsellorData(Counsellor counsellor) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", counsellor.getId());
        data.put("name", counsellor.getName());
        data.put("employeeCode", counsellor.getEmployeeCode());
        data.put("campusId", counsellor.getCampus().getId());
        data.put("campusName", counsellor.getCampus().getName());
        data.put("account", buildAccountData(counsellor.getAccount()));
        return data;
    }
}
