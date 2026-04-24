package com.sp26se041.edubridgehcm.services.implementors;

import com.sp26se041.edubridgehcm.configurations.VNPayConfig;
import com.sp26se041.edubridgehcm.enums.BoardingType;
import com.sp26se041.edubridgehcm.enums.CategoryTemplate;
import com.sp26se041.edubridgehcm.enums.CurriculumType;
import com.sp26se041.edubridgehcm.enums.FeeUnit;
import com.sp26se041.edubridgehcm.enums.LanguageInstruction;
import com.sp26se041.edubridgehcm.enums.LearningMethod;
import com.sp26se041.edubridgehcm.enums.PackageType;
import com.sp26se041.edubridgehcm.enums.ResourceType;
import com.sp26se041.edubridgehcm.enums.Role;
import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.enums.SubjectType;
import com.sp26se041.edubridgehcm.enums.SubscriptionAction;
import com.sp26se041.edubridgehcm.models.Account;
import com.sp26se041.edubridgehcm.models.AdmissionCampaign;
import com.sp26se041.edubridgehcm.models.Campus;
import com.sp26se041.edubridgehcm.models.CampusProgramOffering;
import com.sp26se041.edubridgehcm.models.CampusResourceQuota;
import com.sp26se041.edubridgehcm.models.CampusScheduleTemplate;
import com.sp26se041.edubridgehcm.models.Counsellor;
import com.sp26se041.edubridgehcm.models.Curriculum;
import com.sp26se041.edubridgehcm.models.OpenDayEvent;
import com.sp26se041.edubridgehcm.models.Parent;
import com.sp26se041.edubridgehcm.models.PaymentTransaction;
import com.sp26se041.edubridgehcm.models.PlatformConfig;
import com.sp26se041.edubridgehcm.models.Program;
import com.sp26se041.edubridgehcm.models.School;
import com.sp26se041.edubridgehcm.models.SchoolConfig;
import com.sp26se041.edubridgehcm.models.SchoolSubscription;
import com.sp26se041.edubridgehcm.models.Subject;
import com.sp26se041.edubridgehcm.models.Subscription;
import com.sp26se041.edubridgehcm.models.TemplateDocx;
import com.sp26se041.edubridgehcm.repositories.AccountRepo;
import com.sp26se041.edubridgehcm.repositories.AdmissionCampaignRepo;
import com.sp26se041.edubridgehcm.repositories.AdmissionReservationFormRepo;
import com.sp26se041.edubridgehcm.repositories.CampusProgramOfferingRepo;
import com.sp26se041.edubridgehcm.repositories.CampusRepo;
import com.sp26se041.edubridgehcm.repositories.CampusResourceQuotaRepo;
import com.sp26se041.edubridgehcm.repositories.CampusScheduleTemplateRepo;
import com.sp26se041.edubridgehcm.repositories.CurriculumRepo;
import com.sp26se041.edubridgehcm.repositories.FavouriteSchoolRepo;
import com.sp26se041.edubridgehcm.repositories.OpenDayEventRepo;
import com.sp26se041.edubridgehcm.repositories.ParentRepo;
import com.sp26se041.edubridgehcm.repositories.PaymentTransactionRepo;
import com.sp26se041.edubridgehcm.repositories.PlatformConfigRepo;
import com.sp26se041.edubridgehcm.repositories.PostRepo;
import com.sp26se041.edubridgehcm.repositories.ProgramRepo;
import com.sp26se041.edubridgehcm.repositories.SchoolConfigRepo;
import com.sp26se041.edubridgehcm.repositories.SchoolRepo;
import com.sp26se041.edubridgehcm.repositories.SchoolSubscriptionRepo;
import com.sp26se041.edubridgehcm.repositories.SubjectRepo;
import com.sp26se041.edubridgehcm.repositories.SubscriptionRepo;
import com.sp26se041.edubridgehcm.repositories.TemplateDocxRepo;
import com.sp26se041.edubridgehcm.requests.CreateAdmissionCampaignTemplateRequest;
import com.sp26se041.edubridgehcm.requests.CreateCampusRequest;
import com.sp26se041.edubridgehcm.requests.CreateOpenDayEventRequest;
import com.sp26se041.edubridgehcm.requests.CreateSubscriptionRequest;
import com.sp26se041.edubridgehcm.requests.CurriculumRequest;
import com.sp26se041.edubridgehcm.requests.ProgramRequest;
import com.sp26se041.edubridgehcm.requests.SubscriptionPreviewRequest;
import com.sp26se041.edubridgehcm.requests.UpdateAdmissionCampaignTemplateRequest;
import com.sp26se041.edubridgehcm.responses.PageResponse;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.SchoolService;
import com.sp26se041.edubridgehcm.services.SupabaseStorageService;
import com.sp26se041.edubridgehcm.utils.AccountRestrictionUtil;
import com.sp26se041.edubridgehcm.utils.AuthRequestUtil;
import com.sp26se041.edubridgehcm.utils.ConfigSystemUtil;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchoolServiceImpl implements SchoolService {

    private final PostRepo postRepo;
    private final SubjectRepo subjectRepo;
    @Value("${AI_SERVICE_N8N}")
    private String n8nUrl;

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

    private final PlatformConfigRepo platformConfigRepo;

    private final CampusResourceQuotaRepo campusResourceQuotaRepo;

    private final TemplateDocxRepo templateDocxRepo;

    private final SupabaseStorageService supabaseStorageService;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    @Transactional
    public ResponseEntity<ResponseObject> createCampus(CreateCampusRequest request) {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Tài khoản của bạn đã bị vô hiệu", null);
        }

        Campus actorCampus = extractActorCampus();

        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Tài khoản campus đã bị vô hiệu", null);
        }

        if (!actorCampus.getIsPrimaryBranch()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Tài khoản campus chính mới có quyền tạo cơ sở", null);
        }

        String error = CampusValidation.validateCreateCampus(request, accountRepo, campusRepo, actorCampus.getSchool().getId());
        if (error != null && !error.isBlank()) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, error, null);
        }

        BoardingType boardingType = parseBoardingType(request.getBoardingType());

        if (boardingType == null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Dịch vụ nội trú không hợp lệ. Giá trị hợp lệ bao gồm: FULL_BOARDING, SEMI_BOARDING, BOTH.", null);
        }

        Account acc = accountRepo.save(Account.builder().email(normalize(request.getEmail())).role(Role.SCHOOL).status(Status.ACCOUNT_ACTIVE).registerDate(LocalDate.now()).firstLogin(true).isRestricted(false).build());

        Campus campus = campusRepo.save(Campus.builder().school(actorCampus.getSchool()).account(acc).name(generateCampusName(actorCampus.getSchool().getId())).address(normalize(request.getAddress())).phoneNumber(normalize(request.getPhone())).city(normalize(request.getCity())).district(normalize(request.getDistrict())).ward(normalize(request.getWard())).boardingType(boardingType).latitude(request.getLatitude()).longitude(request.getLongitude()).status(Status.ACTIVE).isPrimaryBranch(false).build());

        String fileName = "";
        String folderName = "";

        try {

            SchoolConfig facilityData = schoolConfigRepo.findBySchoolIdAndKey(campus.getSchool().getId(), "facilityData").orElse(null);

            List<Map<String, Object>> itemList = new ArrayList<>();

            if (facilityData != null && facilityData.getValue() instanceof Map) {
                Map<String, Object> val = (Map<String, Object>) facilityData.getValue();
                itemList = (List<Map<String, Object>>) val.get("itemList");
            }

            Optional<TemplateDocx> campusTemplateDocx = templateDocxRepo.findTopByTypeOrderByVersionDesc(CategoryTemplate.CAMPUS_INFO_TEMPLATE);

            if (campusTemplateDocx.isEmpty()) {
                throw new Exception("Mẫu tài liệu thông tin cơ sở không có sẵn.");
            }

            String templatePath = campusTemplateDocx.get().getFolderName() + "/" + campusTemplateDocx.get().getFileName();

            String uuid = UUID.randomUUID().toString();

            List<Map<String, Object>> facilityItems = itemList.stream()
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

            Map<String, Object> campusData = buildCampusDocxData(campus, facilityItems);

            String campusName = toSafeObjectKey(campus.getName());

            folderName = actorCampus.getSchool().getFolderPath() + "/" + campusName;
            fileName = "campus_info_" + uuid + ".docx";


            String campusFileUrl = supabaseStorageService.generateDocFileFromTemplate(
                    campusData,
                    templatePath,
                    folderName,
                    fileName
            );

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("type", "campus_info");
            payload.put("schoolId", campus.getSchool().getId());
            payload.put("schoolName", campus.getSchool().getName());
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

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        campus.setFileName(fileName);
        campus.setFolderPath(folderName);
        campusRepo.save(campus);

        Map<String, Object> data = new HashMap<>();
        data.put("campus", buildCampusData(campus));
        data.put("account", buildAccountData(acc));

        return ResponseBuilder.build(HttpStatus.OK, "Tạo cơ sở thành công", data);
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
        return switch (type) {

            case FULL_BOARDING ->
                    "Cơ sở này cung cấp dịch vụ nội trú toàn phần, nơi học sinh sinh hoạt tại trường với chỗ ở, bữa ăn và sự chăm sóc toàn diện hằng ngày.";

            case SEMI_BOARDING ->
                    "Cơ sở này cung cấp dịch vụ bán trú, cho phép học sinh ở lại trường vào ban ngày để dùng bữa, được hỗ trợ học tập và tham gia các hoạt động ngoại khóa mà không lưu trú qua đêm.";

            case BOTH ->
                    "Cơ sở này cung cấp cả dịch vụ nội trú toàn phần và bán trú, mang đến lựa chọn linh hoạt về lưu trú và chăm sóc ban ngày để đáp ứng nhu cầu đa dạng của học sinh.";
        };
    }

    private String generateCampusName(Integer schoolId) {
        int currentCount = campusRepo.countBySchoolId(schoolId);
        if (currentCount == 0) {
            return "Cơ sở 1 (Cơ sở chính)";
        }
        return "Cơ sở " + (currentCount + 1);
    }

    @Override
    public ResponseEntity<ResponseObject> viewCampusList(int page, int pageSize) {
        Campus actorCampus = extractActorCampus();

        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Tài khoản cơ sở của bạn không tồn tại trong hệ thống", null);
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

        return ResponseBuilder.build(HttpStatus.OK, "Hiển thị danh sách cơ sở thành công", pageResponse);
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
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Tài khoản của bạn đã bị vô hiệu", null);
        }

        Campus actorCampus = extractActorCampus();

        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Tài khoản cơ sở của bạn không tồn tại trong hệ thống.", null);
        }

        // actor campus co phai la primary campus ko
        if (!actorCampus.getIsPrimaryBranch()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Tài khoản cơ sở chính mới được phép tạo", null);
        }

        String error = AdmissionCampaignValidation.validationCreateAdmissionCampaignTemplate(request, actorCampus, admissionCampaignRepo);

        if ((error != null)) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, error, null);
        }

        AdmissionCampaign admissionCampaign = AdmissionCampaign.builder().school(actorCampus.getSchool()).name(normalize(request.getName())).description(normalize(request.getDescription())).year(request.getYear()).startDate(request.getStartDate()).endDate(request.getEndDate()).status(Status.DRAFT_ADMISSION_CAMPAIGN).build();
        admissionCampaignRepo.save(admissionCampaign);

        return ResponseBuilder.build(HttpStatus.CREATED, "Tạo chiến dịch tuyển sinh thành công", null);
    }

    @Override
    public ResponseEntity<ResponseObject> cloneAdmissionCampaign(int id) {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Tài khoản của bạn đã bị vô hiệu", null);
        }

        Campus actorCampus = extractActorCampus();

        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Tài khoản cơ sở của bạn không tồn tại trong hệ thống", null);
        }

        // actor campus co phai la primary campus ko
        if (!actorCampus.getIsPrimaryBranch()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Tài khoản cơ sở chính mới được phép nhân bản chiến dịch tuyển sing", null);
        }

        AdmissionCampaign oldCampaign = admissionCampaignRepo.findById(id).orElse(null);
        if (oldCampaign == null)
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Không tìm thấy chiến dịch tuyển sinh trong hệ thống", null);

        // 1. Tạo một Request giả từ dữ liệu cũ
        CreateAdmissionCampaignTemplateRequest request = new CreateAdmissionCampaignTemplateRequest();
        request.setName(oldCampaign.getName() + " (Bản chỉnh sửa)");
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

        return ResponseBuilder.build(HttpStatus.CREATED, "Nhân bản chiến dịch tuyển sinh thành công!", buildCampaignData(newCampaign));
    }

    @Override
    @Transactional
    public ResponseEntity<ResponseObject> updateAdmissionCampaignTemplate(UpdateAdmissionCampaignTemplateRequest request) {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Tài khoản của bạn đã bị vô hiệu", null);
        }

        Campus actorCampus = extractActorCampus();

        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Tài khoản campus đã bị vô hiệu", null);
        }

        // actor campus co phai la primary campus ko
        if (!actorCampus.getIsPrimaryBranch()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Tài khoản cơ sở chính mới được phép cập nhật", null);
        }

        AdmissionCampaign admissionCampaign = admissionCampaignRepo.findById(request.getAdmissionCampaignTemplateId()).orElse(null);

        if (admissionCampaign == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Không tìm thấy chiến dịch tuyển sinh", null);
        }

        //Chỉ cho sửa khi trạng thái là DRAFT ==> đang sửa sao cho update
        if (!admissionCampaign.getStatus().equals(Status.DRAFT_ADMISSION_CAMPAIGN)) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Chỉ các chiến dịch ở trạng thái nháp mới được phép cập nhật. Trạng thái hiện tại: " + admissionCampaign.getStatus(), null);
        }

        //Chỉ cho sửa năm hiện tại
        if (admissionCampaign.getYear() < LocalDate.now().getYear()) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Không thể cập nhật chiến dịch tuyển sinh của các năm trước", null);
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

        return ResponseBuilder.build(HttpStatus.OK, "Cập nhật chiến dịch tuyển sinh thành công", null);
    }

    @Override
    @Transactional
    public ResponseEntity<ResponseObject> publishAdmissionCampaignStatus(int id) {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Tài khoản của bạn đã bị vô hiệu", null);
        }

        //kiểm tra Actor & Quyền (Tương tự Create/Update)
        Campus actorCampus = extractActorCampus();
        if (actorCampus == null || !actorCampus.getIsPrimaryBranch()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Cơ sở chính mới được phép cập nhật trạng thái chiến dịch tuyển sinh", null);
        }

        //Tìm Campaign
        AdmissionCampaign campaign = admissionCampaignRepo.findById(id).filter(c -> c.getSchool().getId().equals(actorCampus.getSchool().getId())).orElse(null);

        if (campaign == null)
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Cơ sở không tồn tại trong hệ thống", null);

        if (!campaign.getStatus().equals(Status.DRAFT_ADMISSION_CAMPAIGN)) {
            return ResponseBuilder.build(
                    HttpStatus.BAD_REQUEST,
                    "Chỉ các chiến dịch ở trạng thái nháp mới được phép công bố",
                    null
            );
        }

        if (admissionCampaignRepo.existsBySchoolIdAndYearAndStatus(
                actorCampus.getSchool().getId(),
                campaign.getYear(),
                Status.OPEN_ADMISSION_CAMPAIGN
        )) {
            return ResponseBuilder.build(
                    HttpStatus.CONFLICT,
                    "Năm học " + campaign.getYear() + " đã tồn tại chiến dịch đang mở. Vui lòng đóng chiến dịch hiện tại trước khi công bố chiến dịch mới",
                    null
            );
        }

