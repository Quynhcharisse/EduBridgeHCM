package com.sp26se041.edubridgehcm.services.implementors;

import com.sp26se041.edubridgehcm.enums.ImportType;
import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.models.PlatformConfig;
import com.sp26se041.edubridgehcm.models.School;
import com.sp26se041.edubridgehcm.models.Subscription;
import com.sp26se041.edubridgehcm.repositories.PlatformConfigRepo;
import com.sp26se041.edubridgehcm.repositories.SchoolRepo;
import com.sp26se041.edubridgehcm.repositories.SchoolSubscriptionRepo;
import com.sp26se041.edubridgehcm.repositories.SubscriptionRepo;
import com.sp26se041.edubridgehcm.requests.CreateConfigDataRequest;
import com.sp26se041.edubridgehcm.requests.ImportConfirmRequest;
import com.sp26se041.edubridgehcm.requests.UpsertServicePackageFeeRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.SystemService;
import com.sp26se041.edubridgehcm.utils.ConfigSystemUtil;
import com.sp26se041.edubridgehcm.utils.ResponseBuilder;
import com.sp26se041.edubridgehcm.validations.platform.SystemConfigValidation;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SystemServiceImpl implements SystemService {

    private final PlatformConfigRepo platformConfigRepo;

    private final SchoolRepo schoolRepo;

    private final SubscriptionRepo subscriptionRepo;

    private final SchoolSubscriptionRepo schoolSubscriptionRepo;

    @Override
    public ResponseEntity<ResponseObject> getConfigData() {

        List<PlatformConfig> platformConfigList = platformConfigRepo.findAll();

        Map<String, Object> data = new HashMap<>();

        for (PlatformConfig config : platformConfigList) {
            String key = config.getKey();
            Map<String, Object> value = (Map<String, Object>) config.getValue();
            data.put(key, value);
        }

        return ResponseBuilder.build(HttpStatus.OK, "", data);
    }

    @Override
    public ResponseEntity<ResponseObject> getConfigDataByKey(String k) {

        Map<String, Object> data = getConfigByKey(k);

        if (data == null) {
            return ResponseBuilder.build(
                    HttpStatus.BAD_REQUEST,
                    "Dữ liệu không hợp lệ",
                    null
            );
        }

        return ResponseBuilder.build(HttpStatus.OK, "", data);
    }

    private Map<String, Object> getConfigByKey(String key) {

        PlatformConfig config = platformConfigRepo.findByKey(key).orElse(null);

        if (config == null) return null;

        Map<String, Object> data = new HashMap<>();
        Map<String, Object> value = (Map<String, Object>) config.getValue();
        data.put(key, value);

        return data;
    }


    @Override
    @Transactional
    public ResponseEntity<ResponseObject> updateConfigData(CreateConfigDataRequest request) {

        if (request.getBusinessData() == null
                && request.getMediaData() == null
                && request.getAdmissionQuotaData() == null
                && request.getAdmissionSettingsData() == null
        ) {
            return ResponseBuilder.build(
                    HttpStatus.BAD_REQUEST,
                    "Thiếu dữ liệu",
                    null
            );
        }

        if (request.getBusinessData() != null) {
            CreateConfigDataRequest.BusinessData businessData = request.getBusinessData();
            if (businessData.getSubscriptionPricing() == null
                    || businessData.getSubscriptionPricing().getBasePrices() == null) {
                return ResponseBuilder.build(
                        HttpStatus.BAD_REQUEST,
                        "Thiếu cấu hình giá cơ bản cho gói dịch vụ.",
                        null
                );
            }
        }

        try {
            updateConfig(request);
        } catch (RuntimeException e) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, e.getMessage(), null);
        }
        return ResponseBuilder.build(
                HttpStatus.OK,
                "Cập nhật thành công",
                null
        );
    }

    @Transactional
    public void updateConfig(CreateConfigDataRequest request) {
        if (request.getBusinessData() != null) updateBusiness(request);
        if (request.getMediaData() != null) updateMedia(request);
        if (request.getAdmissionQuotaData() != null) updateAdmissionQuota(request);
        if (request.getAdmissionSettingsData() != null) updateAdmissionSettingsTemplate(request);
    }

    @Transactional
    public void updateBusiness(CreateConfigDataRequest request) {
        CreateConfigDataRequest.BusinessData business = request.getBusinessData();
        Map<String, Object> businessJson = new HashMap<>();

        businessJson.put("taxRate", business.getTaxRate());
        businessJson.put("serviceRate", business.getServiceRate());
        businessJson.put("minPay", business.getMinPay());
        businessJson.put("maxPay", business.getMaxPay());

        BigDecimal trialBasePrice = business.getSubscriptionPricing().getBasePrices().getTrial();
        BigDecimal standardBasePrice = business.getSubscriptionPricing().getBasePrices().getStandard();
        BigDecimal enterpriseBasePrice = business.getSubscriptionPricing().getBasePrices().getEnterprise();

        if (trialBasePrice == null || standardBasePrice == null || enterpriseBasePrice == null) {
            throw new RuntimeException("Thiếu cấu hình giá nền cho một hoặc nhiều gói (trial/standard/enterprise).");
        }

        if (standardBasePrice.compareTo(enterpriseBasePrice) > 0) {
            throw new RuntimeException("Giá nền gói dịch vụ phải tăng dần: Standard <= Enterprise.");
        }

        Map<String, Object> basePrices = new HashMap<>();
        basePrices.put("trial", BigDecimal.ZERO);
        basePrices.put("standard", business.getSubscriptionPricing().getBasePrices().getStandard());
        basePrices.put("enterprise", business.getSubscriptionPricing().getBasePrices().getEnterprise());

        Map<String, Object> featureUnitPrices = new HashMap<>();
        featureUnitPrices.put("aiChatbotMonthlyFee", business.getSubscriptionPricing().getFeatureUnitPrices().getAiChatbotMonthlyFee());
        featureUnitPrices.put("premiumSupportFee", business.getSubscriptionPricing().getFeatureUnitPrices().getPremiumSupportFee());

        var quotas = business.getSubscriptionPricing().getPackageQuotas();
        if (quotas == null) {
            throw new RuntimeException("Thiếu cấu hình định mức trong phần định giá gói dịch vụ.");
        }

        // Lấy tỉ lệ giới hạn Dùng thử do Admin cấu hình
        double trialRatioCap = business.getSubscriptionPricing().getTrialRatioCap();
        if (trialRatioCap <= 0 || trialRatioCap >= 1) {
            throw new RuntimeException("Tỉ lệ giới hạn gói Dùng thử phải nằm trong khoảng từ 0 đến 1 (ví dụ: 0.3 cho 30%).");
        }

        int durationDays = quotas.getDurationDays();
        int trialCounsellor = quotas.getTrialCounsellor();
        int standardCounsellor = quotas.getStandardCounsellor();
        int enterpriseCounsellor = quotas.getEnterpriseCounsellor();
        int trialPostLimit = quotas.getTrialPostLimit();
        int standardPostLimit = quotas.getStandardPostLimit();
        int enterprisePostLimit = quotas.getEnterprisePostLimit();

        if (durationDays <= 0) {
            throw new RuntimeException("Thời hạn gói phải lớn hơn 0.");
        }

        if (trialCounsellor < 0 || standardCounsellor < 0 || enterpriseCounsellor < 0) {
            throw new RuntimeException("Giới hạn bài đăng phải tăng dần theo cấp độ: Dùng thử <= Tiêu chuẩn <= Doanh nghiệp. Vui lòng đảm bảo số lượng tư vấn viên là số nguyên dương hoặc bằng 0.");
        }
        if (!(trialCounsellor <= standardCounsellor && standardCounsellor <= enterpriseCounsellor)) {
            throw new RuntimeException("Các giá trị counsellor phải không giảm theo thứ tự các cấp: trial <= standard <= enterprise");
        }

        if (trialPostLimit < 0 || standardPostLimit < 0 || enterprisePostLimit < 0) {
            throw new RuntimeException("Các giá trị postLimit phải lớn hơn hoặc bằng 0");
        }
        if (!(trialPostLimit <= standardPostLimit && standardPostLimit <= enterprisePostLimit)) {
            throw new RuntimeException("Các giá trị postLimit phải không giảm theo thứ tự các cấp: trial <= standard <= enterprise");
        }

        // Kiểm tra ràng buộc tỉ lệ với tỉ lệ Admin vừa nhập
        int maxTrialCounsellorByRatio = (int) Math.floor(standardCounsellor * trialRatioCap);
        if (trialCounsellor > maxTrialCounsellorByRatio) {
            throw new RuntimeException(String.format(
                    "Với tỉ lệ cấu hình là %d%%, số tư vấn viên dùng thử không được vượt quá %d.",
                    (int) (trialRatioCap * 100),
                    maxTrialCounsellorByRatio
            ));
        }

        int maxTrialPostLimitByRatio = (int) Math.floor(standardPostLimit * trialRatioCap);
        if (trialPostLimit > maxTrialPostLimitByRatio) {
            throw new RuntimeException(String.format(
                    "Với tỉ lệ cấu hình là %d%%, giới hạn bài đăng dùng thử không được vượt quá %d.",
                    (int) (trialRatioCap * 100),
                    maxTrialPostLimitByRatio
            ));
        }

        Map<String, Object> packageQuotas = new HashMap<>();
        packageQuotas.put("durationDays", durationDays);
        packageQuotas.put("trialCounsellor", trialCounsellor);
        packageQuotas.put("standardCounsellor", standardCounsellor);
        packageQuotas.put("enterpriseCounsellor", enterpriseCounsellor);
        packageQuotas.put("trialPostLimit", trialPostLimit);
        packageQuotas.put("standardPostLimit", standardPostLimit);
        packageQuotas.put("enterprisePostLimit", enterprisePostLimit);

        Map<String, Object> subscriptionPricing = new HashMap<>();
        subscriptionPricing.put("basePrices", basePrices);
        subscriptionPricing.put("featureUnitPrices", featureUnitPrices);
        subscriptionPricing.put("packageQuotas", packageQuotas);
        subscriptionPricing.put("trialRatioCap", trialRatioCap);

        businessJson.put("subscriptionPricing", subscriptionPricing);

        PlatformConfig config = platformConfigRepo.findByKey("business").orElse(
                PlatformConfig.builder()
                        .key("business")
                        .creationDate(LocalDateTime.now())
                        .build()
        );

        config.setValue(businessJson);
        config.setModifiedDate(LocalDateTime.now());
        platformConfigRepo.save(config);

        List<Subscription> activeSubscriptions = subscriptionRepo.findAllByPackageStatus(Status.PACKAGE_ACTIVE);

        for (Subscription sub : activeSubscriptions) {

            UpsertServicePackageFeeRequest tempRequest = convertToRequest(sub);

            ConfigSystemUtil.SubscriptionPriceBreakdown newBreakdown = ConfigSystemUtil.calculateSubscriptionPriceBreakdown(tempRequest, businessJson);
            // Chỉ xử lý nếu giá thực sự thay đổi
            if (sub.getFinalPrice().compareTo(newBreakdown.finalPrice()) != 0) {

                // Kiểm tra xem đã có School nào mua/đang dùng gói này chưa
                boolean hasPurchased = schoolSubscriptionRepo.existsBySubscriptionAndEndDateAfter(sub, LocalDate.now());

                if (hasPurchased) {
                    //case co ng mua -> Chuyển sang PENDING để "Lock", không cho mua mới/gia hạn
                    sub.setPackageStatus(Status.PACKAGE_INACTIVE_PENDING);
                    //luu gia moi vao trường new_price để Admin biết tại sao gói này bị deactive
                    //luu ngay ap dung gia moi
                    sub.setNewPrice(newBreakdown.finalPrice());
                    sub.setPriceApplyDate(LocalDate.now());
                } else {
                    // case chưa ai dùng -> chuyển deactive
                    sub.setPackageStatus(Status.PACKAGE_DEACTIVATED);
                }
            }
        }
        subscriptionRepo.saveAll(activeSubscriptions);
    }

    private UpsertServicePackageFeeRequest convertToRequest(Subscription sub) {
        UpsertServicePackageFeeRequest request = new UpsertServicePackageFeeRequest();
        request.setPackageType(sub.getPackageType().name());
        request.setName(sub.getName());
        request.setDurationDays(sub.getDurationDays());

        Map<String, Object> featureMap = (Map<String, Object>) sub.getFeatures();

        UpsertServicePackageFeeRequest.FeatureData featureData = new UpsertServicePackageFeeRequest.FeatureData();

        featureData.setMaxCounsellors(featureMap.get("maxCounsellors") != null ? ((Number) featureMap.get("maxCounsellors")).intValue() : null);
        featureData.setPostLimit(featureMap.get("postLimit") != null ? ((Number) featureMap.get("postLimit")).intValue() : null);
        featureData.setHasAiAssistant((Boolean) featureMap.get("hasAiAssistant"));
        featureData.setParentPostPermission((String) featureMap.get("parentPostPermission"));
        featureData.setSupportLevel((String) featureMap.get("supportLevel"));

        request.setFeatureData(featureData);
        return request;
    }

    @Transactional
    public void updateMedia(CreateConfigDataRequest request) {
        CreateConfigDataRequest.MediaData media = request.getMediaData();

        validateMediaConfigRequest(media);

        Map<String, Object> mediaJson = new HashMap<>();

        mediaJson.put("maxImgSize", media.getMaxImgSize());
        mediaJson.put("maxDocSize", media.getMaxDocSize());

        mediaJson.put("imgFormat", mapFormats(media.getImgFormats()));
        mediaJson.put("docFormat", mapFormats(media.getDocFormats()));

        PlatformConfig config = platformConfigRepo.findByKey("media").orElse(
                PlatformConfig.builder()
                        .key("media")
                        .creationDate(LocalDateTime.now())
                        .build()
        );

        config.setValue(mediaJson);
        config.setModifiedDate(LocalDateTime.now());
        platformConfigRepo.save(config);
    }

    private List<Map<String, String>> mapFormats(List<CreateConfigDataRequest.MediaFormat> formats) {
        if (formats == null) return Collections.emptyList();

        return formats.stream()
                .map(f -> {
                    String ext = f.getFormat().trim().toLowerCase();
                    // Nếu Admin quên nhập dấu chấm, mình sẽ tự thêm vào (ví dụ: "pdf" -> ".pdf")
                    if (!ext.startsWith(".")) {
                        ext = "." + ext;
                    }
                    Map<String, String> map = new HashMap<>();
                    map.put("format", ext);
                    return map;
                })
                .toList();
    }

    private void validateMediaConfigRequest(CreateConfigDataRequest.MediaData media) {
        if (media == null) {
            throw new RuntimeException("Dữ liệu cấu hình không được để trống");
        }

        if (media.getImgFormats() == null || media.getImgFormats().isEmpty()) {
            throw new RuntimeException("Danh sách định dạng ảnh không được để trống");
        }
        if (media.getDocFormats() == null || media.getDocFormats().isEmpty()) {
            throw new RuntimeException("Danh sách định dạng tài liệu không được để trống");
        }

        validateFormatStrings(media.getImgFormats(), "Ảnh");
        validateFormatStrings(media.getDocFormats(), "Tài liệu");
    }

    private void validateFormatStrings(List<CreateConfigDataRequest.MediaFormat> formats, String typeLabel) {
        for (CreateConfigDataRequest.MediaFormat format : formats) {
            if (format.getFormat() == null) {
                throw new RuntimeException("Định dạng " + typeLabel + " không được để trống");
            }

            String fmt = format.getFormat().trim();
            if (fmt.isEmpty() || !fmt.matches("^[a-zA-Z0-9.]+$")) {
                throw new RuntimeException("Định dạng " + typeLabel + " '" + fmt + "' không hợp lệ");
            }
        }
    }

    @Transactional
    public void updateAdmissionQuota(CreateConfigDataRequest request) {
        CreateConfigDataRequest.AdmissionQuotaData admissionQuota = request.getAdmissionQuotaData();

        List<Map<String, Object>> quotaAIData = admissionQuota.getQuotas().stream()
                .filter(q -> schoolRepo.existsById(q.getSchoolId()))
                .map(q -> {
                    School school = schoolRepo.findById(q.getSchoolId()).orElse(null);
                    assert school != null;

                    Map<String, Object> data = new HashMap<>();
                    data.put("schoolId", school.getId());
                    data.put("schoolName", school.getName());
                    data.put("value", q.getValue());
                    return data;
                })
                .toList();

        Map<String, Object> sourceData = new HashMap<>();

        sourceData.put("sourceName", admissionQuota.getSource().getSourceName());
        sourceData.put("sourceType", admissionQuota.getSource().getSourceType());
        sourceData.put("sourceUrl", admissionQuota.getSource().getSourceUrl());
        sourceData.put("year", admissionQuota.getSource().getYear());

        Map<String, Object> quotaData = new HashMap<>();
        quotaData.put("source", sourceData);
        quotaData.put("quotas", quotaAIData);

        PlatformConfig config = platformConfigRepo.findByKey("admissionQuota")
                .orElse(PlatformConfig.builder()
                        .key("admissionQuota")
                        .creationDate(LocalDateTime.now())
                        .build());

        assert config != null;
        config.setValue(quotaData);
        config.setModifiedDate(LocalDateTime.now());
        platformConfigRepo.save(config);
    }

    @Transactional
    public void updateAdmissionSettingsTemplate(CreateConfigDataRequest request) {
        CreateConfigDataRequest.AdmissionSettingsData admissionSettingsData = request.getAdmissionSettingsData();

        // T them hinh anh mau cho OCR hoc ba
        if (admissionSettingsData.getTranscriptTemplateImageUrl() == null || admissionSettingsData.getTranscriptTemplateImageUrl().trim().isBlank()){
            throw new RuntimeException("URL hình ảnh mẫu học bạ không được để trống.");
        }

        List<Map<String, Object>> allowedMethodsJson = new ArrayList<>();
        if (admissionSettingsData.getAllowedMethods() != null) {
            allowedMethodsJson = admissionSettingsData.getAllowedMethods().stream()
                    .map(method -> {
                        Map<String, Object> row = new HashMap<>();
                        row.put("code", method.getCode());
                        row.put("displayName", method.getDisplayName());
                        row.put("description", method.getDescription());
                        return row;
                    })
                    .collect(Collectors.toList());
        }

        List<String> validMethodCodes = allowedMethodsJson.stream()
                .map(m -> m.get("code").toString())
                .toList();

        List<Map<String, Object>> admissionProcessesJson = new ArrayList<>();
        if (admissionSettingsData.getMethodAdmissionProcess() != null) {
            for (var methodProcess : admissionSettingsData.getMethodAdmissionProcess()) {
                if (!validMethodCodes.contains(methodProcess.getMethodCode())) {
                    throw new RuntimeException("Mã phương pháp " + methodProcess.getMethodCode() + " không hợp lệ.");
                }

                Map<String, Object> processMap = new HashMap<>();
                processMap.put("methodCode", methodProcess.getMethodCode());
                processMap.put("steps", methodProcess.getSteps() == null
                        ? Collections.emptyList()
                        : methodProcess.getSteps().stream()
                        .map(step -> {
                            Map<String, Object> stepData = new HashMap<>();
                            stepData.put("stepOrder", step.getStepOrder());
                            stepData.put("stepName", step.getStepName());
                            stepData.put("description", step.getDescription());
                            return stepData;
                        })
                        .collect(Collectors.toList()));
                admissionProcessesJson.add(processMap);
            }
        }

        List<Map<String, Object>> mandatoryAllJson = new ArrayList<>();
        List<Map<String, Object>> byMethodJson = new ArrayList<>();

        CreateConfigDataRequest.DocumentRequirementsData documentRequirementsData = admissionSettingsData.getDocumentRequirementsData();
        if (documentRequirementsData != null) {
            if (documentRequirementsData.getMandatoryAll() != null) {
                mandatoryAllJson = documentRequirementsData.getMandatoryAll().stream()
                        .map(doc -> {
                            Map<String, Object> docData = new HashMap<>();
                            docData.put("code", doc.getCode());
                            docData.put("name", doc.getName());
                            docData.put("required", true);
                            docData.put("validateCriterion", doc.getValidateCriterion() != null ? doc.getValidateCriterion() : List.of());
                            docData.put("templateFileUrl", doc.getTemplateFileUrl());
                            return docData;
                        })
                        .collect(Collectors.toList());
            }

            if (documentRequirementsData.getByMethod() != null) {
                for (var methodRequirement : documentRequirementsData.getByMethod()) {
                    if (!validMethodCodes.contains(methodRequirement.getMethodCode())) {
                        throw new RuntimeException("Mã phương pháp " + methodRequirement.getMethodCode() + " không hợp lệ.");
                    }

                    Map<String, Object> requirementMap = new HashMap<>();
                    requirementMap.put("methodCode", methodRequirement.getMethodCode());
                    requirementMap.put("documents", methodRequirement.getDocuments() == null
                            ? Collections.emptyList()
                            : methodRequirement.getDocuments().stream()
                            .map(doc -> {
                                Map<String, Object> docData = new HashMap<>();
                                docData.put("code", doc.getCode());
                                docData.put("name", doc.getName());
                                docData.put("required", doc.isRequired());
                                docData.put("templateUrl", doc.getTemplateUrl() != null ? doc.getTemplateUrl() : "");
                                return docData;
                            })
                            .collect(Collectors.toList()));
                    byMethodJson.add(requirementMap);
                }
            }
        }

        Map<String, Object> documentRequirementsTemplateJson = new HashMap<>();
        documentRequirementsTemplateJson.put("mandatoryAll", mandatoryAllJson);
        documentRequirementsTemplateJson.put("byMethod", byMethodJson);
        documentRequirementsTemplateJson.put("transcriptTemplateImageUrl", admissionSettingsData.getTranscriptTemplateImageUrl().trim());

        Map<String, Object> admissionJson = new HashMap<>();
        admissionJson.put("allowedMethods", allowedMethodsJson);
        admissionJson.put("admissionProcesses", admissionProcessesJson);
        admissionJson.put("documentRequirementsData", documentRequirementsTemplateJson);

        PlatformConfig config = platformConfigRepo.findByKey("admissionSettingsData").orElse(
                PlatformConfig.builder()
                        .key("admissionSettingsData")
                        .creationDate(LocalDateTime.now())
                        .build()
        );

        config.setValue(admissionJson);
        config.setModifiedDate(LocalDateTime.now());
        platformConfigRepo.save(config);
    }

    @Override
    public ResponseEntity<ResponseObject> importPreview(MultipartFile file, ImportType type) {
        validateImportType(type);
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheet(type.getSheetName());
            if (sheet == null) {
                return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Không tìm thấy sheet: " + type.getSheetName(), null);
            }

            DataFormatter formatter = new DataFormatter();
            List<ImportConfirmRequest.ImportRow> resultRows = new ArrayList<>();

            Map<String, Object> configValue = getAdmissionConfigMap();
            Set<String> dbValidCodes = getExistingKeys(configValue, ImportType.ALLOWED_METHODS);

            Set<String> fileLevelCodes = new HashSet<>();
            Set<String> fileLevelAdmissionKeys = new HashSet<>();
            Set<String> fileLevelMethodDocKeys = new HashSet<>();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) continue;

                // 1. Map row to RowData
                Map<String, Object> rowData = mapRowToMap(row, type, formatter, i + 1);

                // 2. Validate RowData with file-level duplicate checking
                ImportConfirmRequest.Error error = SystemConfigValidation.validateRowData(
                        rowData, type,
                        fileLevelCodes, fileLevelAdmissionKeys, fileLevelMethodDocKeys,
                        dbValidCodes
                );

                // 3. Package into ImportRow
                resultRows.add(ImportConfirmRequest.ImportRow.builder()
                        .rowData(rowData)
                        .error(error)
                        .isError(error != null)
                        .isDeleted(false)
                        .build());
            }

            return ResponseBuilder.build(HttpStatus.OK, "Xem trước thành công", resultRows);
        } catch (Exception e) {
            return ResponseBuilder.build(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi: " + e.getMessage(), null);
        }
    }

    private void validateImportType(ImportType type) {
        List<ImportType> allowedTypes = List.of(
                ImportType.ALLOWED_METHODS,
                ImportType.ADMISSION_PROCESSES,
                ImportType.METHOD_DOCUMENTS,
                ImportType.MANDATORY_ALL
        );
        if (!allowedTypes.contains(type)) {
            throw new IllegalArgumentException("Loại import không hợp lệ hoặc không được hỗ trợ.");
        }
    }

    @Override
    public ResponseEntity<ResponseObject> importConfirm(ImportConfirmRequest request, ImportType type) {
        try {
            validateImportType(type);
            if (request.getRows() == null || request.getRows().isEmpty()) {
                return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Dữ liệu trống", null);
            }

            Map<String, Object> configValue = getAdmissionConfigMap();

            Set<String> dbValidCodes = getExistingKeys(configValue, ImportType.ALLOWED_METHODS);

            Set<String> fileLevelCodes = new HashSet<>();
            Set<String> fileLevelAdmissionKeys = new HashSet<>();
            Set<String> fileLevelMethodDocKeys = new HashSet<>();

            List<ImportConfirmRequest.ImportRow> rows = request.getRows();

            rows.stream()
                    .filter(r -> !Boolean.TRUE.equals(r.getIsDeleted()))
                    .forEach(row -> {
                        var error = SystemConfigValidation.validateRowData(
                                row.getRowData(), type,
                                fileLevelCodes, fileLevelAdmissionKeys, fileLevelMethodDocKeys,
                                dbValidCodes
                        );
                        row.setError(error);
                        row.setIsError(error != null);
                    });

            if (rows.stream()
                    .filter(r -> !Boolean.TRUE.equals(r.getIsDeleted()))
                    .anyMatch(r -> Boolean.TRUE.equals(r.getIsError()))) {
                return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Dữ liệu không hợp lệ", rows);
            }

            List<Map<String, Object>> existingData = SystemConfigValidation.getExistingFlatData(configValue, type);
            Map<String, Map<String, Object>> mergedMap = new LinkedHashMap<>();

            existingData.forEach(m -> mergedMap.put(SystemConfigValidation.getBusinessKey(m, type), m));

            rows.forEach(r -> {
                String businessKey = SystemConfigValidation.getBusinessKey((Map<String, Object>) r.getRowData(), type);

                if (Boolean.TRUE.equals(r.getIsDeleted())) {
                    mergedMap.remove(businessKey);
                } else {
                    Map<String, Object> rowData = new HashMap<>((Map<String, Object>) r.getRowData());
                    if (mergedMap.containsKey(businessKey)) {
                        Map<String, Object> existing = mergedMap.get(businessKey);
                        if (type == ImportType.MANDATORY_ALL) {
                            Object existingValidateCriterion = existing.get("validateCriterion");
                            if (existingValidateCriterion != null)
                                rowData.put("validateCriterion", existingValidateCriterion);
                            Object existingTemplateFileUrl = existing.get("templateFileUrl");
                            if (existingTemplateFileUrl != null)
                                rowData.put("templateFileUrl", existingTemplateFileUrl);
                        }
                        if (type == ImportType.METHOD_DOCUMENTS) {
                            Object existingTemplateUrl = existing.get("templateUrl");
                            if (existingTemplateUrl != null) rowData.put("templateUrl", existingTemplateUrl);
                        }
                    }
                    mergedMap.put(businessKey, rowData);
                }
            });

            List<Map<String, Object>> finalData = new ArrayList<>(mergedMap.values());

            List<Map<String, Object>> nestedData = SystemConfigValidation.groupToNestedStructure(finalData, type);
            SystemConfigValidation.syncLegacyData(configValue, nestedData, type);

            if (type == ImportType.ALLOWED_METHODS) {
                SystemConfigValidation.cascadeDeleteOnMethodChange(configValue, nestedData);
            }

            PlatformConfig config = platformConfigRepo.findByKey("admissionSettingsData")
                    .orElse(PlatformConfig.builder().key("admissionSettingsData").value(new HashMap<>()).build());

            config.setValue(configValue);
            config.setModifiedDate(LocalDateTime.now());
            platformConfigRepo.save(config);

            return ResponseBuilder.build(HttpStatus.OK, "Lưu dữ liệu " + type + " thành công", null);
        } catch (Exception e) {
            return ResponseBuilder.build(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi lưu dữ liệu: " + e.getMessage(), null);
        }
    }

    @Override
    public ResponseEntity<ResponseObject> validateSingleRow(ImportConfirmRequest request, ImportType type) {
        if (request.getRows() == null || request.getRows().isEmpty()) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Dữ liệu trống", null);
        }

        validateImportType(type);

        Map<String, Object> configValue = getAdmissionConfigMap();

        Set<String> dbValidCodes = getExistingKeys(configValue, ImportType.ALLOWED_METHODS);

        Set<String> fileLevelCodes = new HashSet<>();
        Set<String> fileLevelAdmissionKeys = new HashSet<>();
        Set<String> fileLevelMethodDocKeys = new HashSet<>();

        List<ImportConfirmRequest.ImportRow> rows = request.getRows();

        rows.forEach(row -> {
            ImportConfirmRequest.Error error = SystemConfigValidation.validateRowData(
                    row.getRowData(), type,
                    fileLevelCodes, fileLevelAdmissionKeys, fileLevelMethodDocKeys,
                    dbValidCodes
            );
            row.setError(error);
            row.setIsError(error != null);
        });

        return ResponseBuilder.build(HttpStatus.OK, "Validate thành công", rows);
    }

    private Map<String, Object> mapRowToMap(Row row, ImportType type, DataFormatter formatter, int index) {
        Map<String, Object> rowData = new LinkedHashMap<>();
        rowData.put("index", index);

        switch (type) {
            case ALLOWED_METHODS -> {
                rowData.put("code", cellText(row.getCell(0), formatter));
                rowData.put("displayName", cellText(row.getCell(1), formatter));
                rowData.put("description", cellText(row.getCell(2), formatter));
            }
            case ADMISSION_PROCESSES -> {
                rowData.put("methodCode", cellText(row.getCell(0), formatter));
                rowData.put("stepOrder", cellText(row.getCell(1), formatter));
                rowData.put("stepName", cellText(row.getCell(2), formatter));
                rowData.put("description", cellText(row.getCell(3), formatter));
            }
            case METHOD_DOCUMENTS -> {
                rowData.put("methodCode", cellText(row.getCell(0), formatter));
                rowData.put("code", cellText(row.getCell(1), formatter));
                rowData.put("name", cellText(row.getCell(2), formatter));
                rowData.put("required", parseBoolean(cellText(row.getCell(3), formatter)));
            }

            case MANDATORY_ALL -> {
                rowData.put("code", cellText(row.getCell(0), formatter));
                rowData.put("name", cellText(row.getCell(1), formatter));
                rowData.put("required", parseBoolean(cellText(row.getCell(2), formatter)));
            }
        }
        return rowData;
    }

    private String cellText(Cell cell, DataFormatter formatter) {
        if (cell == null) return "";
        return formatter.formatCellValue(cell).trim();
    }

    private boolean parseBoolean(String raw) {
        if (raw == null) return false;
        String v = raw.trim().toLowerCase();
        return List.of("true", "1", "yes", "y", "x").contains(v);
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) return true;
        for (int cellNum = row.getFirstCellNum(); cellNum < row.getLastCellNum(); cellNum++) {
            Cell cell = row.getCell(cellNum);
            if (cell != null && cell.getCellType() != CellType.BLANK && !cell.toString().isBlank()) {
                return false;
            }
        }
        return true;
    }

    private Map<String, Object> getAdmissionConfigMap() {
        return platformConfigRepo.findByKey("admissionSettingsData")
                .map(config -> config.getValue() instanceof Map ? (Map<String, Object>) config.getValue() : new HashMap<String, Object>())
                .orElse(new HashMap<>());
    }

    private Set<String> getExistingKeys(Map<String, Object> config, ImportType type) {
        Object data = config.get(type.getSheetName());
        if (!(data instanceof List<?> list)) return new HashSet<>();
        return list.stream()
                .filter(Map.class::isInstance)
                .map(m -> SystemConfigValidation.getBusinessKey((Map<String, Object>) m, type))
                .filter(k -> !k.isBlank())
                .collect(Collectors.toCollection(HashSet::new));
    }
}
