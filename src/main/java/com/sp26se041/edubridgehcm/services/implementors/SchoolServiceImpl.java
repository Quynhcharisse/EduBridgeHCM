
package com.sp26se041.edubridgehcm.services.implementors;

import com.sp26se041.edubridgehcm.enums.*;
import com.sp26se041.edubridgehcm.models.*;
import com.sp26se041.edubridgehcm.repositories.*;
import com.sp26se041.edubridgehcm.requests.*;
import com.sp26se041.edubridgehcm.responses.PageResponse;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.SchoolService;
import com.sp26se041.edubridgehcm.utils.*;
import io.hypersistence.utils.common.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
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

    private final CurriculumRepo curriculumRepo;

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
        if (error != null && !error.isBlank()) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, error, null);
        }

        BoardingType boardingType = parseBoardingType(request.getBoardingType());

        if (boardingType == null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Boarding type is invalid. Accepted values: NONE, FULL_BOARDING, SEMI_BOARDING, BOTH", null);
        }

        Account acc = accountRepo.save(Account.builder().email(normalize(request.getEmail())).role(Role.SCHOOL).status(Status.ACCOUNT_ACTIVE).registerDate(LocalDate.now()).firstLogin(false).isRestricted(false).build());

        Campus campus = campusRepo.save(Campus.builder().school(actorCampus.getSchool()).account(acc).name(normalize(request.getName())).address(normalize(request.getAddress())).phoneNumber(normalize(request.getPhone())).city(normalize(request.getCity())).district(normalize(request.getDistrict())).latitude(request.getLatitude()).longitude(request.getLongitude()).boardingType(boardingType).status(Status.VERIFIED).isPrimaryBranch(false).build());

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

        if (error != null && !error.isBlank()) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, error, null);
        }

        AdmissionCampaign admissionCampaign = AdmissionCampaign.builder().school(actorCampus.getSchool()).name(normalize(request.getName())).description(normalize(request.getDescription())).year(request.getYear()).startDate(request.getStartDate()).endDate(request.getEndDate()).status(Status.OPEN).build();
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
        if (request.getStartDate().getYear() != request.getYear() || request.getEndDate().getYear() != request.getYear()) {
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

        return null;
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

        String error = validationUpdateAdmissionCampaignTemplate(request, admissionCampaign);

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

    private String validationUpdateAdmissionCampaignTemplate(UpdateAdmissionCampaignTemplateRequest request, AdmissionCampaign admissionCampaign) {

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

        // 1. Lấy ngày hiện tại trong DB để so sánh
        LocalDate oldStart = admissionCampaign.getStartDate();
        // 3. Logic thời gian
        // StartDate cho phép lùi 1 ngày

        // 2. Logic StartDate: Chỉ chặn nếu người dùng THAY ĐỔI ngày bắt đầu sang một ngày quá khứ mới
        // Nếu họ giữ nguyên ngày cũ (dù là quá khứ), thì cho phép qua.
        if (!request.getStartDate().equals(oldStart)) {
            if (request.getStartDate().isBefore(LocalDate.now().minusDays(1))) {
                return "Start date cannot be in the past";
            }
        }

        // EndDate phải từ hôm nay trở đi
        if (request.getEndDate().isBefore(LocalDate.now())) {
            return "End date must be a future date";
        }

        // End phải sau Start (Dùng !isAfter để đảm bảo không trùng ngày)
        if (!request.getEndDate().isAfter(request.getStartDate())) {
            return "End date must be after start date";
        }

        List<CampusProgramOffering> offeringList = campusProgramOfferingRepo.findByAdmissionCampaignId(admissionCampaign.getId());

        Optional<CampusProgramOffering> checkOffering = offeringList.stream()
                .filter(o -> o.getOpenDate().isBefore(request.getStartDate())
                        || o.getCloseDate().isAfter(request.getEndDate()))
                .findFirst();

        return checkOffering.map(campusProgramOffering -> "Cannot update: The program offering '" + campusProgramOffering.getProgram().getCurriculum().getName() +
                "' has dates outside the new campaign range.").orElse("");

    }

    @Override
    @Transactional
    public ResponseEntity<ResponseObject> changeAdmissionCampaignStatus(Integer id, Status targetStatus) {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Your account is restricted", null);
        }

        //kiểm tra Actor & Quyền (Tương tự Create/Update)
        Campus actorCampus = extractActorCampus();
        if (actorCampus == null || !actorCampus.getIsPrimaryBranch()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Only primary campus can change status", null);
        }

        //Tìm Campaign
        AdmissionCampaign campaign = admissionCampaignRepo.findById(id)
                .filter(c -> c.getSchool().getId().equals(actorCampus.getSchool().getId()))
                .orElse(null);

        if (campaign == null) return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Campaign not found", null);

        if (campaign.getStatus().equals(targetStatus)) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Campaign is already in status " + targetStatus.name(), null);
        }

        if (campaign.getStatus().equals(Status.CLOSED) || campaign.getStatus().equals(Status.EXPIRED)) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Cannot change status of a closed or expired campaign", null);
        }

        if (targetStatus.equals(Status.OPEN)) {
            if (LocalDate.now().isAfter(campaign.getEndDate())) {
                return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "End date passed. Update campaign duration first.", null);
            }
        }

        campaign.setStatus(targetStatus);
        admissionCampaignRepo.save(campaign);

        // 6. XỬ LÝ CASCADE ĐỒNG BỘ CHO CÁC NGÀNH HỌC (OFFERINGS)
        List<CampusProgramOffering> offerings = campusProgramOfferingRepo.findByAdmissionCampaignId(id);

        if (targetStatus.equals(Status.CLOSED) || targetStatus.equals(Status.PAUSED)) {
            // Nếu Cha dừng/đóng -> Toàn bộ con dừng/đóng theo
            for (CampusProgramOffering offering : offerings) {
                offering.setApplicationStatus(targetStatus);
            }
        } else if (targetStatus.equals(Status.OPEN)) {
            // Nếu Cha mở lại -> Chỉ mở lại những ngành đang bị PAUSED (không mở lại ngành đã FULL/CLOSED)
            for (CampusProgramOffering offering : offerings) {
                if (offering.getApplicationStatus().equals(Status.PAUSED)) {
                    offering.setApplicationStatus(Status.OPEN);
                }
            }
        }

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
            AdmissionCampaign campaign = admissionCampaignRepo.findFirstBySchoolIdAndYearOrderByIdDesc(schoolId, year).orElse(null);

            if (campaign == null) {
                return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Campaign template not found", null);
            }

            return ResponseBuilder.build(HttpStatus.OK, "View campaign template successfully", buildCampaignData(campaign));
        }

        List<AdmissionCampaign> campaignList = admissionCampaignRepo.findBySchoolIdOrderByYearDesc(schoolId);

        List<Map<String, Object>> data = campaignList.stream().map(this::buildCampaignData).toList();

        return ResponseBuilder.build(HttpStatus.OK, "View campaign template list successfully", data);
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

        String error = validationUpsertCurriculum(request);

        if (error != null && !error.isBlank()) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, error, null);
        }

        Curriculum targetCurriculum;

        long currentVersion = Long.parseLong(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));

        boolean isNew = request.getCurriculumId() == null || request.getCurriculumId() <= 0;

        if (isNew) {

            targetCurriculum = buildNewCurriculum(request, actorCampus.getSchool(), currentVersion);
        } else {

            Curriculum existingCurriculum = curriculumRepo.findById(request.getCurriculumId()).orElse(null);

            if (existingCurriculum == null) {
                return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Curriculum not found", null);
            }

            if (Status.CUR_DRAFT.equals(existingCurriculum.getCurriculumStatus())) {
                targetCurriculum = existingCurriculum;
                applyRequestToCurriculum(targetCurriculum, request, currentVersion);
            } else {
                // Sửa bản ACTIVE: Tạo bản sao (Clone) ở dạng DRAFT để không ảnh hưởng dữ liệu đang chạy
                targetCurriculum = evolveFromExisting(existingCurriculum, request, currentVersion);
            }
        }

        targetCurriculum.setCurriculumStatus(Status.CUR_DRAFT);
        demoteLatestStatus(targetCurriculum.getGroupCode(), targetCurriculum.getEnrollmentYear());
        targetCurriculum.setLatest(true);

        curriculumRepo.save(targetCurriculum);
        return ResponseBuilder.build(HttpStatus.OK, isNew ? "Created draft successfully" : "Updated draft successfully", null);
    }

    private void demoteLatestStatus(String groupCode, int enrollmentYear) {
        // 1. Tìm tất cả các bản ghi đang mang cờ isLatest = true trong nhóm này
        List<Curriculum> latestVersions = curriculumRepo.findAllByGroupCodeAndEnrollmentYearAndIsLatestTrue(groupCode, enrollmentYear);

        if (!latestVersions.isEmpty()) {
            latestVersions.forEach(c -> c.setLatest(false));

            curriculumRepo.saveAll(latestVersions);

            curriculumRepo.flush();
        }
    }

    @Override
    @Transactional
    public ResponseEntity<ResponseObject> activateCurriculum(int id) {

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

        // 1. Nếu đã ACTIVE rồi thì thôi
        if (Status.CUR_ACTIVE.equals(target.getCurriculumStatus())) {
            return ResponseBuilder.build(HttpStatus.OK, "This curriculum is already active", null);
        }

        // 2. Chặn ARCHIVED (Bạn đã làm tốt)
        if (Status.CUR_ARCHIVED.equals(target.getCurriculumStatus())) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Cannot activate an archived curriculum", null);
        }

        // 1. Kiểm tra tính hợp lệ của thời điểm nhấn nút (EnrollmentYear)
        int currentYear = Year.now().getValue();
        if (target.getEnrollmentYear() < currentYear - 1) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Cannot activate curriculum for a past enrollment year.", null);
        }

        // 2. Kiểm tra trùng lặp nội dung với bản ACTIVE hiện tại
        Curriculum currentActive = curriculumRepo.findByGroupCodeAndEnrollmentYearAndCurriculumStatus(
                target.getGroupCode(), target.getEnrollmentYear(), Status.CUR_ACTIVE);

        if (currentActive != null) {
            // So sánh nội dung quan trọng: Subjects và Description
            boolean isSameContent = Objects.equals(target.getSubjectsJsonb(), currentActive.getSubjectsJsonb())
                    && Objects.equals(target.getDescription(), currentActive.getDescription())
                    && Objects.equals(target.getMethodLearning(), currentActive.getMethodLearning());

            if (isSameContent) {
                return ResponseBuilder.build(HttpStatus.BAD_REQUEST,
                        "This draft has no changes compared to the current Active version. Activation canceled.", null);
            }
        }

        // 5. THỰC HIỆN PUBLISH (Chuyển DRAFT -> ACTIVE)
        target.setCurriculumStatus(Status.CUR_ACTIVE);
        target.setLatest(true);
        target.setVersion(Long.parseLong(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))));

        curriculumRepo.save(target);
        return ResponseBuilder.build(HttpStatus.OK, "Publish curriculum successful", null);
    }

    private Curriculum buildNewCurriculum(CurriculumRequest request, School school, long version) {
        return Curriculum.builder()
                .name(CurriculumNamingUtil.generateName(request))
                .groupCode(CurriculumNamingUtil.generateGroupCode(request))
                .curriculumType(CurriculumType.valueOf(request.getCurriculumType()))
                .methodLearning(LearningMethod.valueOf(request.getMethodLearning()))
                .enrollmentYear(request.getEnrollmentYear())
                .description(request.getDescription())
                .subjectsJsonb(buildSubjectsJsonb(request.getSubjectOptions()))
                .version(version)
                .school(school)
                .isLatest(false)
                .curriculumStatus(Status.CUR_DRAFT).build();
    }

    // bảng update đối vs draft
    private void applyRequestToCurriculum(Curriculum curriculum, CurriculumRequest request, long version) {

        curriculum.setDescription(request.getDescription());
        curriculum.setSubjectsJsonb(buildSubjectsJsonb(request.getSubjectOptions()));
        curriculum.setMethodLearning(LearningMethod.valueOf(request.getMethodLearning()));
        curriculum.setVersion(version);

        // Chỉ generate lại tên curriculum từ các trường thành phần, tuyệt đối không lấy từ request.getName()
        // Không setName ở bất kỳ nơi nào khác ngoài đây và buildNewCurriculum
        boolean isIdentityChanging = curriculum.getEnrollmentYear() != request.getEnrollmentYear()
                || !curriculum.getCurriculumType().name().equals(request.getCurriculumType())
                || !curriculum.getGroupCode().equals(CurriculumNamingUtil.generateGroupCode(request));

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

    private Curriculum evolveFromExisting(Curriculum existing, CurriculumRequest request, long version) {

        Curriculum clone = Curriculum.builder()
                .name(existing.getName())
                .groupCode(existing.getGroupCode())
                .curriculumType(existing.getCurriculumType())
                .enrollmentYear(existing.getEnrollmentYear())
                .school(existing.getSchool())
                .parent(existing)
                .isLatest(false)
                .curriculumStatus(Status.CUR_DRAFT)
                .build();

        // tận dụng hàm apply để gán các thông tin thay đổi từ request
        applyRequestToCurriculum(clone, request, version);
        return clone;
    }

    // define cấu trúc subjectsJsonb theo format jsonb
    private List<Map<String, Object>> buildSubjectsJsonb(List<CurriculumRequest.SubjectOptionRequest> request) {
        if (request == null) return Collections.emptyList();

        return request.stream().map(opt -> {
            return Map.<String, Object>of(
                    "name", Objects.requireNonNullElse(opt.getName(), ""),
                    "description", Objects.requireNonNullElse(opt.getDescription(), ""),
                    "isMandatory", Boolean.TRUE.equals(opt.getIsMandatory())
            );
        }).collect(Collectors.toList());
    }

    private String validationUpsertCurriculum(CurriculumRequest request) {

        // 1. Kiểm tra tồn tại bản ghi và tính bất biến (Immutability)
        Curriculum existing = null;
        if (request.getCurriculumId() != null && request.getCurriculumId() > 0) {
            existing = curriculumRepo.findById(request.getCurriculumId()).orElse(null);
            if (existing == null) return "Curriculum not found";

            // Kiểm tra Program liên kết để chặn đổi thông tin định danh
            int linkedPrograms = programRepo.countByCurriculumId(existing.getId());
            if (linkedPrograms > 0) {
                if (existing.getEnrollmentYear() != request.getEnrollmentYear()) {
                    return String.format("Cannot change enrollment year because %d programs are using this curriculum.", linkedPrograms);
                }
                if (!existing.getCurriculumType().name().equals(request.getCurriculumType())) {
                    return "Cannot change curriculum type for a curriculum already linked to programs.";
                }
            }
        }

        // 2. Kiểm tra trùng lặp định danh (Business Identity Check)
        // Tránh việc tạo 2 bản ghi khác ID nhưng cùng (Type + Year + SubType) dẫn đến trùng GroupCode
        String newGroupCode = CurriculumNamingUtil.generateGroupCode(request);

        // Nếu tạo mới, hoặc sửa bản cũ mà thay đổi thông tin định danh (Identity)
        boolean isIdentityChanged = (existing == null) ||
                (!existing.getGroupCode().equals(newGroupCode)) ||
                (existing.getEnrollmentYear() != request.getEnrollmentYear());

        // 3. Validate các trường bắt buộc & Enum
        if (StringUtils.isBlank(request.getSubTypeName())) {
            return "Sub-type name is required (e.g., Cambridge, Global).";
        }

        try {
            CurriculumType.valueOf(request.getCurriculumType());
            LearningMethod.valueOf(request.getMethodLearning());
        } catch (Exception e) {
            return "Invalid Curriculum Type or Learning Method selected.";
        }

        // 4. Validate Năm học (5 năm cũ - 2 năm tương lai)
        int currentYear = Year.now().getValue();
        if (request.getEnrollmentYear() < currentYear - 5 || request.getEnrollmentYear() > currentYear + 2) {
            return String.format("Invalid enrollment year. Allowed range: %d to %d.", currentYear - 5, currentYear + 2);
        }

        // 5. Validate Nội dung môn học (Subjects)
        if (request.getSubjectOptions() == null || request.getSubjectOptions().isEmpty()) {
            return "The curriculum must contain at least one subject.";
        }

        // Kiểm tra từng môn học trong danh sách
        for (var opt : request.getSubjectOptions()) {
            if (StringUtils.isBlank(opt.getName())) {
                return "Subject name cannot be empty.";
            }
            if (StringUtils.isBlank(opt.getDescription())) {
                return "Description for subject '" + opt.getName() + "' is required.";
            }
        }

        // Chốt chặn nghiệp vụ: Phải có ít nhất 1 môn bắt buộc
        boolean hasMandatory = request.getSubjectOptions().stream()
                .anyMatch(o -> Boolean.TRUE.equals(o.getIsMandatory()));

        if (!hasMandatory) {
            return "The curriculum must have at least one mandatory (required) subject.";
        }

        return null;
    }

    @Override
    public ResponseEntity<ResponseObject> viewCurriculumList(int page, int pageSize) {

        Campus actorCampus = extractActorCampus();

        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.UNAUTHORIZED, "User session invalid or school not found", null);
        }

        Pageable pageable;
        try {
            pageable = PaginationUtil.buildPageRequest(page, pageSize);
        } catch (IllegalArgumentException e) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, e.getMessage(), null);
        }

        Page<Curriculum> curriculumPage = curriculumRepo.findBySchoolIdOrderByEnrollmentYearDescVersionDesc(actorCampus.getSchool().getId(), pageable);

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
        data.put("version", curriculum.getVersion());
        data.put("versionDisplay", CurriculumNamingUtil.formatLongVersion(curriculum.getVersion()));
        data.put("isLatest", curriculum.isLatest());
        data.put("curriculumStatus", curriculum.getCurriculumStatus().name());
        data.put("subjects", curriculum.getSubjectsJsonb());
        data.put("status", curriculum.getCurriculumStatus().name());

        int programCount = (curriculum.getPrograms() != null) ? curriculum.getPrograms().size() : 0;
        data.put("programCount", programCount);
        data.put("canEditIdentity", programCount == 0);

        // Thêm danh sách tên Program để hiển thị Tooltip/Modal
        if (programCount > 0) {
            List<String> linkedProgramNames = curriculum.getPrograms().stream()
                    // Lấy tên Program (thường map từ Graduation Standard hoặc một field name riêng của Program)
                    .map(p -> "Program: " + p.getGraduationStandard()).collect(Collectors.toList());
            data.put("linkedProgramNames", linkedProgramNames);

        } else {

            data.put("linkedProgramNames", Collections.emptyList());
        }
        return data;
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

        String error = validationUpsertProgram(request, actorCampus);

        if (error != null && !error.isBlank()) {
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

        // Đồng bộ dữ liệu (Gom chung cho cả Create/Update để tránh lặp code)
        program.setCurriculum(curriculum);
        program.setGraduationStandard(normalize(request.getGraduationStandard()));
        program.setTargetStudentDescription(normalize(request.getTargetStudentDescription()));
        program.setBaseTuitionFee(request.getBaseTuitionFee());
        program.setActive(Boolean.TRUE.equals(request.getIsActive()));

        try {
            programRepo.save(program);
        } catch (DataIntegrityViolationException e) {
            return ResponseBuilder.build(HttpStatus.CONFLICT, "Graduation standard already exists in this curriculum", null);
        }

        return ResponseBuilder.build(isNew ? HttpStatus.CREATED : HttpStatus.OK, isNew ? "Create Program success" : "Update Program success", null);
    }

    private String validationUpsertProgram(ProgramRequest request, Campus actorCampus) {

        if (request == null) {
            return "Request is required";
        }

        if (request.getCurriculumId() == null) {
            return "Curriculum ID is not found";
        }

        Curriculum curriculum = curriculumRepo.findById(request.getCurriculumId()).orElse(null);
        if (curriculum == null || !curriculum.getSchool().getId().equals(actorCampus.getSchool().getId())) {
            return "Curriculum is invalid";
        }

        if (curriculum.getCurriculumStatus() != Status.CUR_ACTIVE) {
            return "Only active curriculum can be used for a program";
        }

        String graduationStandard = normalize(request.getGraduationStandard());
        if (graduationStandard == null) {
            return "Graduation standard is required";
        }

        if (graduationStandard.length() > 2000) {
            return "Graduation standard exceeds 2000 characters";
        }

        String targetStudentDescription = normalize(request.getTargetStudentDescription());
        if (targetStudentDescription == null) {
            return "Target student description is required";
        }

        if (targetStudentDescription.length() > 2000) {
            return "Target student description exceeds 2000 characters";
        }

        boolean isUpdate = request.getProgramId() != null && request.getProgramId() > 0;

        if (isUpdate) {

            Program existingProgram = programRepo.findByIdAndCurriculum_School_Id(request.getProgramId(), actorCampus.getSchool().getId());

            if (existingProgram == null) {
                return "Program not found in your school scope";
            }

            boolean isCurriculumChanging = !existingProgram.getCurriculum().getId().equals(request.getCurriculumId());
            int offeringCount = programRepo.countOfferingsById(existingProgram.getId());
            int effectiveOfferingCount = programRepo.countEffectiveOfferingsById(existingProgram.getId(),
                    List.of(Status.OPEN, Status.PAUSED),
                    List.of(Status.OPEN, Status.PAUSED, Status.FULL));

            if (offeringCount > 0 && isCurriculumChanging) {
                return "Cannot change curriculum because this program has active offerings/enrollments.";
            }

            if (effectiveOfferingCount > 0 && existingProgram.isActive() && Boolean.FALSE.equals(request.getIsActive())) {
                return "Cannot deactivate a program that still has effective offerings.";
            }

            boolean duplicatedWhenUpdate = programRepo.existsByCurriculum_School_IdAndCurriculum_IdAndGraduationStandardIgnoreCaseAndIdNot(
                    actorCampus.getSchool().getId(),
                    request.getCurriculumId(),
                    graduationStandard,
                    existingProgram.getId()
            );

            if (duplicatedWhenUpdate) {
                return "Graduation standard already exists in this curriculum";
            }
        } else {
            boolean duplicatedWhenCreate = programRepo.existsByCurriculum_School_IdAndCurriculum_IdAndGraduationStandardIgnoreCase(
                    actorCampus.getSchool().getId(),
                    request.getCurriculumId(),
                    graduationStandard
            );

            if (duplicatedWhenCreate) {
                return "Graduation standard already exists in this curriculum";
            }
        }

        if (request.getBaseTuitionFee() == null) return "Tuition fee is required";

        if (request.getBaseTuitionFee().compareTo(BigDecimal.ZERO) < 0) return "Tuition fee cannot be negative";

        if (request.getIsActive() == null) return "Active flag is required";

        return null;
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

        String error = validateCreateCampusProgramOffering(request, actorCampus);

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

        Campus targetCampus = resolveTargetCampus(actorCampus, request.getCampusId());

        if (targetCampus == null) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Campus is out of your scope", null);
        }

        Status applicationStatus = parseApplicationStatus(request.getApplicationStatus());

        if (applicationStatus == null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Application status must be OPEN, PAUSED, FULL, or CLOSED", null);
        }

        campusProgramOfferingRepo.save(CampusProgramOffering.builder()
                .campus(targetCampus)
                .admissionCampaign(campaign)
                .program(program)
                .quota(request.getQuota())
                .remainingQuota((parseApplicationStatus(request.getApplicationStatus()) == Status.FULL) ? 0 : request.getQuota())
                .learningMode(request.getLearningMode())
                .priceAdjustmentPercentage(0)
                .tuitionFee(request.getTuitionFee())
                .applicationStatus(applicationStatus)
                .openDate((request.getOpenDate() != null) ? request.getOpenDate() : campaign.getStartDate())
                .closeDate((request.getCloseDate() != null) ? request.getCloseDate() : campaign.getEndDate())
                .status(Status.OPEN)
                .build());

        return ResponseBuilder.build(HttpStatus.OK, "Create campus offering successfully", null);
    }

    private String validateCreateCampusProgramOffering(CreateCampusProgramOfferingRequest request, Campus
            actorCampus) {

        if (request == null || request.getAdmissionCampaignId() == null || request.getProgramId() == null || request.getLearningMode() == null || request.getQuota() <= 0) {
            return "Campaign, program, learning mode and quota are required";
        }

        AdmissionCampaign campaign = admissionCampaignRepo.findById(request.getAdmissionCampaignId()).orElse(null);
        if (campaign == null || !campaign.getSchool().getId().equals(actorCampus.getSchool().getId())) {
            return "Campaign is out of your school scope";
        }

        if (campaign.getStatus() == Status.CLOSED || campaign.getStatus() == Status.EXPIRED) {
            return "Cannot add offering to closed/expired campaign";
        }

        Program program = programRepo.findByIdAndCurriculum_School_Id(request.getProgramId(), actorCampus.getSchool().getId());

        if (program == null) {
            return "Program not found";
        }

        if (!program.isActive()) {
            return "Program is inactive";
        }

        if (program.getCurriculum().getCurriculumStatus() != Status.CUR_ACTIVE) {
            return "Program curriculum must be active";
        }

        if (campaign.getYear() != program.getCurriculum().getEnrollmentYear()) {
            return "Campaign year must match curriculum enrollment year";
        }

        Campus targetCampus = resolveTargetCampus(actorCampus, request.getCampusId());

        if (targetCampus == null) {
            return "Campus is out of your scope";
        }

        BigDecimal tuitionFee = request.getTuitionFee();

        if (tuitionFee == null || tuitionFee.signum() < 0) {
            return "Tuition fee must be >= 0";
        }

        AdmissionCampaign finalCampaign = campaign;

        LocalDate openDate = request.getOpenDate() != null ? request.getOpenDate() : finalCampaign.getStartDate();

        LocalDate closeDate = request.getCloseDate() != null ? request.getCloseDate() : finalCampaign.getEndDate();

        if (closeDate.isBefore(openDate)) {
            return "Close date must be after or equal to open date";
        }

        if (openDate.isBefore(finalCampaign.getStartDate()) || closeDate.isAfter(finalCampaign.getEndDate())) {
            return "Offering open/close date must be within campaign date range";
        }

        Status applicationStatus = parseApplicationStatus(request.getApplicationStatus());

        if (applicationStatus == null) {
            return "Application status must be OPEN, PAUSED, FULL, or CLOSED";
        }

        boolean duplicatedOffering = campusProgramOfferingRepo.existsByAdmissionCampaignIdAndCampusIdAndProgramIdAndLearningMode(
                finalCampaign.getId(),
                targetCampus.getId(),
                program.getId(),
                request.getLearningMode()
        );

        if (duplicatedOffering) {
            return "This campus already has the same program offering in this campaign";
        }

        return null;
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
        int usedQuota = (offering != null) ? Math.max(0, offering.getQuota() - offering.getRemainingQuota()) : 0;
        AdmissionCampaign targetCampaign = offering != null ? offering.getAdmissionCampaign() : null;
        if (request.getAdmissionCampaignId() != null && (targetCampaign == null || !request.getAdmissionCampaignId().equals(targetCampaign.getId()))) {
            targetCampaign = admissionCampaignRepo.findById(request.getAdmissionCampaignId()).orElse(null);
        }
        Campus targetCampus = offering != null ? offering.getCampus() : null;
        if (request.getCampusId() != null && (targetCampus == null || !request.getCampusId().equals(targetCampus.getId()))) {
            targetCampus = resolveTargetCampus(actorCampus, request.getCampusId());
        }
        Program targetProgram = offering != null ? offering.getProgram() : null;
        if (request.getProgramId() != null && (targetProgram == null || !request.getProgramId().equals(targetProgram.getId()))) {
            targetProgram = programRepo.findByIdAndCurriculum_School_Id(request.getProgramId(), actorCampus.getSchool().getId());
        }
        LearningMode targetLearningMode = request.getLearningMode() != null ? request.getLearningMode() : (offering != null ? offering.getLearningMode() : null);
        Integer targetQuota = request.getQuota() != null ? request.getQuota() : (offering != null ? offering.getQuota() : null);
        Status targetApplicationStatus = request.getApplicationStatus() != null
                ? parseApplicationStatus(request.getApplicationStatus())
                : (offering != null ? offering.getApplicationStatus() : null);
        LocalDate targetOpenDate = request.getOpenDate() != null ? request.getOpenDate()
                : (offering != null && offering.getOpenDate() != null ? offering.getOpenDate() : (targetCampaign != null ? targetCampaign.getStartDate() : null));
        LocalDate targetCloseDate = request.getCloseDate() != null ? request.getCloseDate()
                : (offering != null && offering.getCloseDate() != null ? offering.getCloseDate() : (targetCampaign != null ? targetCampaign.getEndDate() : null));
        String error = validateUpdateCampusProgramOffering(request, actorCampus, offering, targetCampaign, targetCampus, targetProgram, usedQuota, targetApplicationStatus, targetQuota, targetOpenDate, targetCloseDate, targetLearningMode);
        if (error != null) {
            if (error.contains("already has the same program offering")) {
                return ResponseBuilder.build(HttpStatus.CONFLICT, error, null);
            }
            if (error.contains("out of your school scope") || error.contains("out of your scope") || error.contains("only update your campus offering")) {
                return ResponseBuilder.build(HttpStatus.FORBIDDEN, error, null);
            }
            if (error.contains("not found")) {
                return ResponseBuilder.build(HttpStatus.NOT_FOUND, error, null);
            }
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, error, null);
        }
        int targetRemainingQuota = targetApplicationStatus == Status.FULL ? 0 : (targetQuota - usedQuota);
        offering.setAdmissionCampaign(targetCampaign);
        offering.setCampus(targetCampus);
        offering.setProgram(targetProgram);
        offering.setLearningMode(targetLearningMode);
        offering.setQuota(targetQuota);
        offering.setRemainingQuota(targetRemainingQuota);
        BigDecimal targetTuition = request.getTuitionFee() != null ? request.getTuitionFee() : offering.getTuitionFee();
        offering.setTuitionFee(targetTuition);
        offering.setOpenDate(targetOpenDate);
        offering.setCloseDate(targetCloseDate);
        offering.setApplicationStatus(targetApplicationStatus);
        try {
            campusProgramOfferingRepo.save(offering);
        } catch (DataIntegrityViolationException e) {
            return ResponseBuilder.build(HttpStatus.CONFLICT, "This campus already has the same program offering in this campaign", null);
        }
        return ResponseBuilder.build(HttpStatus.OK, "Update campus offering successfully", buildOfferingData(offering));
    }

    // Validation cho updateCampusProgramOffering
    private String validateUpdateCampusProgramOffering(UpdateCampusProgramOfferingRequest request, Campus
            actorCampus, CampusProgramOffering offering, AdmissionCampaign targetCampaign, Campus targetCampus, Program
                                                               targetProgram, int usedQuota, Status targetApplicationStatus, Integer targetQuota, LocalDate
                                                               targetOpenDate, LocalDate targetCloseDate, LearningMode targetLearningMode) {
        if (request == null || request.getId() == null || request.getId() <= 0) {
            return "Offering id is required";
        }
        if (offering == null) {
            return "Offering not found";
        }
        if (!offering.getAdmissionCampaign().getSchool().getId().equals(actorCampus.getSchool().getId())) {
            return "Offering is out of your school scope";
        }
        if (!actorCampus.getIsPrimaryBranch() && !offering.getCampus().getId().equals(actorCampus.getId())) {
            return "You can only update your campus offering";
        }
        if (targetProgram == null) {
            return "Target program is invalid";
        }
        if (!targetProgram.isActive()) {
            return "Program is inactive";
        }
        if (targetProgram.getCurriculum().getCurriculumStatus() != Status.CUR_ACTIVE) {
            return "Program curriculum must be active";
        }
        if (targetCampaign.getStatus() == Status.CLOSED || targetCampaign.getStatus() == Status.EXPIRED) {
            return "Cannot move offering to closed/expired campaign";
        }
        if (targetCampaign.getYear() != targetProgram.getCurriculum().getEnrollmentYear()) {
            return "Campaign year must match curriculum enrollment year";
        }
        boolean identityChanged = !targetCampaign.getId().equals(offering.getAdmissionCampaign().getId())
                || !targetCampus.getId().equals(offering.getCampus().getId())
                || !targetProgram.getId().equals(offering.getProgram().getId())
                || targetLearningMode != offering.getLearningMode();
        if (usedQuota > 0 && identityChanged) {
            return "Cannot change campaign/campus/program/mode after applications have been received";
        }
        if (targetQuota == null || targetQuota <= 0) {
            return "Quota must be greater than 0";
        }
        if (targetQuota < usedQuota) {
            return "Quota cannot be smaller than registered quantity";
        }
        if (targetOpenDate == null || targetCloseDate == null) {
            return "Open date and close date are required";
        }
        if (targetCloseDate.isBefore(targetOpenDate)) {
            return "Close date must be after or equal to open date";
        }
        if (targetOpenDate.isBefore(targetCampaign.getStartDate()) || targetCloseDate.isAfter(targetCampaign.getEndDate())) {
            return "Offering open/close date must be within campaign date range";
        }
        if (targetApplicationStatus == null) {
            return "Application status must be OPEN, PAUSED, FULL, or CLOSED";
        }
        if (targetApplicationStatus == Status.OPEN && targetQuota == usedQuota) {
            return "Cannot set OPEN status when remaining quota is zero";
        }
        boolean duplicatedOffering = campusProgramOfferingRepo.existsByAdmissionCampaignIdAndCampusIdAndProgramIdAndLearningModeAndIdNot(
                targetCampaign.getId(),
                targetCampus.getId(),
                targetProgram.getId(),
                targetLearningMode,
                offering.getId()
        );
        if (duplicatedOffering) {
            return "This campus already has the same program offering in this campaign";
        }
        BigDecimal targetTuition = request.getTuitionFee() != null ? request.getTuitionFee() : offering.getTuitionFee();
        if (targetTuition == null || targetTuition.signum() < 0) {
            return "Tuition fee must be >= 0";
        }
        return null;
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

    private Map<String, Object> buildProgramData(Program program) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", program.getId());
        data.put("graduationStandard", program.getGraduationStandard());
        data.put("targetStudentDescription", program.getTargetStudentDescription());
        data.put("baseTuitionFee", program.getBaseTuitionFee());
        data.put("isActive", program.isActive());
        data.put("programStatus", program.isActive() ? Status.PRO_ACTIVE : Status.PRO_INACTIVE);

        Curriculum curriculum = program.getCurriculum();
        data.put("curriculumId", curriculum.getId());
        data.put("curriculumName", curriculum.getName());
        data.put("curriculumType", curriculum.getCurriculumType());
        data.put("enrollmentYear", curriculum.getEnrollmentYear());
        data.put("curriculumStatus", curriculum.getCurriculumStatus());
        data.put("schoolId", curriculum.getSchool().getId());
        data.put("offeringCount", program.getCampusProgramOfferingList().size());
        return data;
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
        data.put("programName", offering.getProgram().getGraduationStandard());
        data.put("curriculumId", offering.getProgram().getCurriculum().getId());
        data.put("curriculumType", offering.getProgram().getCurriculum().getCurriculumType());
        data.put("enrollmentYear", offering.getProgram().getCurriculum().getEnrollmentYear());
        data.put("quota", offering.getQuota());
        data.put("remainingQuota", offering.getRemainingQuota());
        data.put("learningMode", offering.getLearningMode());
        data.put("tuitionFee", offering.getTuitionFee());
        data.put("baseTuitionFee", offering.getProgram().getBaseTuitionFee());
        data.put("applicationStatus", offering.getApplicationStatus());
        data.put("openDate", offering.getOpenDate());
        data.put("closeDate", offering.getCloseDate());
        data.put("status", offering.getStatus());
        data.put("schoolId", offering.getAdmissionCampaign().getSchool().getId());
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

    private Status parseApplicationStatus(String value) {
        String normalized = normalize(value);
        if (normalized == null) {
            return Status.OPEN;
        }

        String enumKey = normalized.toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');

        Status parsed;
        try {
            parsed = Status.valueOf(enumKey);
        } catch (IllegalArgumentException ignored) {
            return null;
        }

        if (parsed != Status.OPEN && parsed != Status.PAUSED && parsed != Status.FULL && parsed != Status.CLOSED) {
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

        Account account = accountRepo.save(Account.builder().email(normalize(request.getEmail())).role(Role.COUNSELLOR).status(Status.ACCOUNT_ACTIVE).registerDate(LocalDate.now()).firstLogin(true).build());

        Counsellor counsellor = counsellorRepo.save(Counsellor.builder().account(account).campus(actorCampus).employeeCode(UUID.randomUUID()).build());

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

        Page<Counsellor> counsellorPage = counsellorRepo.findByCampusId(actorCampus.getId(), pageable);

        PageResponse<Map<String, Object>> pageResponse = PaginationUtil.buildPageResponse(counsellorPage, this::buildCounsellorData);

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
}