// Kiểm tra tính hợp lệ của ngày kết thúc trước khi Publish
        if (LocalDate.now().isAfter(campaign.getEndDate())) {
            return ResponseBuilder.build(
                    HttpStatus.BAD_REQUEST,
                    "Không thể công bố chiến dịch đã hết hạn. Vui lòng cập nhật ngày kết thúc",
                    null
            );
        }

        campaign.setStatus(Status.OPEN_ADMISSION_CAMPAIGN);
        admissionCampaignRepo.save(campaign);

        return ResponseBuilder.build(
                HttpStatus.OK,
                "Công bố chiến dịch tuyển sinh thành công. Các cơ sở hiện có thể đăng ký chương trình tuyển sinh",
                null
        );
    }

    @Override
    @Transactional
    public ResponseEntity<ResponseObject> cancelAdmissionCampaign(int id, String reason) {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Tài khoản của bạn đã bị vô hiệu", null);
        }

        //kiểm tra Actor & Quyền (Tương tự Create/Update)
        Campus actorCampus = extractActorCampus();
        if (actorCampus == null || !actorCampus.getIsPrimaryBranch()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Cơ sở chính mới được phép cập nhật trạng thái chiến dịch tuyển sinh", null);
        }

        //Tìm Campaign
        AdmissionCampaign campaign = admissionCampaignRepo.findById(id).filter(c -> c.getSchool().getId().equals(actorCampus.getSchool().getId())).orElse(null);

        if (campaign == null)
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Cơ sở không tồn tại trong hệ thống", null);

        // 2. Chỉ cho hủy nếu đang OPEN
        if (campaign.getStatus() == Status.CANCELLED_ADMISSION_CAMPAIGN) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Cơ sở hiện tại không hoạt động", null);
        }

        // 3. Kiểm tra xem có hồ sơ nào đang bám vào các Offering của Campaign này không
        // Bạn nên đếm các hồ sơ CHƯA HOÀN THÀNH (Ví dụ: PENDING, PROCESSING)
        long activeProfilesCount = admissionReservationFormRepo.countByCampusProgramOffering_AdmissionCampaign_Id(id);

        if (activeProfilesCount > 0) {
            return ResponseBuilder.build(
                    HttpStatus.PRECONDITION_FAILED,
                    String.format(
                            "Không thể hủy chiến dịch. Hiện có %d hồ sơ đăng ký đang hoạt động thuộc chiến dịch này. " +
                                    "Vui lòng từ chối hoặc xử lý tất cả hồ sơ trước khi hủy để đảm bảo tính toàn vẹn dữ liệu",
                            activeProfilesCount
                    ),
                    null
            );
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

        return ResponseBuilder.build(HttpStatus.OK, "Hủy chiến dịch tuyển sinh thành công", null);
    }

    @Override
    public ResponseEntity<ResponseObject> viewAdmissionCampaignTemplate(int year) {

        Campus actorCampus = extractActorCampus();

        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Tài khoản cơ sở của bạn không tồn tại trong hệ thống", null);
        }

        int schoolId = actorCampus.getSchool().getId();

        if (year > 0) {
            List<AdmissionCampaign> campaigns = admissionCampaignRepo.findBySchoolIdAndYearOrderByStatusAsc(schoolId, year);

            if (campaigns == null) {
                return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Không tìm thấy danh sách chiến dịch dịch tuyển sinh năm " + year, null);
            }

            List<Map<String, Object>> data = campaigns.stream().map(this::buildCampaignData).toList();

            return ResponseBuilder.build(HttpStatus.OK, "Hiển thị danh sách chiến dịch tuyển sinh năm " + year + " thành công", data);
        }

        List<AdmissionCampaign> campaignList = admissionCampaignRepo.findBySchoolIdOrderByYearDesc(schoolId);

        List<Map<String, Object>> data = campaignList.stream().map(this::buildCampaignData).toList();

        return ResponseBuilder.build(HttpStatus.OK, "Hiển thị danh sách chiến dịch tuyển sinh", data);
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
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Tài khoản của bạn đã bị vô hiệu", null);
        }

        Campus actorCampus = extractActorCampus();

        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản cơ sở", null);
        }

        // actor campus co phai la primary campus ko
        if (!actorCampus.getIsPrimaryBranch()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Tài khoản cơ sở chính mới được cập nhật chương trình giảng dạy", null);
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
                return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Không tìm thấy khung chương trình của trường trong hệ thống", null);
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
        Curriculum saved = curriculumRepo.save(targetCurriculum);

        //Khi sửa một bản ACTIVE, hệ thống sẽ tạo ra một bản DRAFT mới (Clone).
        // Nếu bạn không trả về ID của bản DRAFT mới này,
        // Frontend vẫn cầm ID của bản ACTIVE. Khi người dùng nhấn "Lưu" lần 2,
        // Backend lại thấy ID đó là ACTIVE và lại tiếp tục clone ra thêm một bản DRAFT nữa ==> Sinh ra hàng loạt bản nháp trùng lặp.

        return ResponseBuilder.build(isNew ? HttpStatus.CREATED : HttpStatus.OK, isNew ? "Tạo khung chương trình (nháp) thành công" : "Cập nhật khung chương trình thành công", saved.getId());
    }

    @Override
    public ResponseEntity<ResponseObject> extractSubjectsFromExcel(MultipartFile file) {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) { // Sử dụng try-with-resources để tự động đóng file
            List<CurriculumRequest.SubjectOptionRequest> subjects = new ArrayList<>();
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                // 1. Đọc Tên môn (Bắt buộc phải có)
                Cell nameCell = row.getCell(0);
                if (nameCell == null || StringUtils.isBlank(getCellValueAsString(nameCell))) continue;
                String name = getCellValueAsString(nameCell);

                // 2. Đọc Mô tả (Optional - Có thể trống)
                Cell descCell = row.getCell(1);
                String desc = (descCell != null) ? getCellValueAsString(descCell) : "";

                // 3. Xử lý isMandatory (Mặc định là false nếu để trống)
                boolean isMandatory = parseMandatoryCell(row.getCell(2));

                subjects.add(CurriculumRequest.SubjectOptionRequest.builder()
                        .name(name)
                        .description(desc)
                        .isMandatory(isMandatory)
                        .build());
            }
            return ResponseBuilder.build(HttpStatus.OK, "Đọc file thành công", subjects);
        } catch (Exception e) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Lỗi đọc file Excel: " + e.getMessage(), null);
        }
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }

    @Override
    public ResponseEntity<ResponseObject> getNationalCurriculumTemplate() {

        List<Subject> allSubjects = subjectRepo.findAll();

        // lọc 7 môn học bắt buộc
        List<Map<String, Object>> mandatory = allSubjects.stream()
                .filter(s -> s.getType() == SubjectType.THPT_SUBJECT)
                .map(s -> Map.<String, Object>of(
                        "name", s.getName(),
                        "description", "Môn học bắt buộc theo chương trình GDPT 2018",
                        "isMandatory", true
                )).toList();

        // lọc danh sách ngoại ngữ để chọn
        List<Map<String, Object>> languages = allSubjects.stream()
                .filter(s -> s.getType() == SubjectType.FOREIGN_LANGUAGE_SUBJECT)
                .map(s -> Map.<String, Object>of(
                        "id", s.getId(),
                        "name", s.getName(),
                        "description", "Môn ngoại ngữ lựa chọn",
                        "isMandatory", false
                )).toList();

        Map<String, Object> data = Map.of(
                "mandatorySubjects", mandatory,
                "languageOptions", languages
        );
        return ResponseBuilder.build(HttpStatus.OK, "Lấy mẫu chương trình quốc gia thành công", data);
    }

    @Override
    @Transactional
    public ResponseEntity<ResponseObject> handleCurriculumAction(int id, String action) {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Tài khoản của bạn đã bị vô hiệu", null);
        }

        Campus actorCampus = extractActorCampus();

        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Tài khoản cơ sở không tồn tại trong hệ thống", null);
        }

        // actor campus co phai la primary campus ko
        if (!actorCampus.getIsPrimaryBranch()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Chỉ có cơ sở chính mới được phép cập nhật trạng thái khung chương trình", null);
        }

        // 2. Tìm bản ghi muốn kích hoạt
        Curriculum target = curriculumRepo.findById(id).orElse(null);
        if (target == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Khung chương trình không tồn tại trong hệ thống", null);
        }

        if (!target.getSchool().getId().equals(actorCampus.getSchool().getId())) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Khung chương trình không thuộc trường của bạn.", null);
        }

        switch (action.toUpperCase()) {

            case "PUBLISH":
                //Chỉ cho phép Publish nếu đang là DRAFT
                if (!Status.CUR_DRAFT.equals(target.getCurriculumStatus())) {
                    return ResponseBuilder.build(HttpStatus.OK, "Chỉ có khung chương trình nháp mới được công bố", null);
                }

                int yearToPublish = LocalDate.now().getYear();

                //Nếu bạn đang chuẩn bị PUBLISH một bản nháp mới cho "Khối Tự Nhiên - 2024",
                // hệ thống sẽ lục tìm: "Hệ thống đã có bản nào là Tự Nhiên - 2024 đang chạy (ACTIVE) chưa?"
                // . Nếu có, bản đó lập tức bị coi là "phiên bản cũ" và bị đẩy vào kho ARCHIVED.
                Curriculum currentActive = curriculumRepo.findByGroupCodeAndApplicationYearAndCurriculumStatus(target.getGroupCode(),
                        yearToPublish, Status.CUR_ACTIVE);

                //Tìm bản ACTIVE hiện tại của cùng nhóm --> nếu có cho vào bản CUR_ARCHIVED
                if (currentActive != null) {
                    currentActive.setCurriculumStatus(Status.CUR_ARCHIVED);
                    curriculumRepo.save(currentActive);
                }

                String subTypeName = CurriculumNamingUtil.extractSubTypeNameFromName(target.getName());
                target.setName(CurriculumNamingUtil.generatePublishedName(subTypeName, yearToPublish));

                target.setCurriculumStatus(Status.CUR_ACTIVE);
                target.setApplicationYear(yearToPublish);
                curriculumRepo.save(target);
                return ResponseBuilder.build(HttpStatus.OK, "Công bố khung chương trình" + yearToPublish + "thành công", target.getId());

            case "REVISE":
                // Chỉnh sửa, cập nhật dựa trên bản cũ để tạo bản mới
                // Chỉ cho phép REVISE nếu đang là ACTIVE
                if (!Status.CUR_ACTIVE.equals(target.getCurriculumStatus())) {
                    return ResponseBuilder.build(
                            HttpStatus.BAD_REQUEST,
                            "Chỉ khung chương trình đang hoạt động mới được phép chỉnh sửa",
                            null
                    );
                }

                //GIỮ NGUYÊN bản cũ là ACTIVE, chỉ đơn giản là CLONE ra bản DRAFT mới để sửa
                Curriculum newDraft = evolveFromExisting(target, null);

                return ResponseBuilder.build(HttpStatus.OK, "Đã tạo bản nháp mới. Vui lòng cập nhật các thay đổi", curriculumRepo.save(newDraft).getId());

            case "ARCHIVE":
                if (Status.CUR_ARCHIVED.equals(target.getCurriculumStatus())) {
                    return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Khung chương trình đã ở trạng thái lưu trữ.", null);
                }
                if (programRepo.existsByCurriculumId(target.getId())) {
                    return ResponseBuilder.build(
                            HttpStatus.CONFLICT,
                            "Không thể lưu trữ khung chương trình vì vẫn còn chương trình đào tạo đang gắn với khung này. "
                                    + "Vui lòng gỡ hoặc chuyển các chương trình sang khung khác trước, hoặc dùng luồng REVISE/PUBLISH để thay thế phiên bản.",
                            null
                    );
                }
                target.setCurriculumStatus(Status.CUR_ARCHIVED);
                curriculumRepo.save(target);
                return ResponseBuilder.build(HttpStatus.OK, "Đã lưu trữ khung chương trình thành công", target.getId());

            default:
                return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Hành động cập nhật không hợp lệ: " + action, null);
        }
    }

    private Curriculum buildNewCurriculum(CurriculumRequest request, School school) {
        CurriculumType type = CurriculumValidation.parseCurriculumType(request.getCurriculumType());
        return Curriculum.builder()
                .name(CurriculumNamingUtil.generateDraftName(request))
                .groupCode(CurriculumNamingUtil.generateGroupCode(request))
                .curriculumType(type)
                .learningMethodList(request.getMethodLearningList().stream().map(LearningMethod::valueOf).collect(Collectors.toList()))
                .description(request.getDescription())
                .subjectsJsonb(buildSubjectsJsonb(request.getSubjectOptions()))
                .school(school)
                .curriculumStatus(Status.CUR_DRAFT).build();
    }

    // bảng update đối vs draft
    private void applyRequestToCurriculum(Curriculum curriculum, CurriculumRequest request) {

        if (request == null) return;
        curriculum.setDescription(request.getDescription());
        curriculum.setSubjectsJsonb(buildSubjectsJsonb(request.getSubjectOptions()));
        curriculum.setLearningMethodList(request.getMethodLearningList().stream().map(LearningMethod::valueOf).collect(Collectors.toList()));

        // Chỉ generate lại tên curriculum từ các trường thành phần, tuyệt đối không lấy từ request.getName()
        // Không setName ở bất kỳ nơi nào khác ngoài đây và buildNewCurriculum
        boolean isIdentityChanging = !curriculum.getCurriculumType().name().equals(request.getCurriculumType()) || !curriculum.getGroupCode().equals(CurriculumNamingUtil.generateGroupCode(request));

        if (isIdentityChanging) {
            boolean hasLinkedPrograms = curriculum.getId() != null && programRepo.existsByCurriculumId(curriculum.getId());
            if (!hasLinkedPrograms) {
                curriculum.setName(CurriculumNamingUtil.generateDraftName(request));
                curriculum.setGroupCode(CurriculumNamingUtil.generateGroupCode(request));
                curriculum.setCurriculumType(CurriculumType.valueOf(request.getCurriculumType()));
            }
        }
    }

    private Curriculum evolveFromExisting(Curriculum existing, CurriculumRequest request) {
        Curriculum clone = Curriculum.builder()
                .name(existing.getName())
                .groupCode(existing.getGroupCode())
                .description(existing.getDescription())
                .curriculumType(existing.getCurriculumType())
                .learningMethodList(existing.getLearningMethodList())
                .applicationYear(null)
                .subjectsJsonb(existing.getSubjectsJsonb())
                .school(existing.getSchool())
                .parent(existing)
                .curriculumStatus(Status.CUR_DRAFT).build();

        if (request != null) {
            applyRequestToCurriculum(clone, request);
        }

        return clone;
    }

    // define cấu trúc subjectsJsonb theo format jsonb
    private List<Map<String, Object>> buildSubjectsJsonb(List<CurriculumRequest.SubjectOptionRequest> request) {
        if (request == null) return Collections.emptyList();

        return request.stream().map(opt -> {
            return Map.<String, Object>of("name", Objects.requireNonNullElse(opt.getName(), ""),
                    "description", Objects.requireNonNullElse(opt.getDescription(), ""),
                    "isMandatory", true); // đối vs môn bắt buộc là true
        }).collect(Collectors.toList());
    }

    @Override
    public ResponseEntity<ResponseObject> viewCurriculumList(int page, int pageSize) {

        Campus actorCampus = extractActorCampus();

        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.UNAUTHORIZED, "Tài khoản cơ sở không tồn tại trong hệ thống", null);
        }

        Pageable pageable;
        try {
            pageable = PaginationUtil.buildPageRequest(page, pageSize);
        } catch (IllegalArgumentException e) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, e.getMessage(), null);
        }

        Page<Curriculum> curriculumPage = curriculumRepo.findBySchoolIdOrderByApplicationYearDesc(actorCampus.getSchool().getId(), pageable);

        PageResponse<Map<String, Object>> pageResponse = PaginationUtil.buildPageResponse(curriculumPage, this::buildCurriculumData);

        return ResponseBuilder.build(HttpStatus.OK, "Hiển thị danh sách khung chương trình thành công", pageResponse);
    }

    private Map<String, Object> buildCurriculumData(Curriculum curriculum) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", curriculum.getId());
        data.put("name", curriculum.getName());
        data.put("subTypeName", CurriculumNamingUtil.extractSubTypeNameFromName(curriculum.getName()));
        data.put("description", curriculum.getDescription());
        data.put("curriculumType", curriculum.getCurriculumType());

        Object rawMethods = curriculum.getLearningMethodList();
        List<Map<String, String>> methodList = new ArrayList<>();

        if (rawMethods instanceof List<?> list) {
            for (Object obj : list) {
                if (obj instanceof String methodStr) {
                    // Nếu là String (do Jackson load từ JSONB)
                    try {
                        LearningMethod m = LearningMethod.valueOf(methodStr);
                        methodList.add(Map.of("code", m.name(), "displayName", m.getDisplayName()));
                    } catch (IllegalArgumentException e) {
                        // Trường hợp data cũ hoặc sai định dạng
                        methodList.add(Map.of("code", methodStr, "displayName", methodStr));
                    }
                } else if (obj instanceof LearningMethod m) {
                    // Nếu đã là Enum (do Hibernate cast sẵn)
                    methodList.add(Map.of("code", m.name(), "displayName", m.getDisplayName()));
                }
            }
        }
        data.put("methodLearnings", methodList);
        data.put("applicationYear", curriculum.getApplicationYear());
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

    @Override
    public ResponseEntity<ResponseObject> upsertProgram(ProgramRequest request) {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Tài khoản của bạn đã bị vô hiệu", null);
        }

        Campus actorCampus = extractActorCampus();

        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Tài khoản cơ sở không tồn tại trong hệ thống", null);
        }

        // actor campus co phai la primary campus ko
        if (!actorCampus.getIsPrimaryBranch()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Chỉ có tài khoản cơ sở chính mới được phép tạo/ cập nhật chương trình", null);
        }

        String error = ProgramValidation.validationUpsertProgram(request, actorCampus, curriculumRepo, programRepo);

        if (error != null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, error, null);
        }

        boolean isNew = request.getProgramId() == null || request.getProgramId() <= 0;
        Program program = isNew ? new Program() : programRepo.findByIdAndCurriculum_School_Id(request.getProgramId(), actorCampus.getSchool().getId());

        if (!isNew && program == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Chương trình của trường không tồn tại trong hệ thống", null);
        }

        Curriculum curriculum = curriculumRepo.findById(request.getCurriculumId()).orElse(null);

        if (curriculum == null || !curriculum.getSchool().getId().equals(actorCampus.getSchool().getId())) {
            return ResponseBuilder.build(
                    HttpStatus.BAD_REQUEST,
                    "Khung chương trình không tồn tại hoặc không thuộc trường của bạn",
                    null
            );
        }

        if (curriculum.getCurriculumStatus() == Status.CUR_ARCHIVED) {
            return ResponseBuilder.build(
                    HttpStatus.BAD_REQUEST,
                    "Không thể sử dụng khung chương trình đã lưu trữ. Vui lòng sử dụng phiên bản đang hoạt động mới nhất",
                    null
            );
        }

        String duplicatedCoreSubject = findDuplicatedCoreSubjectName(request.getExtraSubjectList(), curriculum.getSubjectsJsonb());
        if (duplicatedCoreSubject != null) {
            return ResponseBuilder.build(
                    HttpStatus.BAD_REQUEST,
                    "Môn học bổ sung bị trùng với môn bắt buộc trong khung chương trình: " + duplicatedCoreSubject,
                    null
            );
        }

        program.setCurriculum(curriculum);
        program.setName(normalize(request.getName()));
        program.setLanguageOfInstructionList(request.getLanguageOfInstructionList().stream().map(LanguageInstruction::valueOf).collect(Collectors.toList()));
        program.setGraduationStandard(normalize(request.getGraduationStandard()));
        program.setTargetStudentDescription(normalize(request.getTargetStudentDescription()));

        List<Map<String, Object>> extraSubjects = new ArrayList<>();
        if (request.getExtraSubjectList() != null) {
            extraSubjects = request.getExtraSubjectList().stream()
                    .map(reqSub -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("name", normalize(reqSub.getName()));
                        map.put("description", normalize(reqSub.getDescription()));
                        map.put("isMandatory", Boolean.TRUE.equals(reqSub.getIsMandatory()));
                        return map;
                    })
                    .collect(Collectors.toList());
        }
        program.setExtraSubjectsJsonb(extraSubjects);

        program.setBaseTuitionFee(request.getBaseTuitionFee());
        program.setFeeUnit(FeeUnit.valueOf(request.getFeeUnit()));
        program.setStatus(Status.PRO_DRAFT);

        try {
            programRepo.save(program);
        } catch (DataIntegrityViolationException e) {
            return ResponseBuilder.build(
                    HttpStatus.CONFLICT,
                    "Chuẩn đầu ra đã tồn tại trong khung chương trình này",
                    null
            );
        }

        return ResponseBuilder.build(
                isNew ? HttpStatus.CREATED : HttpStatus.OK,
                isNew ? "Tạo chương trình thành công" : "Cập nhật chương trình thành công",
                null
        );
    }

    @Override
    public ResponseEntity<ResponseObject> extractProgramSubjectsFromExcel(MultipartFile file) {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            List<ProgramRequest.SubjectExtraRequest> subjects = new ArrayList<>();
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                Cell nameCell = row.getCell(0);
                String normalizedName = normalize(getCellValueAsString(nameCell));
                if (StringUtils.isBlank(normalizedName)) continue;

                String normalizedDescription = normalize(getCellValueAsString(row.getCell(1)));
                boolean isMandatory = parseMandatoryCell(row.getCell(2));

                ProgramRequest.SubjectExtraRequest subject = new ProgramRequest.SubjectExtraRequest();
                subject.setName(normalizedName);
                subject.setDescription(normalizedDescription);
                subject.setIsMandatory(isMandatory);
                subjects.add(subject);
            }

            return ResponseBuilder.build(HttpStatus.OK, "Đọc file môn học của chương trình thành công", subjects);
        } catch (Exception e) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Lỗi đọc file Excel: " + e.getMessage(), null);
        }
    }

    private boolean parseMandatoryCell(Cell mandatoryCell) {
        if (mandatoryCell == null) {
            return false;
        }
        if (mandatoryCell.getCellType() == CellType.BOOLEAN) {
            return mandatoryCell.getBooleanCellValue();
        }
        if (mandatoryCell.getCellType() == CellType.STRING) {
            return "true".equalsIgnoreCase(mandatoryCell.getStringCellValue().trim());
        }
        return false;
    }

    private String findDuplicatedCoreSubjectName(List<ProgramRequest.SubjectExtraRequest> extraSubjects, Object curriculumSubjectsRaw) {
        if (extraSubjects == null || extraSubjects.isEmpty() || !(curriculumSubjectsRaw instanceof List<?> curriculumSubjects) || curriculumSubjects.isEmpty()) {
            return null;
        }

        Set<String> mandatoryCoreNames = curriculumSubjects.stream()
                .filter(Map.class::isInstance)
                .map(subject -> (Map<?, ?>) subject)
                .filter(subject -> Boolean.TRUE.equals(subject.get("isMandatory")))
                .map(subject -> normalize(Objects.toString(subject.get("name"), null)))
                .filter(StringUtils::isNotBlank)
                .map(name -> name.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());

        for (ProgramRequest.SubjectExtraRequest extraSubject : extraSubjects) {
            if (extraSubject == null) {
                continue;
            }
            String normalizedExtraName = normalize(extraSubject.getName());
            if (StringUtils.isBlank(normalizedExtraName)) {
                continue;
            }
            if (mandatoryCoreNames.contains(normalizedExtraName.toLowerCase(Locale.ROOT))) {
                return normalizedExtraName;
            }
        }
        return null;
    }

    @Override
    @Transactional
    public ResponseEntity<ResponseObject> cloneProgram(int id) {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Tài khoản của bạn đã bị vô hiệu", null);
        }

        Campus actorCampus = extractActorCampus();

        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Tài khoản cơ sở không tồn tại trong hệ thống", null);
        }

        // actor campus co phai la primary campus ko
        if (!actorCampus.getIsPrimaryBranch()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Chỉ cơ sở chính mới được phép thực hiện thao tác này", null);
        }

        Program oldProgram = programRepo.findById(id).orElse(null);

        if (oldProgram == null) {
            return ResponseBuilder.build(
                    HttpStatus.NOT_FOUND,
                    "Không tìm thấy chương trình gốc trong hệ thống",
                    null
            );
        }

        Program newProgram = new Program();
        newProgram.setName(oldProgram.getName() + " - Bản sao (" + LocalDateTime.now().getYear() + ")");
        newProgram.setCurriculum(oldProgram.getCurriculum());
        //copy danh sách môn học bổ sung
        if (oldProgram.getExtraSubjectsJsonb() != null) {
            // Ép kiểu về List và bọc trong new ArrayList để tách biệt vùng nhớ
            List<?> oldExtraList = (List<?>) oldProgram.getExtraSubjectsJsonb();
            newProgram.setExtraSubjectsJsonb(new ArrayList<>(oldExtraList));
        }

        //copy danh sách ngôn ngữ giảng dạy
        if (oldProgram.getLanguageOfInstructionList() != null) {
            List<?> oldLangList = (List<?>) oldProgram.getLanguageOfInstructionList();
            newProgram.setLanguageOfInstructionList(new ArrayList<>(oldLangList));
        }

        newProgram.setGraduationStandard(oldProgram.getGraduationStandard());
        newProgram.setTargetStudentDescription(oldProgram.getTargetStudentDescription());
        newProgram.setExtraSubjectsJsonb(oldProgram.getExtraSubjectsJsonb());
        newProgram.setBaseTuitionFee(oldProgram.getBaseTuitionFee());
        newProgram.setFeeUnit(oldProgram.getFeeUnit());
        newProgram.setStatus(Status.PRO_DRAFT);

        Program savedProgram = programRepo.save(newProgram);

        return ResponseBuilder.build(
                HttpStatus.CREATED,
                "Đã tạo bản sao thành công. Vui lòng cập nhật thông tin",
                buildProgramData(savedProgram)
        );
    }

    @Override
    @Transactional
    public ResponseEntity<ResponseObject> handleProgramAction(int id, String action) {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Tài khoản của bạn đã bị vô hiệu", null);
        }

        Campus actorCampus = extractActorCampus();

        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Tài khoản cơ sở không tồn tại trong hệ thống", null);
        }

        if (!actorCampus.getIsPrimaryBranch()) {
            return ResponseBuilder.build(
                    HttpStatus.FORBIDDEN,
                    "Chỉ cơ sở chính mới được phép thực hiện thao tác này",
                    null
            );
        }

        Program program = programRepo.findById(id).orElse(null);

        if (program == null) {
            return ResponseBuilder.build(
                    HttpStatus.NOT_FOUND,
                    "Không tìm thấy chương trình",
                    null
            );
        }

        switch (action.toUpperCase()) {

            case "ACTIVATE":

                if (Status.PRO_ACTIVE.equals(program.getStatus())) {
                    return ResponseBuilder.build(
                            HttpStatus.OK,
                            "Chương trình đã ở trạng thái hoạt động",
                            null
                    );
                }

                program.setStatus(Status.PRO_ACTIVE);
                programRepo.save(program);
                return ResponseBuilder.build(
                        HttpStatus.OK,
                        "Kích hoạt chương trình thành công",
                        null
                );

            case "DEACTIVATE":

                if (Status.PRO_INACTIVE.equals(program.getStatus())) {
                    return ResponseBuilder.build(
                            HttpStatus.OK,
                            "Chương trình đã ở trạng thái ngừng hoạt động",
                            null
                    );
                }

                program.setStatus(Status.PRO_INACTIVE);

                List<CampusProgramOffering> activeOfferings =
                        campusProgramOfferingRepo.findByProgramIdAndStatus(id, Status.OPEN);

                for (CampusProgramOffering off : activeOfferings) {
                    off.setStatus(Status.CLOSED); // Chặn người mới nộp vào
                    campusProgramOfferingRepo.save(off);
                }

                programRepo.save(program);
                return ResponseBuilder.build(
                        HttpStatus.OK,
                        "Ngừng hoạt động chương trình thành công",
                        null
                );

            default:
                return ResponseBuilder.build(
                        HttpStatus.BAD_REQUEST,
                        "Hành động không hợp lệ",
                        null
                );
        }

    }

    @Override
    public ResponseEntity<ResponseObject> viewProgramList(int page, int pageSize) {

        Campus actorCampus = extractActorCampus();

        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Tài khoản cơ sở không tồn tại trong hệ thống", null);
        }

        Pageable pageable;
        try {
            pageable = PaginationUtil.buildPageRequest(page, pageSize);
        } catch (IllegalArgumentException e) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, e.getMessage(), null);
        }

        Page<Program> programs = programRepo.findByCurriculum_School_Id(actorCampus.getSchool().getId(), pageable);

        PageResponse<Map<String, Object>> pageResponse = PaginationUtil.buildPageResponse(programs, this::buildProgramData);

        return ResponseBuilder.build(HttpStatus.OK, "Hiển thị danh sách chương trình thành công", pageResponse);
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
        data.put("languageOfInstructionList", program.getLanguageOfInstructionList());
        data.put("graduationStandard", program.getGraduationStandard());
        data.put("targetStudentDescription", program.getTargetStudentDescription());
        data.put("baseTuitionFee", program.getBaseTuitionFee());
        data.put("feeUnit", program.getFeeUnit()); // Rất quan trọng cho FE hiển thị

        List<Map<String, Object>> allSubjects = new ArrayList<>();
        // lấy môn học bắt buộc từ Khung (Chỉ đọc, FE không được sửa)
        if (program.getCurriculum() != null && program.getCurriculum().getSubjectsJsonb() != null) {
            List<Map<String, Object>> coreSubjects = (List<Map<String, Object>>) program.getCurriculum().getSubjectsJsonb();
            coreSubjects.forEach(s -> {
                Map<String, Object> subject = new HashMap<>(s);
                subject.put("origin", "CORE"); //để FE hiển thị (Ví dụ: màu xám, không có nút xóa)
                allSubjects.add(subject);
            });
        }

        //extra môn thêm của Hệ có thể xóa / thêm
        if (program.getExtraSubjectsJsonb() != null) {
            List<Map<String, Object>> extraSubjects = (List<Map<String, Object>>) program.getExtraSubjectsJsonb();
            extraSubjects.forEach(s -> {
                Map<String, Object> subject = new HashMap<>(s);
                subject.put("origin", "EXTRA"); //để FE cho phép xóa/sửa
                allSubjects.add(subject);
            });
        }

        data.put("subjects", allSubjects);
        data.put("status", program.getStatus());

        Curriculum curriculum = program.getCurriculum();
        Map<String, Object> curriculumData = new HashMap<>();
        curriculumData.put("id", curriculum.getId());
        curriculumData.put("name", curriculum.getName());
        curriculumData.put("type", curriculum.getCurriculumType());
        curriculumData.put("applicationYear", curriculum.getApplicationYear());
        curriculumData.put("subjectOptions", curriculum.getSubjectsJsonb());
        curriculumData.put("status", curriculum.getCurriculumStatus());
        curriculumData.put("schoolId", curriculum.getSchool() != null ? curriculum.getSchool().getId() : null);
        data.put("curriculum", curriculumData);

        // --- Thông tin Thống kê & Logic ---
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

        return ResponseBuilder.build(HttpStatus.OK, "Hiển thị danh sách trường thành công", schoolList);
    }

    @Override
    public ResponseEntity<ResponseObject> viewSchoolDetail(int schoolId) {

        School school = schoolRepo.findById(schoolId).orElse(null);

        if (school == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Không tìm thấy trường trong hệ thống", null);
        }

        Set<Integer> favouriteSchoolIds = getFavouriteSchoolIds();

        Map<String, Object> operationConfig = getOperationConfig(schoolId);

        Map<String, Object> data = buildPublicSchoolData(school, favouriteSchoolIds, operationConfig);

        data.put("campusList", school.getCampusList().stream().filter(campus -> Status.ACTIVE.equals(campus.getStatus())).map(this::buildPublicCampusData).toList());

        data.put("curriculumList", school.getCurriculumList().stream().filter(curriculum -> Status.CUR_ACTIVE.equals(curriculum.getCurriculumStatus())).map(this::buildPublicCurriculumData).toList());


        return ResponseBuilder.build(HttpStatus.OK, "Hiển thị chi tiết trường thành công", data);

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
        return schoolConfigRepo.findBySchoolIdAndKey(schoolId, "operationSettingsData").map(config -> (Map<String, Object>) config.getValue()).orElse(new HashMap<>());
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
        data.put("methodLearningList", curriculum.getLearningMethodList());
        data.put("subjectsJsonb", curriculum.getSubjectsJsonb());
        data.put("applicationYear", curriculum.getApplicationYear());
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
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Tài khoản của bạn bị vô hiệu", null);
        }

        Campus actorCampus = extractActorCampus();

        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Tài khoàn cơ sở không tồn tại trong hệ thống", null);
        }

        if (request.getTitle() == null || request.getTitle().isBlank()) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Yêu cầu tiêu đề", null);
        }

        String title = request.getTitle().trim();
        if (title.length() < 5 || title.length() > 255) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Tiêu đề phải có từ 5 đến 255 ký tự", null);
        }

        if (request.getDescription() == null || request.getDescription().isBlank()) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Mô tả là bắt buộc", null);
        }

        String description = request.getDescription().trim();
        if (description.length() < 20 || description.length() > 2000) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Mô tả phải có từ 20 đến 2000 ký tự", null);
        }

        if (request.getBannerUrl() == null || request.getBannerUrl().isBlank()) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Ảnh banner là bắt buộc", null);
        }

        String bannerUrl = request.getBannerUrl().trim();
        if (bannerUrl.length() > 1000) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Đường dẫn ảnh banner không được vượt quá 1000 ký tự", null);
        }

        if (!(bannerUrl.startsWith("http://") || bannerUrl.startsWith("https://"))) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Đường dẫn ảnh banner không hợp lệ", null);
        }

        if (request.getEventDate() == null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Ngày tổ chức sự kiện là bắt buộc", null);
        }

        if (request.getEventDate().isBefore(LocalDate.now())) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Ngày tổ chức sự kiện phải là hôm nay hoặc trong tương lai", null);
        }

        if (request.getStartTime() == null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Thời gian bắt đầu là bắt buộc", null);
        }

        if (request.getEndTime() == null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Thời gian kết thúc là bắt buộc", null);
        }

        if (request.getCampusId() <= 0) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Mã cơ sở phải lớn hơn 0", null);
        }

        if (!request.getStartTime().isBefore(request.getEndTime())) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Thời gian bắt đầu phải sớm hơn thời gian kết thúc", null);
        }

        if (request.getEventDate().isEqual(LocalDate.now()) && request.getStartTime().isBefore(LocalTime.now())) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Thời gian bắt đầu phải muộn hơn thời điểm hiện tại nếu sự kiện diễn ra trong ngày hôm nay", null);
        }

        if (Duration.between(request.getStartTime(), request.getEndTime()).toMinutes() < 30) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Thời lượng sự kiện phải tối thiểu 30 phút", null);
        }

        boolean isConflict = openDayEventRepo.existsByCampusIdAndEventDateAndStartTimeLessThanAndEndTimeGreaterThan(
                actorCampus.getId(),
                request.getEventDate(),
                request.getEndTime(),
                request.getStartTime()
        );

        if (isConflict) {
            return ResponseBuilder.build(HttpStatus.CONFLICT, "Thời gian tổ chức sự kiện bị trùng với một sự kiện khác tại cơ sở này", null);
        }

        OpenDayEvent openDayEvent = openDayEventRepo.save(
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

        return ResponseBuilder.build(HttpStatus.OK, "Tạo sự kiện Open Day thành công", buildOpenDayEvent(openDayEvent));
    }

    @Override
    public ResponseEntity<ResponseObject> viewOpenDayEventList(int currentPage, int pageSize) {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Tài khoản của bạn bị vô hiệu", null);
        }

        Campus actorCampus = extractActorCampus();
        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Tài khoàn cơ sở không tồn tại trong hệ thống", null);
        }

        Pageable pageable;
        try {
            pageable = PaginationUtil.buildPageRequest(currentPage, pageSize);
        } catch (IllegalArgumentException e) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, e.getMessage(), null);
        }

        Page<OpenDayEvent> openDayEventPage = openDayEventRepo.findByCampusId(actorCampus.getId(), pageable);

        PageResponse<Map<String, Object>> pageResponse = PaginationUtil.buildPageResponse(openDayEventPage, this::buildOpenDayEvent);

        return ResponseBuilder.build(HttpStatus.OK, "Hiển thị danh sách sự kiện ", pageResponse);
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
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Tài khoàn cơ sở không tồn tại trong hệ thống", null);
        }

        // actor campus co phai la primary campus ko
        if (!actorCampus.getIsPrimaryBranch()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Chỉ cơ sở chính mới được phép thực hiện thao tác này", null);
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

        if (actorCampus == null)
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Tài khoàn cơ sở không tồn tại trong hệ thống", null);

        if (!actorCampus.getIsPrimaryBranch()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Chỉ cơ sở chính mới được phép thực hiện thao tác này", null);
        }

        School school = actorCampus.getSchool();

        if (request == null || request.getPackageId() <= 0) {
            return ResponseBuilder.build(
                    HttpStatus.BAD_REQUEST,
                    "Vui lòng cung cấp mã gói dịch vụ hợp lệ",
                    null
            );
        }

        // step 2: lấy thông tin của gói cước
        Subscription subscription = subscriptionRepo.findById(request.getPackageId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy gói dịch vụ trong hệ thống"));

        if (subscription.getFinalPrice() == null || subscription.getFinalPrice().compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseBuilder.build(
                    HttpStatus.BAD_REQUEST,
                    "Tổng tiền gói dịch vụ không hợp lệ",
                    null
            );
        }

        //ktra upgrade vs renew
        List<SchoolSubscription> currentActiveSub =
                schoolSubscriptionRepo.findBySchoolIdAndIsSelected(school.getId(), true);

        LocalDate calculatedStartDate = LocalDate.now();
        LocalDate calculatedEndDate = calculatedStartDate.plusDays(subscription.getDurationDays());
        String orderNote = "Thanh toán gói " + normalize(subscription.getName());
        BigDecimal amountToCharge = subscription.getFinalPrice();

        if (!currentActiveSub.isEmpty()) {
            // Lấy gói có ngày kết thúc xa nhất trong đám đang active để tính nối đuôi
            SchoolSubscription current = currentActiveSub.stream()
                    .max(Comparator.comparing(SchoolSubscription::getEndDate))
                    .get();

            if (current.getSubscription().getId().equals(request.getPackageId())) {
                // Nếu GIA HẠN (Renew) - Cùng loại gói
                LocalDate baseDate = LocalDate.now().isAfter(current.getEndDate()) ? LocalDate.now() : current.getEndDate();
                calculatedStartDate = baseDate;
                calculatedEndDate = baseDate.plusDays(subscription.getDurationDays());
                amountToCharge = subscription.getFinalPrice();
                orderNote = "Gia hạn gói " + normalize(subscription.getName()) + " từ ngày " + calculatedStartDate;
            } else {
                // Nếu NÂNG CẤP (Upgrade) - Khác loại gói
                long remainingDays = ChronoUnit.DAYS.between(LocalDate.now(), current.getEndDate());
                if (remainingDays <= 0) {
                    return ResponseBuilder.build(
                            HttpStatus.BAD_REQUEST,
                            "Gói hiện tại đã hết hạn, vui lòng gia hạn hoặc mua gói mới.",
                            null
                    );
                }

                Integer currentDurationDays = current.getSubscription().getDurationDays();
                Integer targetDurationDays = subscription.getDurationDays();
                if (currentDurationDays == null || currentDurationDays <= 0
                        || targetDurationDays == null || targetDurationDays <= 0) {
                    return ResponseBuilder.build(
                            HttpStatus.BAD_REQUEST,
                            "Gói dịch vụ thiếu cấu hình thời hạn.",
                            null
                    );
                }

                boolean isTrial = current.getSubscription().getPackageType() == PackageType.TRIAL;
                BigDecimal currentPrice = isTrial ? BigDecimal.ZERO : current.getSubscription().getPrice();
                BigDecimal targetPrice = subscription.getPrice();
                if (targetPrice == null || (!isTrial && currentPrice == null)) {
                    return ResponseBuilder.build(
                            HttpStatus.BAD_REQUEST,
                            "Giá gói không hợp lệ để nâng cấp.",
                            null
                    );
                }

                BigDecimal remainingDaysDecimal = BigDecimal.valueOf(remainingDays);
                BigDecimal currentDailyPrice = currentPrice.divide(BigDecimal.valueOf(currentDurationDays), 6, RoundingMode.HALF_UP);
                BigDecimal targetDailyPrice = targetPrice.divide(BigDecimal.valueOf(targetDurationDays), 6, RoundingMode.HALF_UP);
                BigDecimal creditOld = currentDailyPrice.multiply(remainingDaysDecimal).setScale(0, RoundingMode.HALF_UP);
                BigDecimal chargeNew = targetDailyPrice.multiply(remainingDaysDecimal).setScale(0, RoundingMode.HALF_UP);
                BigDecimal netAmount = chargeNew.subtract(creditOld);
                if (netAmount.compareTo(BigDecimal.ZERO) < 0) {
                    return ResponseBuilder.build(
                            HttpStatus.BAD_REQUEST,
                            "Không thể hạ cấp gói trong thao tác nâng cấp.",
                            null
                    );
                }

                ConfigSystemUtil.SubscriptionPriceBreakdown breakdown = calculateBreakdownFromNet(netAmount);
                amountToCharge = breakdown.finalPrice();
                calculatedStartDate = LocalDate.now();
                calculatedEndDate = current.getEndDate(); // giữ nguyên hạn cũ theo policy preview
                orderNote = "Nâng cấp lên gói " + normalize(subscription.getName()) + " theo proration";
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
                .endDate(calculatedEndDate)
                .isSelected(false) // chưa kích hoạt cho đến khi thanh toán xong
                .licenseKey(licenseKey)
                .build();

        schoolSubscription = schoolSubscriptionRepo.save(schoolSubscription);

// step 4 : cấu hình vnpay
        String vnp_TxnRef = generateUniqueVnpTxnRef(); // mã đơn hàng
        long amount = amountToCharge
                .multiply(BigDecimal.valueOf(100)) // VNPay yêu cầu nhân 100
                .setScale(0, RoundingMode.HALF_UP)
                .longValue();

        if (amount <= 0) {
            return ResponseBuilder.build(
                    HttpStatus.BAD_REQUEST,
                    "Số tiền thanh toán không hợp lệ",
                    null
            );
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

        log.info("VNPay request prepared: tmnCode={}, returnUrl={}, txnRef={}, amount={}, version={}", VNPayConfig.vnp_TmnCode, VNPayConfig.vnp_ReturnUrl, vnp_TxnRef, amount, vnp_Params.get("vnp_Version"));
        log.debug("VNPay hashData={}", hashData);
        log.debug("VNPay secureHash={}", vnp_SecureHash);

        paymentTransactionRepo.save(PaymentTransaction.builder().school(school).schoolSubscription(schoolSubscription).vnpTxnRef(vnp_TxnRef).vnpAmount(amount).vnpOrderInfo(orderNote).status(Status.PAYMENT_PENDING).createdAt(LocalDateTime.now()).ipAddress(VNPayConfig.getIpAddress(httpRequest)).build());

        return ResponseBuilder.build(HttpStatus.OK, "Payment URL tạo thành công", paymentUrl);
    }

    private String generateUniqueVnpTxnRef() {
        final int maxAttempts = 10;
        for (int i = 0; i < maxAttempts; i++) {
            String txnRef = VNPayConfig.getRandomNumber(8);
            if (!paymentTransactionRepo.existsByVnpTxnRef(txnRef)) {
                return txnRef;
            }
        }
        throw new RuntimeException("Không thể tạo mã giao dịch thanh toán duy nhất.");
    }

    @Override
    @Transactional
    public ResponseEntity<ResponseObject> handleVNPayCallback(HttpServletRequest request) {

        Map<String, String> vnp_Params = new HashMap<>();

        Map<String, String[]> requestParams = request.getParameterMap();

        for (String paramName : requestParams.keySet()) {
            vnp_Params.put(paramName, request.getParameter(paramName));
        }

        String vnp_SecureHash = vnp_Params.get("vnp_SecureHash");
        if (vnp_SecureHash == null || vnp_SecureHash.isBlank()) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Thiếu vnp_SecureHash", null);
        }

        vnp_Params.remove("vnp_SecureHash");
        vnp_Params.remove("vnp_SecureHashType");

        Map<String, String> sortedFields = new TreeMap<>(vnp_Params);
        String hashData = buildVnpHashDataFromRawQuery(request.getQueryString());
        if (hashData.isBlank()) {
            // Fallback cho các case test nội bộ không truyền raw query string đầy đủ.
            hashData = buildVnpHashData(sortedFields);
        }
        String checkSum = VNPayConfig.hmacSHA512(VNPayConfig.vnp_HashSecret, hashData);

        if (checkSum.equalsIgnoreCase(vnp_SecureHash.trim())) {
            String vnp_ResponseCode = vnp_Params.get("vnp_ResponseCode");
            String vnp_TxnRef = vnp_Params.get("vnp_TxnRef");
            String vnpAmountRaw = vnp_Params.get("vnp_Amount");

            if (vnp_TxnRef == null || vnp_TxnRef.isBlank()) {
                return ResponseBuilder.build(
                        HttpStatus.BAD_REQUEST,
                        "Thiếu mã tham chiếu giao dịch",
                        null
                );
            }

            long callbackAmount;
            try {
                callbackAmount = Long.parseLong(vnpAmountRaw);
            } catch (Exception ex) {
                return ResponseBuilder.build(
                        HttpStatus.BAD_REQUEST,
                        "Số tiền VNPay trả về không hợp lệ",
                        null
                );
            }

            // 4. Tìm giao dịch trong hệ thống
            PaymentTransaction transaction =
                    paymentTransactionRepo.findByVnpTxnRef(vnp_TxnRef).orElse(null);

            if (transaction == null) {
                return ResponseBuilder.build(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy giao dịch",
                        null
                );
            }

            // Kiểm tra xem giao dịch này đã được xử lý trước đó chưa (tránh IPN gọi trùng)
            if (transaction.getStatus() != Status.PAYMENT_PENDING) {
                return ResponseBuilder.build(
                        HttpStatus.OK,
                        "Giao dịch đã được xử lý trước đó",
                        null
                );
            }

            if (transaction.getVnpAmount() == null || transaction.getVnpAmount() != callbackAmount) {
                transaction.setStatus(Status.PAYMENT_FAILED);
                transaction.setUpdatedAt(LocalDateTime.now());
                paymentTransactionRepo.save(transaction);
                return ResponseBuilder.build(
                        HttpStatus.BAD_REQUEST,
                        "Số tiền thanh toán không khớp với giao dịch",
                        null
                );
            }

            if ("00".equals(vnp_ResponseCode)) {
                // Cập nhật giao dịch
                transaction.setStatus(Status.PAYMENT_SUCCESS);
                transaction.setUpdatedAt(LocalDateTime.now());
                paymentTransactionRepo.save(transaction);

                //==> sau khi thành công trả về kết quả kích hoạt gói SchoolSubscription
                SchoolSubscription schoolSub = transaction.getSchoolSubscription();

                //Hủy kích hoạt tất cả các gói isSelected=true hiện tại của School này
                activateSchoolSubscription(schoolSub);

                log.info("Payment success for txnRef: {}", vnp_TxnRef);
                return ResponseBuilder.build(
                        HttpStatus.OK,
                        "Kích hoạt gói dịch vụ thành công",
                        null
                );
            } else {
                // thanh toán thất bại (Người dùng hủy hoặc lỗi thẻ)
                transaction.setStatus(Status.PAYMENT_FAILED);
                transaction.setUpdatedAt(LocalDateTime.now());
                paymentTransactionRepo.save(transaction);

                return ResponseBuilder.build(
                        HttpStatus.BAD_REQUEST,
                        "Thanh toán thất bại. Mã lỗi: " + vnp_ResponseCode,
                        null
                );
            }
        } else {
            log.warn("VNPay signature mismatch for txnRef={}, expected={}, actual={}, hashData={}", vnp_Params.get("vnp_TxnRef"), checkSum, vnp_SecureHash, hashData);
            return ResponseBuilder.build(
                    HttpStatus.BAD_REQUEST,
                    "Chữ ký không hợp lệ",
                    null
            );
        }
    }

    private void activateSchoolSubscription(SchoolSubscription newSubscription) {
        Integer schoolId = newSubscription.getSchool().getId();

        //tìm tất cả các gói đang được active (isSelected = true) của trường này
        List<SchoolSubscription> activeSubs = schoolSubscriptionRepo.findBySchoolIdAndIsSelected(
                schoolId,
                true);

        //chuyển tất cả về false
        if (!activeSubs.isEmpty()) {
            activeSubs.forEach(sub -> sub.setIsSelected(false));
            schoolSubscriptionRepo.saveAll(activeSubs);
        }

        //kích hoạt gói mới
        newSubscription.setIsSelected(true);
        schoolSubscriptionRepo.save(newSubscription);

        // GỌI LOGIC PHÂN BỔ HẠN NGẠCH (QUOTA)
        //Lấy thông tin gói cước thực tế từ Subscription
        Subscription packageInfo = newSubscription.getSubscription();
        distributeResourceQuotas(schoolId, packageInfo);

        log.info("School ID {} activated package: {} (License: {})", schoolId, newSubscription.getSubscription().getName(), newSubscription.getLicenseKey());
    }

    private void distributeResourceQuotas(Integer schoolId, Subscription packageBought) {
        Map<String, Object> features = (Map<String, Object>) packageBought.getFeatures();
        if (features == null) return;

        // Lấy số lượng Counsellor tối đa từ gói (Ví dụ key là "maxCounsellors")
        Object maxCounsellorsObj = features.get("maxCounsellors");
        int totalMaxCounsellors = (maxCounsellorsObj instanceof Number) ? ((Number) maxCounsellorsObj).intValue() : 0;

        // Lấy danh sách tất cả Campus thuộc School này
        List<Campus> campuses = campusRepo.findAllBySchoolId(schoolId);
        if (campuses.isEmpty()) return;

        // Chiến thuật: Chia đều cho các Campus
        int quotaPerCampus = totalMaxCounsellors / campuses.size();

        // Nếu chia có dư (ví dụ 10 slot cho 3 campus), slot dư có thể cộng vào Campus chính
        int remainder = totalMaxCounsellors % campuses.size();

        for (int i = 0; i < campuses.size(); i++) {
            Campus campus = campuses.get(i);

            // Tìm bản ghi quota hiện tại hoặc tạo mới
            CampusResourceQuota quota = campusResourceQuotaRepo
                    .findByCampusIdAndResourceType(campus.getId(), ResourceType.COUNSELLOR)
                    .orElseGet(() -> CampusResourceQuota.builder()
                            .campus(campus)
                            .resourceType(ResourceType.COUNSELLOR)
                            .build());

            // Chỉ campus chính mới được nhận thêm phần dư 'remainder'
            // Gán hạn ngạch mới
            int finalQuota = quotaPerCampus + (campus.getIsPrimaryBranch() ? remainder : 0);
            quota.setMaxQuota(finalQuota);

            campusResourceQuotaRepo.save(quota);
        }

        log.info("Distributed {} counsellors to {} campuses for School ID {}", totalMaxCounsellors, campuses.size(), schoolId);
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

    private String buildVnpHashDataFromRawQuery(String queryString) {
        if (queryString == null || queryString.isBlank()) {
            return "";
        }

        Map<String, String> sortedFields = new TreeMap<>();
        String[] rawPairs = queryString.split("&");

        for (String rawPair : rawPairs) {
            if (rawPair == null || rawPair.isBlank()) {
                continue;
            }

            int separatorIndex = rawPair.indexOf('=');
            String key = separatorIndex >= 0 ? rawPair.substring(0, separatorIndex) : rawPair;
            String value = separatorIndex >= 0 ? rawPair.substring(separatorIndex + 1) : "";

            if (!key.startsWith("vnp_")) {
                continue;
            }

            if ("vnp_SecureHash".equals(key) || "vnp_SecureHashType".equals(key)) {
                continue;
            }

            if (value.isBlank()) {
                continue;
            }

            sortedFields.put(key, value);
        }

        StringBuilder hashData = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : sortedFields.entrySet()) {
            if (!first) {
                hashData.append('&');
            }
            hashData.append(entry.getKey()).append('=').append(entry.getValue());
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
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Tài khoản cơ sở không tồn tại trong hệ thống", null);
        }

        if (!actorCampus.getIsPrimaryBranch()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Chỉ cơ sở chính mới được phép thực hiện thao tác này", null);
        }

        School school = actorCampus.getSchool();

        Optional<SchoolSubscription> activeSubOpt = schoolSubscriptionRepo.findBySchoolIdAndIsSelected(school.getId(), true).stream().findFirst(); // tại 1 thời điểm chỉ có 1 gói đc active

        if (activeSubOpt.isEmpty()) {
            return ResponseBuilder.build(
                    HttpStatus.OK,
                    "Không có gói dịch vụ đang hoạt động",
                    null
            );
        }

        Map<String, Object> data = buildCurrentSubscription(activeSubOpt.get());

        return ResponseBuilder.build(
                HttpStatus.OK,
                "Lấy thông tin gói dịch vụ hiện tại thành công",
                data
        );
    }

    private Map<String, Object> buildCurrentSubscription(SchoolSubscription schoolSub) {
        // tính số ngày còn lại
        long daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), schoolSub.getEndDate());

        boolean isExpired = LocalDate.now().isAfter(schoolSub.getEndDate());

        // 1. Lấy thông tin Features từ gói cước
        Map<String, Object> features = (Map<String, Object>) schoolSub.getSubscription().getFeatures();
        Map<String, Object> entitlements = extractFeatures(features);

        int totalCounsellorsInPackage = ((Number) features.getOrDefault("maxCounsellors", 0)).intValue();
        int postLimit = ((Number) features.getOrDefault("postLimit", 0)).intValue();

        Campus actorCampus = extractActorCampus();

        // Quota (Hạn mức được chia)
        var quotaOpt = campusResourceQuotaRepo.findByCampusIdAndResourceType(actorCampus.getId(), ResourceType.COUNSELLOR);
        int myQuota = quotaOpt.map(CampusResourceQuota::getMaxQuota).orElse(0);

        // Usage (Số lượng đã dùng thực tế)
        long counsellorUsage = accountRepo.countByCampusIdAndRole(actorCampus.getId(), Role.COUNSELLOR);
        long postUsage = postRepo.countByAuthor_Campus_Id(actorCampus.getId());

        int totalAllocatedToOthers = Math.max(0, totalCounsellorsInPackage - myQuota);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("packageName", schoolSub.getSubscription().getName());
        data.put("licenseKey", schoolSub.getLicenseKey());
        data.put("startDate", schoolSub.getStartDate());
        data.put("endDate", schoolSub.getEndDate());
        data.put("dasRemaining", Math.max(0, daysRemaining));
        data.put("isExpired", isExpired);

        // --- CHI TIẾT QUYỀN LỢI ---
        data.put("entitlements", entitlements);

        Map<String, Object> usageReport = new LinkedHashMap<>();
        // Thống kê Counsellor
        Map<String, Object> counsellorStats = new LinkedHashMap<>();
        counsellorStats.put("totalPackage", totalCounsellorsInPackage);
        counsellorStats.put("myCampusQuota", myQuota);
        counsellorStats.put("myCampusUsage", counsellorUsage);
        counsellorStats.put("otherCampusesQuota", totalAllocatedToOthers);
        usageReport.put("counsellors", counsellorStats);

        // Thống kê Post
        Map<String, Object> postStats = new LinkedHashMap<>();
        postStats.put("limit", postLimit == -1 ? "Vô hạn" : postLimit);
        postStats.put("usage", postUsage);
        postStats.put("remaining", postLimit == -1 ? "Vô hạn" : Math.max(0, postLimit - postUsage));
        usageReport.put("posts", postStats);

        data.put("usageReport", usageReport);

        // --- PHẦN BỔ SUNG TRỌNG ĐIỂM ĐỐI ĐỐI VS CAMPUS CHÍNH ---
        Map<String, Object> resourceSummary = new LinkedHashMap<>();
        resourceSummary.put("totalPackageQuota", totalCounsellorsInPackage);
        resourceSummary.put("myCampusQuota", myQuota);
        resourceSummary.put("otherCampusesQuota", Math.max(0, totalAllocatedToOthers));
        data.put("resourceSummary", resourceSummary);

        // ---------------STATUS HIÊNT THỊ---------------//
        data.put("statusMessage", isExpired ? "Đã hết hạn" : "Đang hoạt động (còn " + daysRemaining + " ngày)");
        data.put("suggestion", generateSmartSuggestion(isExpired, daysRemaining, postLimit, postUsage));
        return data;
    }

    private String generateSmartSuggestion(boolean isExpired, long daysLeft, int postLimit, long postUsage) {
        if (isExpired) return "Gói dịch vụ đã hết hạn. Vui lòng mua gói mới để tiếp tục sử dụng.";

        if (daysLeft <= 7)
            return "Gói của bạn chỉ còn " + daysLeft + " ngày. Hãy gia hạn sớm để tránh gián đoạn dịch vụ.";

        // Nếu sắp hết dung lượng bài đăng (trên 90%)
        if (postLimit != -1 && (double) postUsage / postLimit > 0.9) {
            return "Bạn đã sử dụng gần hết hạn mức bài đăng. Hãy cân nhắc nâng cấp gói.";
        }

        return "Gói dịch vụ đang hoạt động tốt. Bạn có thể cộng dồn thời gian bằng cách mua gói cùng loại.";
    }

    private Map<String, Object> extractFeatures(Map<String, Object> rawFeatures) {

        if (rawFeatures == null) return new HashMap<>();

        Map<String, Object> extracted = new LinkedHashMap<>();
        String[] keys = {
                "maxCounsellors",
                "postLimit",
                "hasAiAssistant",
                "supportLevel",
                "topRanking",
                "parentPostPermission",
                "isFeatured"
        };

        for (String key : keys) {
            Object value = rawFeatures.get(key);
            if ("postLimit".equals(key) && Integer.valueOf(-1).equals(value)) {
                extracted.put(key, "Vô hạn");
            } else {
                extracted.put(key, value != null ? value : 0);
            }
        }
        return extracted;
    }

    @Override
    public ResponseEntity<ResponseObject> previewSubscription(SubscriptionPreviewRequest request) {

        Campus actorCampus = extractActorCampus();

        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Tài khoản cơ sở không tồn tại trong hệ thống", null);
        }

        if (!actorCampus.getIsPrimaryBranch()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Chỉ cơ sở chính mới được phép thực hiện thao tác này", null);
        }

        //lấy gói đang active
        SchoolSubscription activeSub = schoolSubscriptionRepo.findBySchoolIdAndIsSelected(actorCampus.getSchool().getId(), true)
                .stream().findFirst().orElse(null);

        if (activeSub == null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Không có gói dịch vụ nào đang hoạt động.", null);
        }

        try {

            // parse action 
            if (request == null || request.getActionType() == null || request.getActionType().isBlank()) {
                throw new RuntimeException("Thiếu loại hành động (actionType).");
            }

            SubscriptionAction action = SubscriptionAction.valueOf(request.getActionType().trim().toUpperCase());

            // netAmount là tiền chênh lệch gốc 
            BigDecimal netAmount = BigDecimal.ZERO;
            Map<String, Object> warnings = new LinkedHashMap<>();
            Map<String, Object> currentDetails = new LinkedHashMap<>();
            Map<String, Object> targetDetails = new LinkedHashMap<>();

            switch (action) {
                case UPGRADE -> {

                    if (request.getTargetPackageId() == null) {
                        throw new RuntimeException("Thiếu gói đích để nâng cấp.");
                    }

                    Subscription targetSub = subscriptionRepo.findById(request.getTargetPackageId())
                            .orElseThrow(() -> new RuntimeException("Gói đích không tồn tại"));

                    if (targetSub.getId().equals(activeSub.getSubscription().getId())) {
                        throw new RuntimeException("Gói đích phải khác gói hiện tại.");
                    }

                    // ktra gói hiện tại có phải là gói dùng thử không
                    boolean isTrial = activeSub.getSubscription().getPackageType() == PackageType.TRIAL;

                    // Policy A: prorate theo thời gian còn lại và giữ nguyên ngày hết hạn hiện tại
                    long remainingDays = ChronoUnit.DAYS.between(LocalDate.now(), activeSub.getEndDate());
                    if (remainingDays <= 0) {
                        throw new RuntimeException("Gói hiện tại đã hết hạn, vui lòng gia hạn để tiếp tục.");
                    }

                    Integer currentDurationDays = activeSub.getSubscription().getDurationDays();
                    Integer targetDurationDays = targetSub.getDurationDays();
                    if (currentDurationDays == null || currentDurationDays <= 0
                            || targetDurationDays == null || targetDurationDays <= 0) {
                        throw new RuntimeException("Gói dịch vụ thiếu cấu hình thời hạn (durationDays).");
                    }

                    BigDecimal remainingDaysDecimal = BigDecimal.valueOf(remainingDays);

                    // netAmount luôn là giá gốc trước thuế/phí để tránh tính chồng
                    BigDecimal currentVal = isTrial ? BigDecimal.ZERO : activeSub.getSubscription().getPrice();
                    BigDecimal targetVal = targetSub.getPrice();
                    if (targetVal == null || (!isTrial && currentVal == null)) {
                        throw new RuntimeException("Giá gói không hợp lệ để tính preview.");
                    }

                    BigDecimal currentDailyPrice = currentVal.divide(BigDecimal.valueOf(currentDurationDays), 6, RoundingMode.HALF_UP);
                    BigDecimal targetDailyPrice = targetVal.divide(BigDecimal.valueOf(targetDurationDays), 6, RoundingMode.HALF_UP);

                    BigDecimal creditOld = currentDailyPrice.multiply(remainingDaysDecimal).setScale(0, RoundingMode.HALF_UP);
                    BigDecimal chargeNew = targetDailyPrice.multiply(remainingDaysDecimal).setScale(0, RoundingMode.HALF_UP);
                    netAmount = chargeNew.subtract(creditOld);

                    if (netAmount.compareTo(BigDecimal.ZERO) < 0) {
                        throw new RuntimeException("Không thể hạ cấp gói trong thao tác nâng cấp.");
                    }

                    currentDetails.put("packageName", activeSub.getSubscription().getName());
                    currentDetails.put("price", currentVal);
                    currentDetails.put("durationDays", currentDurationDays);
                    currentDetails.put("expiryDate", activeSub.getEndDate());

                    targetDetails.put("packageName", targetSub.getName());
                    targetDetails.put("price", targetVal);
                    targetDetails.put("durationDays", targetDurationDays);
                    targetDetails.put("remainingDays", remainingDays);
                    targetDetails.put("creditOld", creditOld);
                    targetDetails.put("chargeNew", chargeNew);
                    targetDetails.put("newExpiryDate", activeSub.getEndDate());
                    warnings.put("message", "Nâng cấp áp dụng theo proration phần thời gian còn lại và giữ nguyên ngày hết hạn hiện tại.");
                    warnings.put("pricingNote", "Tiền chênh lệch được tính từ giá gốc trước thuế/phí, sau đó mới cộng Service Fee và VAT.");
                }

                //netAmount = giá_gói_hiện_tại
                case RENEW -> {
                    netAmount = activeSub.getSubscription().getPrice();
                    if (netAmount == null) {
                        throw new RuntimeException("Giá gói hiện tại không hợp lệ để gia hạn.");
                    }
                    Integer durationDays = activeSub.getSubscription().getDurationDays();
                    if (durationDays == null || durationDays <= 0) {
                        throw new RuntimeException("Gói hiện tại thiếu cấu hình thời hạn (durationDays).");
                    }
                    LocalDate baseDate = LocalDate.now().isAfter(activeSub.getEndDate()) ? LocalDate.now() : activeSub.getEndDate();
                    currentDetails.put("packageName", activeSub.getSubscription().getName());
                    currentDetails.put("price", netAmount);
                    currentDetails.put("expiryDate", activeSub.getEndDate());
                    targetDetails.put("packageName", activeSub.getSubscription().getName());
                    targetDetails.put("baseDate", baseDate);
                    targetDetails.put("newExpiryDate", baseDate.plusDays(durationDays));
                    warnings.put("message", "Gia hạn giúp cộng dồn thời gian sử dụng.");
                }

            }

            // Step 5: Build financial breakdown and response
            var breakdown = calculateBreakdownFromNet(netAmount);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("action", action);
            response.put("netAmount", netAmount);
            response.put("breakdown", breakdown);
            response.put("current", currentDetails);
            response.put("target", targetDetails);
            response.put("warnings", warnings);

            return ResponseBuilder.build(HttpStatus.OK, "Xem trước thay đổi gói thành công", response);
        } catch (Exception e) {
            String message = e.getMessage() != null ? e.getMessage() : "Dữ liệu preview không hợp lệ.";
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, message, null);
        }
    }

    private ConfigSystemUtil.SubscriptionPriceBreakdown calculateBreakdownFromNet(BigDecimal netAmount) {
        BigDecimal serviceRate = resolveBusinessRate("serviceRate");
        BigDecimal taxRate = resolveBusinessRate("taxRate");

        BigDecimal normalizedNet = netAmount == null ? BigDecimal.ZERO : netAmount.max(BigDecimal.ZERO);
        BigDecimal serviceFee = normalizedNet.multiply(serviceRate).setScale(0, RoundingMode.HALF_UP);
        BigDecimal taxFee = normalizedNet.add(serviceFee).multiply(taxRate).setScale(0, RoundingMode.HALF_UP);

        BigDecimal finalPrice = normalizedNet.add(serviceFee).add(taxFee);
        finalPrice = finalPrice
                .divide(new BigDecimal("1000"), 0, RoundingMode.CEILING)
                .multiply(new BigDecimal("1000"));

        return new ConfigSystemUtil.SubscriptionPriceBreakdown(normalizedNet, serviceFee, taxFee, finalPrice);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {
        if (value instanceof Map<?, ?> raw) {
            return (Map<String, Object>) raw;
        }
        return Collections.emptyMap();
    }

    private Map<String, Object> getBusinessMap() {
        PlatformConfig config = platformConfigRepo.findByKey("business").orElse(null);
        if (config == null || config.getValue() == null) {
            throw new RuntimeException("Chưa cấu hình business.");
        }
        return asMap(config.getValue());
    }

    private BigDecimal resolveBusinessRate(String key) {
        Map<String, Object> business = getBusinessMap();
        Object rawValue = business.get(key);
        if (!(rawValue instanceof Number number)) {
            throw new RuntimeException("Thiếu cấu hình " + key + ".");
        }
        return BigDecimal.valueOf(number.doubleValue());
    }

}