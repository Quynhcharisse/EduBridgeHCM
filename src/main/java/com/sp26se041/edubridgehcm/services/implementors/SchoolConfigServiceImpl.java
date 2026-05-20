package com.sp26se041.edubridgehcm.services.implementors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sp26se041.edubridgehcm.enums.BoardingType;
import com.sp26se041.edubridgehcm.enums.CategoryTemplate;
import com.sp26se041.edubridgehcm.enums.ResourceType;
import com.sp26se041.edubridgehcm.enums.Role;
import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.models.Account;
import com.sp26se041.edubridgehcm.models.Campus;
import com.sp26se041.edubridgehcm.models.CampusResourceQuota;
import com.sp26se041.edubridgehcm.models.Curriculum;
import com.sp26se041.edubridgehcm.models.PlatformConfig;
import com.sp26se041.edubridgehcm.models.Program;
import com.sp26se041.edubridgehcm.models.School;
import com.sp26se041.edubridgehcm.models.SchoolConfig;
import com.sp26se041.edubridgehcm.models.SchoolSubscription;
import com.sp26se041.edubridgehcm.models.TemplateDocx;
import com.sp26se041.edubridgehcm.repositories.CampusRepo;
import com.sp26se041.edubridgehcm.repositories.CampusResourceQuotaRepo;
import com.sp26se041.edubridgehcm.repositories.PlatformConfigRepo;
import com.sp26se041.edubridgehcm.repositories.SchoolConfigRepo;
import com.sp26se041.edubridgehcm.repositories.SchoolRepo;
import com.sp26se041.edubridgehcm.repositories.SchoolSubscriptionRepo;
import com.sp26se041.edubridgehcm.repositories.TemplateDocxRepo;
import com.sp26se041.edubridgehcm.requests.SchoolConfigRequest;
import com.sp26se041.edubridgehcm.requests.UpsertServicePackageFeeRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.SchoolConfigService;
import com.sp26se041.edubridgehcm.services.SupabaseStorageService;
import com.sp26se041.edubridgehcm.utils.AuthRequestUtil;
import com.sp26se041.edubridgehcm.utils.ResponseBuilder;
import com.sp26se041.edubridgehcm.utils.WorkShiftConfigValidator;
import com.sp26se041.edubridgehcm.validations.school.CampusValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SchoolConfigServiceImpl implements SchoolConfigService {

    @Value("${AI_SERVICE_N8N}")
    private String n8nUrl;

    private final SchoolConfigRepo schoolConfigRepo;

    private final CampusRepo campusRepo;

    private final SchoolSubscriptionRepo schoolSubscriptionRepo;

    private final ObjectMapper objectMapper;

    private final CampusResourceQuotaRepo campusResourceQuotaRepo;

    private final SchoolRepo schoolRepo;

    private final PlatformConfigRepo platformConfigRepo;

    private final TemplateDocxRepo templateDocxRepo;

    private final SupabaseStorageService supabaseStorageService;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    @Transactional
    public ResponseEntity<ResponseObject> updateSchoolConfig(int schoolId, SchoolConfigRequest request) {

        Campus actorCampus = extractActorCampus();

        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản cơ sở trường học", null);
        }

        if (!actorCampus.getIsPrimaryBranch()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN,
                    "Chỉ cơ sở chính mới có quyền thay đổi cấu hình hệ thống.", null);
        }

        if (actorCampus.getSchool().getId() != schoolId) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN,
                    "Bạn không có quyền sửa đổi cấu hình cho trường này.", null);
        }

        if (request.getAdmissionSettingsData() == null &&
                request.getDocumentRequirementsData() == null &&
                request.getFinancePolicyData() == null &&
                request.getOperationSettingsData() == null &&
                request.getFacilityData() == null &&
                request.getQuotaConfigData() == null &&
                request.getResourceDistributionData() == null &&
                request.getBankInfoData() == null
        ) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Dữ liệu cập nhật không được để trống.", null);
        }

        updateConfig(schoolId, request);
        return ResponseBuilder.build(HttpStatus.OK, "Cập nhật thành công", null);
    }

    @Transactional
    public void updateConfig(int schoolId, SchoolConfigRequest request) {
        if (request.getAdmissionSettingsData() != null) updateAdmissionSettings(schoolId, request);
        if (request.getDocumentRequirementsData() != null) updateDocumentRequirements(schoolId, request);
        if (request.getFinancePolicyData() != null) updateFinancePolicy(schoolId, request);
        if (request.getOperationSettingsData() != null) updateOperationSettings(schoolId, request);
        if (request.getFacilityData() != null) updateFacility(schoolId, request);
        if (request.getQuotaConfigData() != null) updateQuotaConfig(schoolId, request);
        if (request.getResourceDistributionData() != null) updateDistributeSchoolResourcesConfig(schoolId, request);
        if (request.getBankInfoData() != null) updateBankSettings(schoolId, request);
    }

    @Transactional
    public void updateBankSettings(int schoolId, SchoolConfigRequest request) {

        SchoolConfigRequest.BankInfoData bankInfoData = request.getBankInfoData();

        if (bankInfoData.getBankId() == null || bankInfoData.getBankId().isBlank()) {
            throw new IllegalArgumentException("Mã ngân hàng không được để trống");
        }
        if (bankInfoData.getAccountNo() == null || bankInfoData.getAccountNo().isBlank()) {
            throw new IllegalArgumentException("Số tài khoản không được để trống");
        }
        if (bankInfoData.getAccountName() == null || bankInfoData.getAccountName().isBlank()) {
            throw new IllegalArgumentException("Tên tài khoản không được để trống");
        }
        if (bankInfoData.getBankName() == null || bankInfoData.getBankName().isBlank()) {
            throw new IllegalArgumentException("Tên ngân hàng không được để trống");
        }

        Map<String, Object> bankInfoJson = new HashMap<>();
        bankInfoJson.put("bankId", bankInfoData.getBankId().trim());
        bankInfoJson.put("accountNo", bankInfoData.getAccountNo().trim());
        bankInfoJson.put("accountName", bankInfoData.getAccountName().trim().toUpperCase());
        bankInfoJson.put("bankName", bankInfoData.getBankName().trim());

        SchoolConfig config = schoolConfigRepo.findBySchoolIdAndKey(schoolId, "bankInfoData")
                .orElse(SchoolConfig.builder()
                        .schoolId(schoolId)
                        .key("bankInfoData")
                        .build());

        config.setValue(bankInfoJson);
        config.setUpdatedAt(LocalDateTime.now());
        schoolConfigRepo.save(config);
    }

    @Transactional
    public void updateAdmissionSettings(int schoolId, SchoolConfigRequest request) {

        SchoolConfigRequest.AdmissionSettingsData admissionSettingsData = request.getAdmissionSettingsData();

        List<Map<String, Object>> allowedMethodsJson = admissionSettingsData.getAllowedMethods().stream()
                .map(method -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("code", method.getCode());
                    data.put("displayName", method.getDisplayName());
                    data.put("description", method.getDescription());
                    return data;
                })
                .collect(Collectors.toList());

        List<String> validMethodCodes = allowedMethodsJson.stream()
                .map(m -> Objects.toString(m.get("code"), ""))
                .toList();

        List<Map<String, Object>> admissionProcessesJson = new ArrayList<>();
        if (admissionSettingsData.getMethodAdmissionProcess() != null) {
            for (var methodProcess : admissionSettingsData.getMethodAdmissionProcess()) {
                if (!validMethodCodes.contains(methodProcess.getMethodCode())) {
                    throw new RuntimeException("Mã phương pháp " + methodProcess.getMethodCode() + " không hợp lệ.");
                }

                Map<String, Object> processMap = new HashMap<>();
                processMap.put("methodCode", methodProcess.getMethodCode());

                List<Map<String, Object>> stepsData = methodProcess.getSteps().stream()
                        .map(step -> {
                            Map<String, Object> s = new HashMap<>();
                            s.put("stepOrder", step.getStepOrder());
                            s.put("stepName", step.getStepName());
                            s.put("description", step.getDescription());
                            return s;
                        }).collect(Collectors.toList());

                processMap.put("steps", stepsData);
                admissionProcessesJson.add(processMap);
            }
        }

        Map<String, Object> admissionJson = new HashMap<>();
        admissionJson.put("allowedMethods", allowedMethodsJson);
        admissionJson.put("admissionProcesses", admissionProcessesJson);
        admissionJson.put("quotaAlertThresholdPercent", admissionSettingsData.getQuotaAlertThresholdPercent());
        admissionJson.put("autoCloseOnFull", admissionSettingsData.isAutoCloseOnFull());

        SchoolConfig config = schoolConfigRepo.findBySchoolIdAndKey(schoolId, "admissionSettingsData")
                .orElse(SchoolConfig.builder()
                        .schoolId(schoolId)
                        .key("admissionSettingsData")
                        .build());

        config.setValue(admissionJson);
        config.setUpdatedAt(LocalDateTime.now());
        schoolConfigRepo.save(config);
    }

    @Transactional
    public void updateDocumentRequirements(int schoolId, SchoolConfigRequest request) {

        SchoolConfig admissionConfig = schoolConfigRepo.findBySchoolIdAndKey(schoolId, "admissionSettingsData")
                .orElseThrow(() -> new RuntimeException("Vui lòng định cấu hình phương thức nhập học trước"));

        Map<String, Object> admissionData = (Map<String, Object>) admissionConfig.getValue();
        List<Map<String, Object>> allowedMethods = (List<Map<String, Object>>) admissionData.get("allowedMethods");

        List<String> validMethodCodes = allowedMethods.stream()
                .map(m -> m.get("code").toString())
                .toList();

        if (request.getDocumentRequirementsData().getByMethod() != null) {
            for (var methodReq : request.getDocumentRequirementsData().getByMethod()) {
                if (!validMethodCodes.contains(methodReq.getMethodCode())) {
                    throw new RuntimeException("Mã phương pháp " + methodReq.getMethodCode() + " không hợp lệ.");
                }
            }
        }

        SchoolConfigRequest.DocumentRequirementsData documentRequirementsData = request.getDocumentRequirementsData();

        if (documentRequirementsData.getByMethod() == null) {
            throw new RuntimeException("Danh sách tài liệu theo phương thức không được để trống.");
        }

        List<Map<String, Object>> byMethodJson = documentRequirementsData.getByMethod().stream()
                .map(method -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("methodCode", method.getMethodCode());
                    data.put("documents", method.getDocuments().stream()
                            .map(doc -> {
                                Map<String, Object> docMap = new HashMap<>();
                                docMap.put("code", doc.getCode());
                                docMap.put("name", doc.getName());
                                docMap.put("required", doc.isRequired());
                                if (doc.getTemplateUrl() != null) {
                                    docMap.put("templateUrl", doc.getTemplateUrl());
                                }
                                return docMap;
                            })
                            .collect(Collectors.toList()));
                    return data;
                })
                .collect(Collectors.toList());

        Map<String, Object> admissionJson = new HashMap<>();
        admissionJson.put("byMethod", byMethodJson);

        SchoolConfig config = schoolConfigRepo.findBySchoolIdAndKey(schoolId, "documentRequirementsData")
                .orElse(SchoolConfig.builder()
                        .schoolId(schoolId)
                        .key("documentRequirementsData")
                        .build());

        config.setValue(admissionJson);
        config.setUpdatedAt(LocalDateTime.now());
        schoolConfigRepo.save(config);
    }

    @Transactional
    public void updateFinancePolicy(int schoolId, SchoolConfigRequest request) {

        SchoolConfigRequest.FinancePolicyData financePolicyData = request.getFinancePolicyData();

        // Xử lý chính sách điều chỉnh giá theo %
        Map<String, Object> priceAdjustmentJson = new HashMap<>();
        priceAdjustmentJson.put("minPercent", financePolicyData.getPriceAdjustment().getMinPercent());
        priceAdjustmentJson.put("maxPercent", financePolicyData.getPriceAdjustment().getMaxPercent());

        Map<String, Object> financePolicyJson = new HashMap<>();
        financePolicyJson.put("priceAdjustment", priceAdjustmentJson);

        SchoolConfig config = schoolConfigRepo.findBySchoolIdAndKey(schoolId, "financePolicyData")
                .orElse(SchoolConfig.builder()
                        .schoolId(schoolId)
                        .key("financePolicyData")
                        .build());

        config.setValue(financePolicyJson);
        config.setUpdatedAt(LocalDateTime.now());
        schoolConfigRepo.save(config);
    }

    @Transactional
    public void updateOperationSettings(int schoolId, SchoolConfigRequest request) {

        SchoolConfigRequest.OperationSettingsData operationSettingsData = request.getOperationSettingsData();

        // 2. Map Working Config (Giờ làm việc)
        Map<String, Object> workingConfigMap = new HashMap<>();
        if (operationSettingsData.getWorkingConfig() != null) {
            workingConfigMap.put("regularDays", operationSettingsData.getWorkingConfig().getRegularDays());
            workingConfigMap.put("weekendDays", operationSettingsData.getWorkingConfig().getWeekendDays());
            workingConfigMap.put("isOpenSunday", Boolean.TRUE.equals(operationSettingsData.getWorkingConfig().getOpenSunday()));
            workingConfigMap.put("note", operationSettingsData.getWorkingConfig().getNote());

            // Map danh sách ca làm việc — chuẩn hóa tên enum + kiểm tra khung giờ theo loại ca
            List<SchoolConfigRequest.WorkShift> shiftList = operationSettingsData.getWorkingConfig().getWorkShifts();
            if (shiftList == null) {
                shiftList = Collections.emptyList();
            }
            List<Map<String, Object>> shiftsJson = shiftList.stream()
                    .map(WorkShiftConfigValidator::toPersistedShiftMap)
                    .collect(Collectors.toList());
            workingConfigMap.put("workShifts", shiftsJson);
        }

        // map Admission Seasons (Các chiến dịch/mùa tuyển sinh đặc biệt)
        List<Map<String, Object>> seasonsJson = new ArrayList<>();
        if (operationSettingsData.getAdmissionSeasons() != null) {
            seasonsJson = operationSettingsData.getAdmissionSeasons().stream()
                    .map(season -> {
                        Map<String, Object> s = new HashMap<>();
                        s.put("seasonName", season.getSeasonName());
                        s.put("startDate", season.getStartDate());
                        s.put("endDate", season.getEndDate());
                        s.put("enableSunday", Boolean.TRUE.equals(season.getEnableSunday()));
                        s.put("minCounsellorMultiplier", season.getMinCounsellorMultiplier() != null ? season.getMinCounsellorMultiplier() : 1);
                        s.put("note", season.getNote());

                        // Map thêm extraShifts nếu mùa đó có ca làm việc tăng cường
                        if (season.getExtraShifts() != null) {
                            List<Map<String, Object>> extraShiftsJson = season.getExtraShifts().stream()
                                    .map(WorkShiftConfigValidator::toPersistedShiftMap)
                                    .collect(Collectors.toList());
                            s.put("extraShifts", extraShiftsJson);
                        }
                        return s;
                    }).collect(Collectors.toList());
        }

        // 4. Gom tất cả vào Map tổng của Operation
        Map<String, Object> operationJson = new HashMap<>();
        operationJson.put("hotline", operationSettingsData.getHotline());
        operationJson.put("emailSupport", operationSettingsData.getEmailSupport());
        operationJson.put("minCounsellorPerSlot", operationSettingsData.getMinCounsellorPerSlot());
        operationJson.put("maxCounsellorsPerSlot", operationSettingsData.getMaxCounsellorsPerSlot());
        operationJson.put("slotDurationInMinutes", operationSettingsData.getSlotDurationInMinutes());
        operationJson.put("bufferBetweenSlotsMinutes", operationSettingsData.getBufferBetweenSlotsMinutes());
        operationJson.put("maxBookingPerSlot", operationSettingsData.getMaxBookingPerSlot());
        operationJson.put("allowBookingBeforeHours", operationSettingsData.getAllowBookingBeforeHours());

        // allowBookingBeforeHours ==> Trường cần thời gian chuẩn bị phòng thi/hồ sơ phỏng vấn
        // ==> không cho phép học sinh đặt lịch hôm nay để thi ngay hôm nay.
        operationJson.put("workingConfig", workingConfigMap);
        operationJson.put("admissionSeasons", seasonsJson);

        SchoolConfig config = schoolConfigRepo.findBySchoolIdAndKey(schoolId, "operationSettingsData")
                .orElse(SchoolConfig.builder()
                        .schoolId(schoolId)
                        .key("operationSettingsData")
                        .build());

        config.setValue(operationJson);
        config.setUpdatedAt(LocalDateTime.now());
        schoolConfigRepo.save(config);
    }

    @Transactional
    public void updateFacility(int schoolId, SchoolConfigRequest request) {

        SchoolConfigRequest.FacilityData facilityData = request.getFacilityData();

        regenerateSchoolInfoDoc(schoolId);


        // 1. Xử lý phần Hình ảnh (ImageData)
        Map<String, Object> imageMap = new HashMap<>();
        imageMap.put("coverUrl", facilityData.getImageData().getCoverUrl());

        // Map list image chi tiết
        List<Map<String, Object>> imageItems = facilityData.getImageData().getImageList().stream()
                .map(img -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("name", img.getName());
                    item.put("url", img.getUrl()); // Đã sửa lỗi thiếu trường url
                    item.put("altName", img.getAltName());
                    item.put("uploadDate", img.getUploadDate());
                    item.put("isUsage", img.getIsUsage());
                    return item;
                })
                .collect(Collectors.toList());
        imageMap.put("imageList", imageItems);

        List<Map<String, Object>> facilityItems = facilityData.getItemList().stream().map(item -> {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("facilityCode", item.getFacilityCode());
            itemMap.put("name", item.getName());
            itemMap.put("value", item.getValue());
            itemMap.put("unit", item.getUnit());
            itemMap.put("category", item.getCategory());
            return itemMap;
        }).collect(Collectors.toList());

        Map<String, Object> facilityJson = new HashMap<>();
        facilityJson.put("overview", facilityData.getOverview());
        facilityJson.put("imageData", imageMap); // Đổi tên cho khớp với DTO
        facilityJson.put("itemList", facilityItems);

        SchoolConfig config = schoolConfigRepo.findBySchoolIdAndKey(schoolId, "facilityData")
                .orElse(SchoolConfig.builder().schoolId(schoolId).key("facilityData").build());

        config.setValue(facilityJson);
        config.setUpdatedAt(LocalDateTime.now());
        schoolConfigRepo.save(config);

        List<Campus> campusList = campusRepo.findByFacilityIsNull();

        Optional<TemplateDocx> campusTemplateDocx = templateDocxRepo.findTopByTypeOrderByVersionDesc(CategoryTemplate.CAMPUS_INFO_TEMPLATE);

        if (campusTemplateDocx.isEmpty()) {
            System.out.println("Mẫu tài liệu thông tin cơ sở không có sẵn.");
            return;
        }

        String templatePath = campusTemplateDocx.get().getFolderName() + "/" + campusTemplateDocx.get().getFileName();

        for (Campus campus : campusList) {

            try {

                supabaseStorageService.removeFile(campus.getFolderPath(), campus.getFileName());

                String uuid = UUID.randomUUID().toString();


                List<Map<String, Object>> facilityItemsBuild = facilityData.getItemList().stream()
                        .map(item -> {
                            Map<String, Object> row = new HashMap<>();
                            row.put("code", item.getFacilityCode());
                            row.put("facilityName", item.getName());
                            row.put("category", item.getCategory());
                            row.put("quantity", item.getValue());
                            row.put("unit", item.getUnit());
                            return row;
                        })
                        .toList();

                Map<String, Object> campusData = buildCampusDocxData(campus, facilityItemsBuild);

                String folderName = campus.getFolderPath();
                String fileName = "campus_info_" + uuid + ".docx";

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

                campus.setFileName(fileName);
                campusRepo.save(campus);

            } catch (Exception e) {
                System.out.println("Lỗi khi tạo tài liệu trường" + e.getMessage());
            }
        }
    }

    @Transactional
    public void updateQuotaConfig(int schoolId, SchoolConfigRequest request) {

        SchoolConfigRequest.QuotaConfigData quotaConfigData = request.getQuotaConfigData();

        // 1. Tính tổng chỉ tiêu đã chia cho các cơ sở
        // lưu ý --> phải lấy con số tổng config từ system để so sánh mới đúng
        int totalAssigned = quotaConfigData.getCampusAssignments().stream()
                .mapToInt(SchoolConfigRequest.QuotaConfigData.CampusQuotaAssignment::getAllocatedQuota)
                .sum();

        // Kiểm tra xem có vượt quá chỉ tiêu tổng không
        // (Giả sử totalSystemQuota trong request là con số thực tế lấy từ PlatformConfig)
        if (totalAssigned > quotaConfigData.getTotalSystemQuota()) {
            throw new IllegalArgumentException("Tổng phân bổ cho cơ sở vật chất (" + totalAssigned +
                    ") vượt quá giới hạn cho phép của hệ thống. (" + quotaConfigData.getTotalSystemQuota() + ")");
        }

        List<Map<String, Object>> campusAssignmentsJson = quotaConfigData.getCampusAssignments().stream()
                .map(doc -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("campusId", doc.getCampusId());
                    data.put("campusName", doc.getCampusName());
                    data.put("allocatedQuota", doc.getAllocatedQuota());
                    return data;
                })
                .collect(Collectors.toList());

        Map<String, Object> quotaJson = new HashMap<>();
        quotaJson.put("academicYear", quotaConfigData.getAcademicYear());
        quotaJson.put("totalSystemQuota", quotaConfigData.getTotalSystemQuota());
        quotaJson.put("campusAssignments", campusAssignmentsJson);

        SchoolConfig config = schoolConfigRepo.findBySchoolIdAndKey(schoolId, "quotaConfigData")
                .orElse(SchoolConfig.builder()
                        .schoolId(schoolId)
                        .key("quotaConfigData")
                        .build());

        config.setValue(quotaJson);
        config.setUpdatedAt(LocalDateTime.now());
        schoolConfigRepo.save(config);
    }

    @Transactional
    public void updateDistributeSchoolResourcesConfig(int schoolId, SchoolConfigRequest request) {

        SchoolConfigRequest.ResourceDistributionData resourceDistributionData = request.getResourceDistributionData();

        if (resourceDistributionData == null || resourceDistributionData.getAllocations() == null) return;

        SchoolSubscription activeSub = schoolSubscriptionRepo
                .findBySchoolIdAndEndDateGreaterThanEqualAndIsSelectedTrue(
                        schoolId, LocalDate.now())
                .orElseThrow(() -> new RuntimeException("Trường chưa đăng ký gói hoặc gói đã hết hạn"));

        UpsertServicePackageFeeRequest.FeatureData systemFeatures = objectMapper.convertValue(
                activeSub.getSubscription().getFeatures(),
                UpsertServicePackageFeeRequest.FeatureData.class
        );

        //Nhóm các yêu cầu phân bổ theo ResourceType để kiểm tra tổng
        Map<ResourceType, List<SchoolConfigRequest.ResourceAllocation>> groupedAllocations =
                resourceDistributionData.getAllocations().stream()
                        .collect(Collectors.groupingBy(alloc -> {
                            ResourceType type = parseResourceType(alloc.getResourceType());
                            if (type == null)
                                throw new RuntimeException("Loại tài nguyên không hợp lệ: " + alloc.getResourceType());
                            return type;
                        }));

        //Duyệt qua từng loại tài nguyên để validate và lưu
        groupedAllocations.forEach((type, allocations) -> {

            // Tính tổng định phân bổ cho loại này
            int totalAllocated = allocations.stream()
                    .mapToInt(SchoolConfigRequest.ResourceAllocation::getAllocatedAmount)
                    .sum();

            // Lấy giới hạn tối đa mà gói cước cho phép
            int systemLimit = getSystemLimitByType(systemFeatures, type);

            if (totalAllocated > systemLimit) {
                throw new RuntimeException("Tổng phân bổ cho " + type + " (" + totalAllocated +
                        ") vượt quá giới hạn hệ thống (" + systemLimit + ")");
            }

            for (var alloc : allocations) {

                ResourceType safeType = parseResourceType(String.valueOf(alloc.getResourceType()));

                if (safeType == null) {
                    throw new RuntimeException("Tài nguyên không hợp lệ hoặc không được hỗ trợ");
                }

                CampusResourceQuota quota = campusResourceQuotaRepo.findByCampusIdAndResourceType(alloc.getCampusId(), type)
                        .orElse(new CampusResourceQuota());

                quota.setCampus(campusRepo.getReferenceById(alloc.getCampusId()));
                quota.setResourceType(safeType);
                quota.setMaxQuota(alloc.getAllocatedAmount());
                campusResourceQuotaRepo.save(quota);
            }
        });

        saveDistributionToConfig(schoolId, resourceDistributionData);
    }

    // hỗ trợ vào việc lưu vào bảng school config
    private void saveDistributionToConfig(int schoolId, SchoolConfigRequest.ResourceDistributionData data) {
        List<Map<String, Object>> allocationsJson = data.getAllocations().stream()
                .map(alloc -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("resourceType", alloc.getResourceType().trim().toUpperCase());
                    map.put("campusId", alloc.getCampusId());
                    map.put("allocatedAmount", alloc.getAllocatedAmount());
                    return map;
                })
                .collect(Collectors.toList());

        Map<String, Object> finalJson = new HashMap<>();
        finalJson.put("allocations", allocationsJson);

        SchoolConfig config = schoolConfigRepo.findBySchoolIdAndKey(schoolId, "resourceDistributionData")
                .orElse(SchoolConfig.builder()
                        .schoolId(schoolId)
                        .key("resourceDistributionData")
                        .build());

        config.setValue(finalJson);
        config.setUpdatedAt(LocalDateTime.now());
        schoolConfigRepo.save(config);
    }

    // Hàm hỗ trợ lấy giới hạn từ FeatureData dựa trên ResourceType
    private int getSystemLimitByType(UpsertServicePackageFeeRequest.FeatureData features, ResourceType type) {
        return switch (type) {
            case COUNSELLOR -> features.getMaxCounsellors();
            default -> 0;
        };
    }

    public static ResourceType parseResourceType(String value) {
        String normalizedValue = normalize(value);
        if (normalizedValue == null) {
            return null;
        }

        return Arrays.stream(ResourceType.values())
                .filter(r -> r.getValue().equalsIgnoreCase(normalizedValue) || r.name().equalsIgnoreCase(normalizedValue))
                .findFirst()
                .orElse(null);
    }

    public static String normalize(String value) {
        return CampusValidation.normalize(value);
    }

    @Override
    public ResponseEntity<ResponseObject> getSchoolConfigList(int schoolId) {

        List<SchoolConfig> configs = schoolConfigRepo.findAllBySchoolId(schoolId);
        Map<String, Object> result = new HashMap<>(configs.stream()
                .collect(Collectors.toMap(SchoolConfig::getKey, SchoolConfig::getValue)));

        for (String key : List.of("admissionSettingsData", "documentRequirementsData")) {
            if (!result.containsKey(key)) {
                Map<String, Object> fallback = getConfigByKey(schoolId, key);
                if (fallback != null) result.put(key, fallback.get(key));
            }
        }

        if (result.containsKey("documentRequirementsData")) {
            result.put("documentRequirementsData", injectMandatoryAll((Map<String, Object>) result.get("documentRequirementsData")));
        }

        return ResponseBuilder.build(HttpStatus.OK, "", result);
    }

    @Override
    public ResponseEntity<ResponseObject> getSchoolConfigByKey(String k) {

        Campus actorCampus = extractActorCampus();

        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.UNAUTHORIZED, "không có phép", null);
        }

        int schoolId = actorCampus.getSchool().getId();

        Map<String, Object> data = getConfigByKey(schoolId, k);

        if (data == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Không tìm thấy cấu hình cho trường này", null);
        }

        return ResponseBuilder.build(HttpStatus.OK, "", data);
    }

    @Override
    public ResponseEntity<ResponseObject> getCampusConfigList() {

        Campus actorCampus = extractActorCampus();
        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.UNAUTHORIZED, "Không có quyền truy cập", null);
        }

        List<Campus> campusList;
        if (Boolean.TRUE.equals(actorCampus.getIsPrimaryBranch())) {
            campusList = campusRepo.findBySchoolId(actorCampus.getSchool().getId());
        } else {
            campusList = Collections.singletonList(actorCampus);
        }

        List<Map<String, Object>> campusConfigJson = campusList.stream()
                .map(campus -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("campusId", campus.getId());
                    map.put("campusName", campus.getName());
                    map.put("campusPrimary", Boolean.TRUE.equals(campus.getIsPrimaryBranch()));
                    map.put("facilityConfig", campus.getFacility());
                    map.put("policyDetail", campus.getPolicyDetail());
                    return map;
                })
                .collect(Collectors.toList());

        return ResponseBuilder.build(HttpStatus.OK, "", campusConfigJson);
    }

    private Map<String, Object> getConfigByKey(int schoolId, String key) {
        SchoolConfig schoolConfig = schoolConfigRepo.findBySchoolIdAndKey(schoolId, key).orElse(null);
        if (schoolConfig != null && schoolConfig.getValue() instanceof Map<?, ?> schoolValue) {
            Map<String, Object> data = new HashMap<>();
            data.put(key, schoolValue);
            return data;
        }

        if ("admissionSettingsData".equals(key) || "documentRequirementsData".equals(key)) {
            Map<String, Object> templateData = resolveAdmissionTemplateByKey(key);
            if (templateData != null) {
                return templateData;
            }
        }

        return null;
    }

    private Map<String, Object> resolveAdmissionTemplateByKey(String key) {
        PlatformConfig platformConfig = platformConfigRepo.findByKey("admissionSettingsData").orElse(null);
        if (platformConfig == null || !(platformConfig.getValue() instanceof Map<?, ?> rawTemplate)) {
            return null;
        }

        Map<String, Object> template = (Map<String, Object>) rawTemplate;
        Map<String, Object> data = new HashMap<>();

        if ("admissionSettingsData".equals(key)) {
            data.put("admissionSettingsData", buildAdmissionSettingsFromTemplate(template));
            return data;
        }

        if ("documentRequirementsData".equals(key)) {
            data.put("documentRequirementsData", buildDocumentRequirementsFromTemplate(template));
            return data;
        }

        return null;
    }

    private Map<String, Object> buildAdmissionSettingsFromTemplate(Map<String, Object> template) {
        Map<String, Object> admission = new HashMap<>();
        admission.put("allowedMethods", toSafeList(template.get("allowedMethods")));
        admission.put("admissionProcesses", toSafeList(template.get("admissionProcesses")));
        return admission;
    }

    private Map<String, Object> buildDocumentRequirementsFromTemplate(Map<String, Object> template) {
        Map<String, Object> docReq = new HashMap<>();
        Map<String, Object> templateDocReq = template.get("documentRequirementsData") instanceof Map<?, ?> doc
                ? (Map<String, Object>) doc
                : null;

        docReq.put("mandatoryAll", templateDocReq != null ? toSafeList(templateDocReq.get("mandatoryAll")) : Collections.emptyList());
        docReq.put("byMethod", templateDocReq != null ? toSafeList(templateDocReq.get("byMethod")) : Collections.emptyList());
        return docReq;
    }

    private Map<String, Object> injectMandatoryAll(Map<String, Object> docReqData) {
        Map<String, Object> result = new HashMap<>(docReqData);
        List<Map<String, Object>> mandatoryAll = Collections.emptyList();
        Object systemDocReq = platformConfigRepo.findByKey("admissionSettingsData")
                .map(c -> ((Map<String, Object>) c.getValue()).get("documentRequirementsData"))
                .orElse(null);
        if (systemDocReq instanceof Map<?, ?> systemMap) {
            Object adminMandatory = ((Map<String, Object>) systemMap).get("mandatoryAll");
            if (adminMandatory instanceof List<?>) {
                mandatoryAll = (List<Map<String, Object>>) adminMandatory;
            }
        }
        result.put("mandatoryAll", mandatoryAll);
        return result;
    }

    private List<Map<String, Object>> toSafeList(Object source) {
        if (source instanceof List<?> list) {
            return (List<Map<String, Object>>) list;
        }
        return Collections.emptyList();
    }

    private Campus extractActorCampus() {
        Account account = AuthRequestUtil.extractAuthenticatedAccount();
        if (account == null || account.getRole() != Role.SCHOOL) {
            return null;
        }
        return account.getCampus();
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
    @Transactional
    public void regenerateSchoolInfoDoc(int schoolId) {

        Optional<School> school = schoolRepo.findById(schoolId);
        if (school.isEmpty()) {
            System.out.println("regenerateSchoolInfoDoc: school not found id=" + schoolId);
            return;
        }

        try {

            Optional<TemplateDocx> tpl = templateDocxRepo.findTopByTypeOrderByVersionDesc(CategoryTemplate.SCHOOL_INFO_TEMPLATE);
            if (tpl.isEmpty()) {
                System.out.println("regenerateSchoolInfoDoc: no SCHOOL_INFO_TEMPLATE found");
                return;
            }

            supabaseStorageService.removeFile(school.get().getFolderPath(), school.get().getFileName());

            Map<String, Object> fields = new LinkedHashMap<>();
            fields.put("name", school.get().getName());
            fields.put("description", school.get().getDescription() != null ? school.get().getDescription() : "");
            fields.put("taxCode", school.get().getTaxCode() != null ? school.get().getTaxCode() : "");
            fields.put("websiteUrl", school.get().getWebsiteUrl() != null ? school.get().getWebsiteUrl() : "");
            fields.put("representativeName", school.get().getRepresentativeName() != null ? school.get().getRepresentativeName() : "");
            fields.put("foundingDate", school.get().getFoundingDate() != null
                    ? school.get().getFoundingDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "");
            fields.put("logoUrl", school.get().getLogoUrl() != null ? school.get().getLogoUrl() : "");
            fields.put("businessLicenseUrl", school.get().getBusinessLicenseUrl() != null ? school.get().getBusinessLicenseUrl() : "");

            List<Map<String, Object>> curriculumRows = new ArrayList<>();
            List<Map<String, Object>> programRows = new ArrayList<>();

            List<Curriculum> curriculumList = school.get().getCurriculumList() == null
                    ? Collections.emptyList()
                    : school.get().getCurriculumList()
                    .stream()
                    .filter(c -> c.getCurriculumStatus() == Status.CUR_ACTIVE)
                    .toList();

            int currIdx = 1;
            for (Curriculum curriculum : curriculumList) {


                Map<String, Object> cRow = new LinkedHashMap<>();
                cRow.put("stt", currIdx++);
                cRow.put("curName", curriculum.getName() != null ? curriculum.getName() : "");
                cRow.put("curType", curriculum.getCurriculumType() != null ? curriculum.getCurriculumType().getValue() : "");
                cRow.put("curYear", curriculum.getApplicationYear() != null ? String.valueOf(curriculum.getApplicationYear()) : "");
                cRow.put("curDesc", curriculum.getDescription() != null ? curriculum.getDescription() : "");
                cRow.put("curSubjects", buildSubjectsText(curriculum.getSubjectsJsonb(), null));
                curriculumRows.add(cRow);

                List<Program> activePrograms = curriculum.getPrograms() == null
                        ? Collections.emptyList()
                        : curriculum.getPrograms()
                        .stream()
                        .filter(p -> p.getStatus() == Status.PRO_ACTIVE)
                        .toList();

                int progIdx = 1;
                for (Program program : activePrograms) {
                    Map<String, Object> pRow = new LinkedHashMap<>();
                    pRow.put("stt", progIdx++);
                    pRow.put("progCurName", curriculum.getName() != null ? curriculum.getName() : "");
                    pRow.put("progName", program.getName() != null ? program.getName() : "");
                    pRow.put("progFee", program.getBaseTuitionFee() != null
                            ? program.getBaseTuitionFee().toPlainString()
                            + (program.getFeeUnit() != null ? " / " + program.getFeeUnit().name() : "")
                            : "");
                    pRow.put("progStandard", program.getGraduationStandard() != null ? program.getGraduationStandard() : "");
                    pRow.put("progSubjects", buildSubjectsText(curriculum.getSubjectsJsonb(), program.getExtraSubjectsJsonb()));
                    programRows.add(pRow);
                }
            }

            fields.put("curriculums", curriculumRows);
            fields.put("programs", programRows);

            String folderName = school.get().getFolderPath();
            String fileName = school.get().getFileName();
            String templatePath = tpl.get().getFolderName() + "/" + tpl.get().getFileName();

            String fileUrl = supabaseStorageService.generateDocFileFromTemplate(fields, templatePath, folderName, fileName);

            try {
                Map<String, Object> payload = new LinkedHashMap<>();
                payload.put("type", "school_info");
                payload.put("schoolId", school.get().getId());
                payload.put("schoolName", school.get().getName());
                payload.put("schoolInfoFileUrl", fileUrl);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                restTemplate.postForEntity(n8nUrl, new HttpEntity<>(payload, headers), String.class);
            } catch (Exception e) {
                System.out.println("regenerateSchoolInfoDoc n8n error: " + e.getMessage());
            }

            school.get().setFileName(fileName);
            schoolRepo.save(school.get());

        } catch (Exception ex) {
            System.out.println("regenerateSchoolInfoDoc error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private String buildSubjectsText(Object baseJsonb, Object extraJsonb) {
        List<Map<String, Object>> subjects = new ArrayList<>();
        if (baseJsonb instanceof List<?> base) {
            for (Object item : base) {
                if (item instanceof Map<?, ?> m) subjects.add((Map<String, Object>) m);
            }
        }
        if (extraJsonb instanceof List<?> extra) {
            for (Object item : extra) {
                if (item instanceof Map<?, ?> m) subjects.add((Map<String, Object>) m);
            }
        }
        if (subjects.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < subjects.size(); i++) {
            Map<String, Object> s = subjects.get(i);
            String subjectName = s.getOrDefault("name", "").toString();
            String type = Boolean.TRUE.equals(s.get("isMandatory")) ? "Bắt buộc" : "Tự chọn";
            Object desc = s.get("description");
            sb.append(i + 1).append(". ").append(subjectName).append(" (").append(type).append(")");
            if (desc != null && !desc.toString().isBlank()) sb.append(": ").append(desc);
            if (i < subjects.size() - 1) sb.append("\n");
        }
        return sb.toString();
    }
}

