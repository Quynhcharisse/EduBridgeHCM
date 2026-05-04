package com.sp26se041.edubridgehcm.validations.platform;

import com.sp26se041.edubridgehcm.enums.ImportType;
import com.sp26se041.edubridgehcm.requests.ImportConfirmRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SystemConfigValidation {

    public static List<Map<String, Object>> groupToNestedStructure(List<Map<String, Object>> flatData, ImportType type) {
        if (type == ImportType.ALLOWED_METHODS) {
            return flatData;
        }

        Map<String, Map<String, Object>> groupedMap = new LinkedHashMap<>();
        String childKey = (type == ImportType.ADMISSION_PROCESSES) ? "steps" : "documents";

        for (Map<String, Object> row : flatData) {
            String mCode = String.valueOf(row.get("methodCode"));

            groupedMap.computeIfAbsent(mCode, k -> {
                Map<String, Object> group = new HashMap<>();
                group.put("methodCode", k);
                group.put(childKey, new ArrayList<Map<String, Object>>());
                return group;
            });

            List<Map<String, Object>> children = (List<Map<String, Object>>) groupedMap.get(mCode).get(childKey);
            Map<String, Object> cleanRow = new HashMap<>(row);
            cleanRow.remove("methodCode"); // Remove duplicate at child level
            children.add(cleanRow);
        }

        return new ArrayList<>(groupedMap.values());
    }

    public static String getBusinessKey(Map<String, Object> row, ImportType type) {
        return switch (type) {
            case ALLOWED_METHODS -> normalize(row.get("code"));
            case ADMISSION_PROCESSES -> buildCompositeKey(row.get("methodCode"), row.get("stepOrder"));
            case METHOD_DOCUMENTS -> buildCompositeKey(row.get("methodCode"), row.get("code"));
            case MANDATORY_ALL -> "";
        };
    }

    public static void syncLegacyData(Map<String, Object> configValue, List<Map<String, Object>> data, ImportType type) {
        switch (type) {
            case METHOD_DOCUMENTS -> {
                configValue.put("methodDocumentRequirements", data);
                configValue.put("byMethod", data);
                Map<String, Object> docData = configValue.get("documentRequirementsData") instanceof Map
                        ? new HashMap<>((Map<String, Object>) configValue.get("documentRequirementsData"))
                        : new HashMap<>();
                docData.put("byMethod", data);
                configValue.put("documentRequirementsData", docData);
            }
            case ALLOWED_METHODS -> {
                // Đảm bảo key này luôn đồng bộ với sheet name
                configValue.put("allowedMethods", data);
            }
            case ADMISSION_PROCESSES -> {
                // Đảm bảo key này luôn đồng bộ với sheet name
                configValue.put("admissionProcesses", data);
            }
        }
    }

    public static void cascadeDeleteOnMethodChange(Map<String, Object> configValue, List<Map<String, Object>> allowedMethods) {
        // Extract valid methodCodes from updated allowedMethods
        Set<String> validMethodCodes = new java.util.HashSet<>();
        for (Map<String, Object> method : allowedMethods) {
            String code = normalize(method.get("code"));
            if (!code.isBlank()) {
                validMethodCodes.add(code);
            }
        }

        List<Map<String, Object>> admissionProcesses = (List<Map<String, Object>>) configValue.get("admissionProcesses");
        if (admissionProcesses != null) {
            List<Map<String, Object>> validProcesses = new ArrayList<>();
            for (Map<String, Object> process : admissionProcesses) {
                String methodCode = normalize(process.get("methodCode"));
                if (!methodCode.isBlank() && validMethodCodes.contains(methodCode)) {
                    validProcesses.add(process);
                }
              
            }
            configValue.put("admissionProcesses", validProcesses);
        }

        List<Map<String, Object>> methodDocuments = (List<Map<String, Object>>) configValue.get("methodDocumentRequirements");
        if (methodDocuments != null) {
            List<Map<String, Object>> validDocuments = new ArrayList<>();
            for (Map<String, Object> doc : methodDocuments) {
                String methodCode = normalize(doc.get("methodCode"));
                if (!methodCode.isBlank() && validMethodCodes.contains(methodCode)) {
                    validDocuments.add(doc);
                }
           
            }
            configValue.put("methodDocumentRequirements", validDocuments);

            Map<String, Object> docData = configValue.get("documentRequirementsData") instanceof Map
                    ? new HashMap<>((Map<String, Object>) configValue.get("documentRequirementsData"))
                    : new HashMap<>();
            docData.put("byMethod", validDocuments);
            configValue.put("documentRequirementsData", docData);
            configValue.put("byMethod", validDocuments);
        }
    }

    public static String buildCompositeKey(Object first, Object second) {
        String left = normalize(first);
        String right = normalize(second);
        return (left.isBlank() || right.isBlank()) ? "" : left + "|" + right;
    }

    public static String normalize(Object raw) {
        return raw == null ? "" : String.valueOf(raw).trim().toLowerCase();
    }

    public static ImportConfirmRequest.Error validateRowData(
            Map<String, Object> rowData,
            ImportType type,
            Set<String> fileLevelCodes,
            Set<String> fileLevelAdmissionKeys,
            Set<String> fileLevelMethodDocKeys,
            Set<String> dbCodes
    ) {
        List<ImportConfirmRequest.Fields> fieldErrors = new ArrayList<>();

        // Check empty fields (except index and description)
        rowData.forEach((key, value) -> {
            if (!List.of("description", "index").contains(key) && (value == null || String.valueOf(value).isBlank())) {
                fieldErrors.add(new ImportConfirmRequest.Fields(key, "Trường này không được để trống"));
            }
        });

        String businessKey = getBusinessKey(rowData, type);

        // Business logic validation by type
        switch (type) {
            case ALLOWED_METHODS -> {
                if (!businessKey.isBlank() && !fileLevelCodes.add(businessKey)) {
                    fieldErrors.add(new ImportConfirmRequest.Fields("code", "Mã phương thức đã tồn tại trong file import này"));
                }
            }
            case ADMISSION_PROCESSES -> {
                if (!businessKey.isBlank() && !fileLevelAdmissionKeys.add(businessKey)) {
                    fieldErrors.add(new ImportConfirmRequest.Fields("stepOrder", "Bước xét tuyển đã tồn tại trong file import này"));
                }

                if (!isInteger(rowData.get("stepOrder"))) {
                    fieldErrors.add(new ImportConfirmRequest.Fields("stepOrder", "Thứ tự phải là số"));
                }
            }
            case METHOD_DOCUMENTS -> {
                if (!businessKey.isBlank() && !fileLevelMethodDocKeys.add(businessKey)) {
                    fieldErrors.add(new ImportConfirmRequest.Fields("code", "Mã giấy tờ đã tồn tại trong file import này"));
                }
            }
        }

        String mCode = normalize(rowData.get("methodCode"));
        if (type != ImportType.ALLOWED_METHODS && !mCode.isBlank() && !dbCodes.contains(mCode)) {
            fieldErrors.add(new ImportConfirmRequest.Fields("methodCode", "Mã phương thức không tồn tại"));
        }

        return fieldErrors.isEmpty() ? null : new ImportConfirmRequest.Error(fieldErrors);
    }

    private static boolean isInteger(Object val) {
        try {
            Integer.parseInt(String.valueOf(val));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
