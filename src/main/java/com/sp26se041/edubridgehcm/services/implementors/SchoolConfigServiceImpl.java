package com.sp26se041.edubridgehcm.services.implementors;

import com.sp26se041.edubridgehcm.models.SchoolConfig;
import com.sp26se041.edubridgehcm.repositories.SchoolConfigRepo;
import com.sp26se041.edubridgehcm.requests.CreateFacilityTemplateRequest;
import com.sp26se041.edubridgehcm.requests.SchoolConfigRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.SchoolConfigService;
import com.sp26se041.edubridgehcm.utils.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SchoolConfigServiceImpl implements SchoolConfigService {

    private final SchoolConfigRepo schoolConfigRepo;

    @Override
    @Transactional
    public ResponseEntity<ResponseObject> updateSchoolConfig(int schoolId, SchoolConfigRequest request) {

        if (request.getAdmissionSettingsData() == null &&
                request.getDocumentRequirementsData() == null &&
                request.getFinancePolicyData() == null &&
                request.getOperationSettingsData() == null &&
                request.getFacilityData() == null &&
                request.getQuotaConfigData() == null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Data missing", null);
        }

        updateConfig(schoolId, request);
        return ResponseBuilder.build(HttpStatus.OK, "Update successfully", null);
    }

    @Transactional
    public void updateConfig(int schoolId, SchoolConfigRequest request) {
        if (request.getAdmissionSettingsData() != null) updateAdmissionSettings(schoolId, request);
        if (request.getDocumentRequirementsData() != null) updateDocumentRequirements(schoolId, request);
        if (request.getFinancePolicyData() != null) updateFinancePolicy(schoolId, request);
        if (request.getOperationSettingsData() != null) updateOperationSettings(schoolId, request);
        if (request.getFacilityData() != null) updateFacility(schoolId, request);
        if (request.getQuotaConfigData() != null) updateQuotaConfig(schoolId, request);
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
        admissionJson.put("itemList", admissionSettingsData.isAutoCloseOnFull());

        SchoolConfig config = schoolConfigRepo.findBySchoolIdAndKey(schoolId, "admission_settings")
                .orElse(SchoolConfig.builder()
                        .schoolId(schoolId)
                        .key("admission_settings")
                        .build());

        config.setValue(admissionJson);
        config.setUpdatedAt(LocalDateTime.now());
        schoolConfigRepo.save(config);
    }

    @Transactional
    public void updateDocumentRequirements(int schoolId, SchoolConfigRequest request) {

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

        SchoolConfig config = schoolConfigRepo.findBySchoolIdAndKey(schoolId, "document_requirements")
                .orElse(SchoolConfig.builder()
                        .schoolId(schoolId)
                        .key("document_requirements")
                        .build());

        config.setValue(admissionJson);
        config.setUpdatedAt(LocalDateTime.now());
        schoolConfigRepo.save(config);
    }

    @Transactional
    public void updateFinancePolicy(int schoolId, SchoolConfigRequest request) {

        SchoolConfigRequest.FinancePolicyData financePolicyData = request.getFinancePolicyData();

        Map<String, Object> reservationFeeJson = new HashMap<>();
        reservationFeeJson.put("amount", financePolicyData.getReservationFee().getAmount());
        reservationFeeJson.put("currency", financePolicyData.getReservationFee().getCurrency());
        reservationFeeJson.put("display", financePolicyData.getReservationFee().getDisplay());

        Map<String, Object> priceAdjustmentJson = new HashMap<>();
        priceAdjustmentJson.put("minPercent", financePolicyData.getPriceAdjustment().getMinPercent());
        priceAdjustmentJson.put("maxPercent", financePolicyData.getPriceAdjustment().getMaxPercent());

        Map<String, Object> financePolicyJson = new HashMap<>();
        financePolicyJson.put("reservationFee", reservationFeeJson);
        financePolicyJson.put("priceAdjustment", priceAdjustmentJson);
        financePolicyJson.put("paymentNotes", financePolicyData.getPaymentNotes());

        SchoolConfig config = schoolConfigRepo.findBySchoolIdAndKey(schoolId, "finance_policy")
                .orElse(SchoolConfig.builder()
                        .schoolId(schoolId)
                        .key("finance_policy")
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
            workingConfigMap.put("isOpenSunday", operationSettingsData.getWorkingConfig().isOpenSunday());
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

        // 3. Map Admission Steps (Quy trình các bước)
        List<Map<String, Object>> stepsJson = operationSettingsData.getAdmissionSteps().stream()
                .map(step -> {
                    Map<String, Object> s = new HashMap<>();
                    s.put("stepOrder", step.getStepOrder());
                    s.put("stepName", step.getStepName());
                    s.put("description", step.getDescription());
                    return s;
                }).collect(Collectors.toList());

        // 4. Gom tất cả vào Map tổng của Operation
        Map<String, Object> operationJson = new HashMap<>();
        operationJson.put("hotline", operationSettingsData.getHotline());
        operationJson.put("emailSupport", operationSettingsData.getEmailSupport());
        operationJson.put("workingConfig", workingConfigMap);
        operationJson.put("admissionSteps", stepsJson);

        SchoolConfig config = schoolConfigRepo.findBySchoolIdAndKey(schoolId, "operation_settings")
                .orElse(SchoolConfig.builder()
                        .schoolId(schoolId)
                        .key("operation_settings")
                        .build());

        config.setValue(operationJson);
        config.setUpdatedAt(LocalDateTime.now());
        schoolConfigRepo.save(config);
    }

    @Transactional
    public void updateFacility(int schoolId, SchoolConfigRequest request) {

        SchoolConfigRequest.FacilityData facilityData = request.getFacilityData();

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

        SchoolConfig config = schoolConfigRepo.findBySchoolIdAndKey(schoolId, "facility_template").orElse(SchoolConfig.builder().schoolId(schoolId).key("facility_template").build());

        config.setValue(facilityJson);
        config.setUpdatedAt(LocalDateTime.now());
        schoolConfigRepo.save(config);
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
            throw new IllegalArgumentException("Total allocation for facilities (" + totalAssigned +
                    ") exceeding the system's allowed limits. (" + quotaConfigData.getTotalSystemQuota() + ")");
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

        SchoolConfig config = schoolConfigRepo.findBySchoolIdAndKey(schoolId, "quota_config")
                .orElse(SchoolConfig.builder()
                        .schoolId(schoolId)
                        .key("quota-config")
                        .build());

        config.setValue(quotaJson);
        config.setUpdatedAt(LocalDateTime.now());
        schoolConfigRepo.save(config);
    }

    @Override
    public ResponseEntity<ResponseObject> getSchoolConfigList(int schoolId) {

        List<SchoolConfig> configs = schoolConfigRepo.findAllBySchoolId(schoolId);
        Map<String, Object> result = configs.stream().collect(Collectors.toMap(SchoolConfig::getKey, SchoolConfig::getValue));

        return ResponseBuilder.build(HttpStatus.OK, "Fetch successfully", result);
    }

    @Override
    public ResponseEntity<ResponseObject> getSchoolConfigByKey(String k) {

        Map<String, Object> data = getConfigByKey(k);

        if (data == null) return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Data invalid", null);

        return ResponseBuilder.build(HttpStatus.OK, "", data);
    }

    private Map<String, Object> getConfigByKey(String key) {

        SchoolConfig config = schoolConfigRepo.findByKey(key).orElse(null);

        if (config == null) return null;

        Map<String, Object> data = new HashMap<>();
        Map<String, Object> value = (Map<String, Object>) config.getValue();
        data.put(key, value);

        return data;
    }

    @Override
    @Transactional
    public ResponseEntity<ResponseObject> createOrUpdateFacilityTemplate(int schoolId, CreateFacilityTemplateRequest request) {


        return ResponseBuilder.build(HttpStatus.OK, "Facility template upsert successfully", null);
    }

    @Override
    public ResponseEntity<ResponseObject> getFacilityTemplate(int schoolId) {

        SchoolConfig config = schoolConfigRepo.findBySchoolIdAndKey(schoolId, "facility_template").orElse(null);

        if (config == null || config.getValue() == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "No facility template found for this school", null);
        }
        return ResponseBuilder.build(HttpStatus.OK, "", config.getValue());
    }
}

