package com.sp26se041.edubridgehcm.services.implementors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sp26se041.edubridgehcm.enums.BoardingType;
import com.sp26se041.edubridgehcm.enums.CategoryTemplate;
import com.sp26se041.edubridgehcm.enums.ResourceType;
import com.sp26se041.edubridgehcm.enums.Role;
import com.sp26se041.edubridgehcm.models.Account;
import com.sp26se041.edubridgehcm.models.Campus;
import com.sp26se041.edubridgehcm.models.CampusResourceQuota;
import com.sp26se041.edubridgehcm.models.School;
import com.sp26se041.edubridgehcm.models.SchoolConfig;
import com.sp26se041.edubridgehcm.models.SchoolSubscription;
import com.sp26se041.edubridgehcm.models.TemplateDocx;
import com.sp26se041.edubridgehcm.repositories.CampusRepo;
import com.sp26se041.edubridgehcm.repositories.CampusResourceQuotaRepo;
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
                request.getResourceDistributionData() == null
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

        Map<String, Object> admissionJson = new HashMap<>();
        admissionJson.put("allowedMethods", allowedMethodsJson);
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
                .collect(Collectors.toList());

        if (request.getDocumentRequirementsData().getByMethod() != null) {
            for (var methodReq : request.getDocumentRequirementsData().getByMethod()) {
                if (!validMethodCodes.contains(methodReq.getMethodCode())) {
                    throw new RuntimeException("Mã phương pháp " + methodReq.getMethodCode() + "không hợp lệ.");
                }
            }
        }

        SchoolConfigRequest.DocumentRequirementsData documentRequirementsData = request.getDocumentRequirementsData();

        List<Map<String, Object>> mandatoryAllJson = documentRequirementsData.getMandatoryAll().stream()
                .map(doc -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("code", doc.getCode());
                    map.put("name", doc.getName());
                    map.put("required", doc.isRequired());
                    return map;
                })
                .collect(Collectors.toList());

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
                                return docMap;
                            })
                            .collect(Collectors.toList()));
                    return data;
                })
                .collect(Collectors.toList());

        Map<String, Object> admissionJson = new HashMap<>();
        admissionJson.put("mandatoryAll", mandatoryAllJson);
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

        List<Map<String, Object>> feeItemsJson = Optional.ofNullable(financePolicyData.getFeeItems())
                .orElse(Collections.emptyList())
                .stream()
                .map(fee -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("feeCode", fee.getFeeCode());
                    item.put("feeName", fee.getFeeName());
                    item.put("amount", fee.getAmount());
                    item.put("currency", fee.getCurrency());
                    item.put("display", fee.getDisplay());
                    item.put("isReservationFee", fee.isReservationFee()); //==> Quan trọng để logic check giữ chỗ
                    item.put("isMandatory", fee.isMandatory());
                    item.put("description", fee.getDescription());
                    return item;
                })
                .collect(Collectors.toList());

        // Xử lý chính sách điều chỉnh giá theo %
        Map<String, Object> priceAdjustmentJson = new HashMap<>();
        priceAdjustmentJson.put("minPercent", financePolicyData.getPriceAdjustment().getMinPercent());
        priceAdjustmentJson.put("maxPercent", financePolicyData.getPriceAdjustment().getMaxPercent());

        Map<String, Object> financePolicyJson = new HashMap<>();
        financePolicyJson.put("feeItems", feeItemsJson);
        financePolicyJson.put("priceAdjustment", priceAdjustmentJson);
        financePolicyJson.put("paymentNotes", financePolicyData.getPaymentNotes());

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

        Map<String, Object> academicCalendarMap = new HashMap<>();
        if (operationSettingsData.getAcademicCalendar() != null) {
            var calendar = operationSettingsData.getAcademicCalendar();

            if (calendar.getTerm1() != null && calendar.getTerm2() != null) {
                LocalDate endT1 = calendar.getTerm1().getEnd();
                LocalDate startT2 = calendar.getTerm2().getStart();

                // check cả 2 date khác null trước khi so sánh
                if (endT1 != null && startT2 != null && !endT1.isBefore(startT2)) {
                    throw new IllegalArgumentException("Học kỳ 1 (" + endT1 + ") phải kết thúc trước khi Học kỳ 2 (" + startT2 + ") bắt đầu.");
                }
            }

            if (calendar.getTerm1() != null) {
                Map<String, Object> term1 = new HashMap<>();
                term1.put("start", calendar.getTerm1().getStart());
                term1.put("end", calendar.getTerm1().getEnd());
                academicCalendarMap.put("term1", term1);
            }

            if (calendar.getTerm2() != null) {
                Map<String, Object> term2 = new HashMap<>();
                term2.put("start", calendar.getTerm2().getStart());
                term2.put("end", calendar.getTerm2().getEnd());
                academicCalendarMap.put("term2", term2);
            }
        }

        SchoolConfig admissionConfig = schoolConfigRepo.findBySchoolIdAndKey(schoolId, "admissionSettingsData")
                .orElseThrow(() -> new RuntimeException("Vui lòng định cấu hình phương thức nhập học trước"));

        Map<String, Object> admissionData = (Map<String, Object>) admissionConfig.getValue();
        List<Map<String, Object>> allowedMethods = (List<Map<String, Object>>) admissionData.get("allowedMethods");

        List<String> validMethodCodes = allowedMethods.stream()
                .map(m -> m.get("code").toString())
                .collect(Collectors.toList());

        // 2. Map Working Config (Giờ làm việc)
        Map<String, Object> workingConfigMap = new HashMap<>();
        if (operationSettingsData.getWorkingConfig() != null) {
            workingConfigMap.put("regularDays", operationSettingsData.getWorkingConfig().getRegularDays());
            workingConfigMap.put("weekendDays", operationSettingsData.getWorkingConfig().getWeekendDays());
            workingConfigMap.put("isOpenSunday", Boolean.TRUE.equals(operationSettingsData.getWorkingConfig().getOpenSunday()));
            workingConfigMap.put("note", operationSettingsData.getWorkingConfig().getNote());

            // Map danh sách ca làm việc (Shifts)
            List<Map<String, Object>> shiftsJson = operationSettingsData.getWorkingConfig().getWorkShifts().stream()
                    .map(shift -> {
                        Map<String, Object> s = new HashMap<>();
                        s.put("name", shift.getName());
                        s.put("startTime", shift.getStartTime());
                        s.put("endTime", shift.getEndTime());
                        return s;
                    }).collect(Collectors.toList());
            workingConfigMap.put("workShifts", shiftsJson);
        }

        //Map Admission Processes (Quy trình tuyển sinh theo từng phương thức)
        List<Map<String, Object>> processesJson = new ArrayList<>();

        if (operationSettingsData.getMethodAdmissionProcess() != null) {
            for (var methodProcess : operationSettingsData.getMethodAdmissionProcess()) {

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
                processesJson.add(processMap);
            }
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
                                    .map(shift -> {
                                        Map<String, Object> sh = new HashMap<>();
                                        sh.put("name", shift.getName());
                                        sh.put("startTime", shift.getStartTime());
                                        sh.put("endTime", shift.getEndTime());
                                        return sh;
                                    }).collect(Collectors.toList());
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
        operationJson.put("slotDurationInMinutes", operationSettingsData.getSlotDurationInMinutes());
        operationJson.put("maxBookingPerSlot", operationSettingsData.getMaxBookingPerSlot());
        operationJson.put("allowBookingBeforeHours", operationSettingsData.getAllowBookingBeforeHours());
        // allowBookingBeforeHours ==> Trường cần thời gian chuẩn bị phòng thi/hồ sơ phỏng vấn
        // ==> không cho phép học sinh đặt lịch hôm nay để thi ngay hôm nay.
        operationJson.put("workingConfig", workingConfigMap);
        operationJson.put("academicCalendar", academicCalendarMap);
        operationJson.put("admissionProcesses", processesJson);
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

        try {

            Optional<School> school = schoolRepo.findById(schoolId);

            if (school.isEmpty()) {
                throw new Exception("Trường ko tìm thấy hoặc đã bị xóa");
            }

            Optional<TemplateDocx> schoolTemplateDocx = templateDocxRepo.findTopByTypeOrderByVersionDesc(CategoryTemplate.SCHOOL_INFO_TEMPLATE);

            if (schoolTemplateDocx.isEmpty()) {
                throw new Exception("Mẫu trường học docx không được tìm thấy hoặc đã bị xóa");
            }

            supabaseStorageService.removeFile(school.get().getFolderPath(), school.get().getFileName());

            String uuid = UUID.randomUUID().toString();

            Map<String, Object> fields = new LinkedHashMap<>();
            fields.put("name", school.get().getName());
            fields.put("description", school.get().getDescription());
            fields.put("taxCode", school.get().getTaxCode());
            fields.put("websiteUrl", school.get().getWebsiteUrl());
            fields.put("representativeName", school.get().getRepresentativeName());
            fields.put("hotline", school.get().getHotline());
            fields.put("foundingDate", school.get().getFoundingDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            fields.put("logoUrl", school.get().getLogoUrl());
            fields.put("businessLicenseUrl", school.get().getBusinessLicenseUrl());

            String folderName = school.get().getFolderPath();
            String fileName = "school_info_" + uuid + ".docx";

            String templatePath = schoolTemplateDocx.get().getFolderName() + "/" + schoolTemplateDocx.get().getFileName();

            String schoolFileUrl = supabaseStorageService.generateDocFileFromTemplate(fields, templatePath, folderName, fileName);

            try {
                Map<String, Object> payload = new LinkedHashMap<>();
                payload.put("type", "school_info");
                payload.put("schoolId", school.get().getId());
                payload.put("schoolName", school.get().getName());
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

            school.get().setFileName(fileName);
            schoolRepo.save(school.get());

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }


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
            System.out.println("Mẫu tài liệu của trường không có sẵn.");
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
                System.out.println("Không tạo được docx của trường" + e.getMessage());
            }
        }
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
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    @Override
    public ResponseEntity<ResponseObject> getSchoolConfigList(int schoolId) {

        List<SchoolConfig> configs = schoolConfigRepo.findAllBySchoolId(schoolId);
        Map<String, Object> result = configs.stream().collect(Collectors.toMap(SchoolConfig::getKey, SchoolConfig::getValue));

        return ResponseBuilder.build(HttpStatus.OK, "", result);
    }

    @Override
    public ResponseEntity<ResponseObject> getSchoolConfigByKey(String k) {

        Campus actorCampus = extractActorCampus();
        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.UNAUTHORIZED, "không có phép", null);
        }

        SchoolConfig config = schoolConfigRepo.findBySchoolIdAndKey(actorCampus.getSchool().getId(), k).orElse(null);

        if (config == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Không tìm thấy cấu hình cho trường này", null);
        }

        Map<String, Object> data = getConfigByKey(k);

        if (data == null) return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Dữ liệu không hợp lệ", null);

        return ResponseBuilder.build(HttpStatus.OK, "", data);
    }

    @Override
    public ResponseEntity<ResponseObject> getCampusConfigList() {

        List<Campus> campusList = campusRepo.findAll();

        List<Map<String, Object>> campusConfigJson = campusList.stream()
                .map(campus -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("campusId", campus.getId());
                    map.put("campusName", campus.getName());
                    map.put("facilityConfig", campus.getFacility());
                    map.put("policyDetail", campus.getPolicyDetail());
                    return map;
                })
                .collect(Collectors.toList());

        return ResponseBuilder.build(HttpStatus.OK, "", campusConfigJson);
    }

    private Map<String, Object> getConfigByKey(String key) {

        SchoolConfig config = schoolConfigRepo.findByKey(key).orElse(null);

        if (config == null) return null;

        Map<String, Object> data = new HashMap<>();
        Map<String, Object> value = (Map<String, Object>) config.getValue();
        data.put(key, value);

        return data;
    }

    private Campus extractActorCampus() {
        Account account = AuthRequestUtil.extractAuthenticatedAccount();
        if (account == null || account.getRole() != Role.SCHOOL) {
            return null;
        }
        return account.getCampus();
    }
}

