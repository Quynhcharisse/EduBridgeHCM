package com.sp26se041.edubridgehcm.services.implementors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sp26se041.edubridgehcm.models.PlatformConfig;
import com.sp26se041.edubridgehcm.models.School;
import com.sp26se041.edubridgehcm.repositories.PlatformConfigRepo;
import com.sp26se041.edubridgehcm.repositories.SchoolRepo;
import com.sp26se041.edubridgehcm.requests.CreateConfigDataRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.SystemService;
import com.sp26se041.edubridgehcm.utils.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
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

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SystemServiceImpl implements SystemService {

    private final PlatformConfigRepo platformConfigRepo;

    private final SchoolRepo schoolRepo;

    private final ObjectMapper objectMapper;

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

            double trialPrice = businessData.getSubscriptionPricing().getBasePrices().getTrial();
            if (Double.compare(trialPrice, 0d) != 0) {
                return ResponseBuilder.build(
                        HttpStatus.BAD_REQUEST,
                        "Giá gói trial phải luôn bằng 0.",
                        null
                );
            }
        }

        updateConfig(request);
        return ResponseBuilder.build(
                HttpStatus.OK,
                "Cập nhật thành công",
                null
        );
    }

    @Override
    @Transactional
    public ResponseEntity<ResponseObject> importAdmissionTemplate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "File không được để trống", null);
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Tên file không hợp lệ", null);
        }

        try {
            String lowerName = originalName.toLowerCase();
            Map<String, Object> admissionTemplate;

            if (lowerName.endsWith(".xlsx")) {
                admissionTemplate = buildAdmissionTemplateFromExcel(file);
            } else {
                return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Chỉ hỗ trợ file .xlsx", null);
            }

            PlatformConfig config = platformConfigRepo.findByKey("admissionSettingsData").orElse(
                    PlatformConfig.builder()
                            .key("admissionSettingsData")
                            .creationDate(LocalDateTime.now())
                            .build()
            );

            config.setValue(admissionTemplate);
            config.setModifiedDate(LocalDateTime.now());
            platformConfigRepo.save(config);

            return ResponseBuilder.build(HttpStatus.OK, "Import template tuyển sinh thành công", admissionTemplate);
        } catch (IllegalArgumentException e) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, e.getMessage(), null);
        } catch (IOException e) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Không đọc được file import", null);
        } catch (Exception e) {
            return ResponseBuilder.build(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi hệ thống khi import template", null);
        }
    }

    private Map<String, Object> buildAdmissionTemplateFromExcel(MultipartFile file) throws IOException {
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            List<Map<String, Object>> allowedMethods = parseAllowedMethodsSheet(workbook.getSheet("allowedMethods"));
            Set<String> validMethodCodes = allowedMethods.stream()
                    .map(m -> String.valueOf(m.get("code")).trim())
                    .collect(Collectors.toCollection(HashSet::new));

            List<Map<String, Object>> admissionProcesses = parseAdmissionProcessesSheet(
                    workbook.getSheet("admissionProcesses"),
                    validMethodCodes
            );
            List<Map<String, Object>> mandatoryAll = parseDocumentsSheet(workbook.getSheet("mandatoryAll"));
            List<Map<String, Object>> byMethod = parseByMethodDocumentsSheet(
                    workbook.getSheet("byMethod"),
                    validMethodCodes
            );

            Map<String, Object> documentRequirementsData = new HashMap<>();
            documentRequirementsData.put("mandatoryAll", mandatoryAll);
            documentRequirementsData.put("byMethod", byMethod);

            Map<String, Object> admissionTemplate = new HashMap<>();
            admissionTemplate.put("allowedMethods", allowedMethods);
            admissionTemplate.put("admissionProcesses", admissionProcesses);
            admissionTemplate.put("documentRequirementsData", documentRequirementsData);
            admissionTemplate.put("byMethod", byMethod);
            admissionTemplate.put("methodDocumentRequirements", byMethod);
            return admissionTemplate;
        }
    }

    private List<Map<String, Object>> parseAllowedMethodsSheet(Sheet sheet) {
        if (sheet == null) {
            throw new IllegalArgumentException("Thiếu sheet allowedMethods");
        }

        DataFormatter formatter = new DataFormatter();
        List<Map<String, Object>> methods = new ArrayList<>();
        Set<String> seenCodes = new HashSet<>();

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            String code = cellText(row.getCell(0), formatter);
            String displayName = cellText(row.getCell(1), formatter);
            String description = cellText(row.getCell(2), formatter);

            if (code.isBlank() && displayName.isBlank() && description.isBlank()) continue;
            if (code.isBlank() || displayName.isBlank()) {
                throw new IllegalArgumentException("Sheet allowedMethods thiếu code/displayName tại dòng " + (i + 1));
            }
            if (!seenCodes.add(code.toLowerCase())) {
                throw new IllegalArgumentException("Phương thức tuyển sinh bị trùng: " + code);
            }

            Map<String, Object> item = new HashMap<>();
            item.put("code", code);
            item.put("displayName", displayName);
            item.put("description", description);
            methods.add(item);
        }

        if (methods.isEmpty()) {
            throw new IllegalArgumentException("Sheet allowedMethods không có dữ liệu");
        }
        return methods;
    }

    private List<Map<String, Object>> parseAdmissionProcessesSheet(Sheet sheet, Set<String> validMethodCodes) {
        if (sheet == null) return Collections.emptyList();

        DataFormatter formatter = new DataFormatter();
        Map<String, List<Map<String, Object>>> grouped = new HashMap<>();

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            String methodCode = cellText(row.getCell(0), formatter);
            String stepOrderRaw = cellText(row.getCell(1), formatter);
            String stepName = cellText(row.getCell(2), formatter);
            String description = cellText(row.getCell(3), formatter);

            if (methodCode.isBlank() && stepOrderRaw.isBlank() && stepName.isBlank() && description.isBlank()) continue;
            if (methodCode.isBlank() || stepOrderRaw.isBlank() || stepName.isBlank()) {
                throw new IllegalArgumentException("Sheet admissionProcesses thiếu dữ liệu tại dòng " + (i + 1));
            }
            if (!validMethodCodes.contains(methodCode)) {
                throw new IllegalArgumentException("methodCode không tồn tại trong allowedMethods: " + methodCode);
            }

            int stepOrder;
            try {
                stepOrder = Integer.parseInt(stepOrderRaw);
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("stepOrder không hợp lệ tại dòng " + (i + 1));
            }

            Map<String, Object> step = new HashMap<>();
            step.put("stepOrder", stepOrder);
            step.put("stepName", stepName);
            step.put("description", description);

            grouped.computeIfAbsent(methodCode, k -> new ArrayList<>()).add(step);
        }

        List<Map<String, Object>> processes = new ArrayList<>();
        for (Map.Entry<String, List<Map<String, Object>>> entry : grouped.entrySet()) {
            Map<String, Object> process = new HashMap<>();
            process.put("methodCode", entry.getKey());
            process.put("steps", entry.getValue());
            processes.add(process);
        }

        return processes;
    }

    private List<Map<String, Object>> parseDocumentsSheet(Sheet sheet) {
        if (sheet == null) return Collections.emptyList();

        DataFormatter formatter = new DataFormatter();
        List<Map<String, Object>> docs = new ArrayList<>();

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            String code = cellText(row.getCell(0), formatter);
            String name = cellText(row.getCell(1), formatter);
            String requiredRaw = cellText(row.getCell(2), formatter);

            if (code.isBlank() && name.isBlank() && requiredRaw.isBlank()) continue;
            if (code.isBlank() || name.isBlank()) {
                throw new IllegalArgumentException("Sheet mandatoryAll thiếu code/name tại dòng " + (i + 1));
            }

            Map<String, Object> doc = new HashMap<>();
            doc.put("code", code);
            doc.put("name", name);
            doc.put("required", parseBoolean(requiredRaw));
            docs.add(doc);
        }

        return docs;
    }

    private List<Map<String, Object>> parseByMethodDocumentsSheet(Sheet sheet, Set<String> validMethodCodes) {
        if (sheet == null) return Collections.emptyList();

        DataFormatter formatter = new DataFormatter();
        Map<String, List<Map<String, Object>>> grouped = new HashMap<>();

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            String methodCode = cellText(row.getCell(0), formatter);
            String code = cellText(row.getCell(1), formatter);
            String name = cellText(row.getCell(2), formatter);
            String requiredRaw = cellText(row.getCell(3), formatter);

            if (methodCode.isBlank() && code.isBlank() && name.isBlank() && requiredRaw.isBlank()) continue;
            if (methodCode.isBlank() || code.isBlank() || name.isBlank()) {
                throw new IllegalArgumentException("Sheet byMethod thiếu methodCode/code/name tại dòng " + (i + 1));
            }
            if (!validMethodCodes.contains(methodCode)) {
                throw new IllegalArgumentException("methodCode không tồn tại trong allowedMethods: " + methodCode);
            }

            Map<String, Object> doc = new HashMap<>();
            doc.put("code", code);
            doc.put("name", name);
            doc.put("required", parseBoolean(requiredRaw));
            grouped.computeIfAbsent(methodCode, k -> new ArrayList<>()).add(doc);
        }

        List<Map<String, Object>> byMethod = new ArrayList<>();
        for (Map.Entry<String, List<Map<String, Object>>> entry : grouped.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("methodCode", entry.getKey());
            item.put("documents", entry.getValue());
            byMethod.add(item);
        }
        return byMethod;
    }

    private String cellText(Cell cell, DataFormatter formatter) {
        if (cell == null) return "";
        return formatter.formatCellValue(cell).trim();
    }

    private boolean parseBoolean(String raw) {
        if (raw == null || raw.isBlank()) return false;
        String value = raw.trim().toLowerCase();
        return value.equals("true") || value.equals("1") || value.equals("yes") || value.equals("y");
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

        Map<String, Object> basePrices = new HashMap<>();
        basePrices.put("trial", business.getSubscriptionPricing().getBasePrices().getTrial());
        basePrices.put("standard", business.getSubscriptionPricing().getBasePrices().getStandard());
        basePrices.put("enterprise", business.getSubscriptionPricing().getBasePrices().getEnterprise());

        Map<String, Object> featureUnitPrices = new HashMap<>();
        featureUnitPrices.put("extraPostFee", business.getSubscriptionPricing().getFeatureUnitPrices().getExtraPostFee());
        featureUnitPrices.put("aiChatbotMonthlyFee", business.getSubscriptionPricing().getFeatureUnitPrices().getAiChatbotMonthlyFee());
        featureUnitPrices.put("premiumSupportFee", business.getSubscriptionPricing().getFeatureUnitPrices().getPremiumSupportFee());
        featureUnitPrices.put("topRankingFee", business.getSubscriptionPricing().getFeatureUnitPrices().getTopRankingFee());

        Map<String, Object> packageQuotas = new HashMap<>();
        packageQuotas.put("durationDays", business.getSubscriptionPricing().getPackageQuotas().getDurationDays());
        packageQuotas.put("trialCounsellor", business.getSubscriptionPricing().getPackageQuotas().getTrialCounsellor());
        packageQuotas.put("standardCounsellor", business.getSubscriptionPricing().getPackageQuotas().getStandardCounsellor());
        packageQuotas.put("enterpriseCounsellor", business.getSubscriptionPricing().getPackageQuotas().getEnterpriseCounsellor());
        packageQuotas.put("trialPostLimit", business.getSubscriptionPricing().getPackageQuotas().getTrialPostLimit());
        packageQuotas.put("standardPostLimit", business.getSubscriptionPricing().getPackageQuotas().getStandardPostLimit());
        packageQuotas.put("enterprisePostLimit", business.getSubscriptionPricing().getPackageQuotas().getEnterprisePostLimit());

        Map<String, Object> subscriptionPricing = new HashMap<>();
        subscriptionPricing.put("basePrices", basePrices);
        subscriptionPricing.put("featureUnitPrices", featureUnitPrices);
        subscriptionPricing.put("packageQuotas", packageQuotas);

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
                .collect(Collectors.toList());

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

        List<Map<String, Object>> methodDocumentRequirementsJson = new ArrayList<>();
        if (admissionSettingsData.getMethodDocumentRequirements() != null) {
            for (var methodRequirement : admissionSettingsData.getMethodDocumentRequirements()) {
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
                            return docData;
                        })
                        .collect(Collectors.toList()));
                methodDocumentRequirementsJson.add(requirementMap);
            }
        }

        Map<String, Object> admissionJson = new HashMap<>();
        admissionJson.put("allowedMethods", allowedMethodsJson);
        admissionJson.put("admissionProcesses", admissionProcessesJson);
        // Alias để đồng bộ với cấu trúc school config hiện tại.
        admissionJson.put("byMethod", methodDocumentRequirementsJson);
        admissionJson.put("methodDocumentRequirements", methodDocumentRequirementsJson);

        Map<String, Object> documentRequirementsTemplateJson = new HashMap<>();
        documentRequirementsTemplateJson.put("mandatoryAll", Collections.emptyList());
        documentRequirementsTemplateJson.put("byMethod", methodDocumentRequirementsJson);
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

    private Map<String, Object> buildAdmissionTemplateFromJson(JsonNode root) {
        JsonNode admissionNode = root.has("admissionSettingsData") ? root.get("admissionSettingsData") : root;
        if (admissionNode == null || !admissionNode.isObject()) {
            throw new IllegalArgumentException("Thiếu object admissionSettingsData trong file import");
        }

        List<Map<String, Object>> allowedMethods = parseAllowedMethods(admissionNode.get("allowedMethods"));
        Set<String> validMethodCodes = allowedMethods.stream()
                .map(m -> String.valueOf(m.get("code")).trim())
                .collect(Collectors.toCollection(HashSet::new));

        List<Map<String, Object>> admissionProcesses = parseAdmissionProcesses(
                admissionNode.get("admissionProcesses"),
                validMethodCodes
        );

        JsonNode documentRequirementsNode = admissionNode.get("documentRequirementsData");
        List<Map<String, Object>> mandatoryAll = parseDocuments(
                documentRequirementsNode != null ? documentRequirementsNode.get("mandatoryAll") : null,
                "documentRequirementsData.mandatoryAll"
        );
        List<Map<String, Object>> byMethod = parseByMethodDocuments(
                documentRequirementsNode != null ? documentRequirementsNode.get("byMethod") : null,
                validMethodCodes
        );

        Map<String, Object> documentRequirementsData = new HashMap<>();
        documentRequirementsData.put("mandatoryAll", mandatoryAll);
        documentRequirementsData.put("byMethod", byMethod);

        Map<String, Object> admissionTemplate = new HashMap<>();
        admissionTemplate.put("allowedMethods", allowedMethods);
        admissionTemplate.put("admissionProcesses", admissionProcesses);
        admissionTemplate.put("documentRequirementsData", documentRequirementsData);
        admissionTemplate.put("byMethod", byMethod);
        admissionTemplate.put("methodDocumentRequirements", byMethod);
        return admissionTemplate;
    }

    private List<Map<String, Object>> parseAllowedMethods(JsonNode node) {
        if (node == null || !node.isArray()) {
            throw new IllegalArgumentException("allowedMethods phải là mảng");
        }

        List<Map<String, Object>> methods = new ArrayList<>();
        Set<String> seenCodes = new HashSet<>();
        for (JsonNode method : node) {
            String code = text(method, "code", "allowedMethods.code");
            if (!seenCodes.add(code.toLowerCase())) {
                throw new IllegalArgumentException("Phương thức tuyển sinh bị trùng: " + code);
            }

            Map<String, Object> item = new HashMap<>();
            item.put("code", code);
            item.put("displayName", text(method, "displayName", "allowedMethods.displayName"));
            item.put("description", method.has("description") ? method.get("description").asText("") : "");
            methods.add(item);
        }

        return methods;
    }

    private List<Map<String, Object>> parseAdmissionProcesses(JsonNode node, Set<String> validMethodCodes) {
        if (node == null) {
            return Collections.emptyList();
        }
        if (!node.isArray()) {
            throw new IllegalArgumentException("admissionProcesses phải là mảng");
        }

        List<Map<String, Object>> processes = new ArrayList<>();
        Set<String> seenMethodCodes = new HashSet<>();
        for (JsonNode process : node) {
            String methodCode = text(process, "methodCode", "admissionProcesses.methodCode");
            if (!validMethodCodes.contains(methodCode)) {
                throw new IllegalArgumentException("methodCode không tồn tại trong allowedMethods: " + methodCode);
            }
            if (!seenMethodCodes.add(methodCode.toLowerCase())) {
                throw new IllegalArgumentException("admissionProcesses bị trùng methodCode: " + methodCode);
            }

            JsonNode stepsNode = process.get("steps");
            if (stepsNode == null || !stepsNode.isArray()) {
                throw new IllegalArgumentException("admissionProcesses.steps phải là mảng cho methodCode: " + methodCode);
            }

            List<Map<String, Object>> steps = new ArrayList<>();
            for (JsonNode step : stepsNode) {
                if (!step.has("stepOrder") || !step.get("stepOrder").canConvertToInt()) {
                    throw new IllegalArgumentException("stepOrder phải là số nguyên cho methodCode: " + methodCode);
                }

                Map<String, Object> stepItem = new HashMap<>();
                stepItem.put("stepOrder", step.get("stepOrder").asInt());
                stepItem.put("stepName", text(step, "stepName", "admissionProcesses.steps.stepName"));
                stepItem.put("description", step.has("description") ? step.get("description").asText("") : "");
                steps.add(stepItem);
            }

            Map<String, Object> processItem = new HashMap<>();
            processItem.put("methodCode", methodCode);
            processItem.put("steps", steps);
            processes.add(processItem);
        }

        return processes;
    }

    private List<Map<String, Object>> parseByMethodDocuments(JsonNode node, Set<String> validMethodCodes) {
        if (node == null) {
            return Collections.emptyList();
        }
        if (!node.isArray()) {
            throw new IllegalArgumentException("documentRequirementsData.byMethod phải là mảng");
        }

        List<Map<String, Object>> byMethod = new ArrayList<>();
        Set<String> seenMethodCodes = new HashSet<>();
        for (JsonNode methodNode : node) {
            String methodCode = text(methodNode, "methodCode", "documentRequirementsData.byMethod.methodCode");
            if (!validMethodCodes.contains(methodCode)) {
                throw new IllegalArgumentException("methodCode không tồn tại trong allowedMethods: " + methodCode);
            }
            if (!seenMethodCodes.add(methodCode.toLowerCase())) {
                throw new IllegalArgumentException("documentRequirementsData.byMethod bị trùng methodCode: " + methodCode);
            }

            List<Map<String, Object>> documents = parseDocuments(
                    methodNode.get("documents"),
                    "documentRequirementsData.byMethod.documents"
            );

            Map<String, Object> item = new HashMap<>();
            item.put("methodCode", methodCode);
            item.put("documents", documents);
            byMethod.add(item);
        }

        return byMethod;
    }

    private List<Map<String, Object>> parseDocuments(JsonNode node, String pathLabel) {
        if (node == null) {
            return Collections.emptyList();
        }
        if (!node.isArray()) {
            throw new IllegalArgumentException(pathLabel + " phải là mảng");
        }

        List<Map<String, Object>> docs = new ArrayList<>();
        for (JsonNode doc : node) {
            Map<String, Object> item = new HashMap<>();
            item.put("code", text(doc, "code", pathLabel + ".code"));
            item.put("name", text(doc, "name", pathLabel + ".name"));
            item.put("required", doc.has("required") && doc.get("required").asBoolean());
            docs.add(item);
        }
        return docs;
    }

    private String text(JsonNode node, String fieldName, String pathLabel) {
        JsonNode value = node.get(fieldName);
        if (value == null || value.asText("").trim().isEmpty()) {
            throw new IllegalArgumentException(pathLabel + " không được để trống");
        }
        return value.asText().trim();
    }
}
