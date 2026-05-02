package com.sp26se041.edubridgehcm.validations.admin;

import com.sp26se041.edubridgehcm.enums.PackageType;
import com.sp26se041.edubridgehcm.enums.ParentPostPermission;
import com.sp26se041.edubridgehcm.enums.SupportLevel;
import com.sp26se041.edubridgehcm.requests.UpsertServicePackageFeeRequest;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Map;

public class SubscriptionValidation {

    public static String upsertSubscriptionValidation(UpsertServicePackageFeeRequest request) {
        if (request == null) {
            return "Thiếu dữ liệu yêu cầu.";
        }

        if (!StringUtils.hasText(request.getName())) {
            return "Tên gói dịch vụ không được để trống.";
        }

        if (request.getName().length() > 100) {
            return "Tên gói dịch vụ quá dài (tối đa 100 ký tự).";
        }

        if (parsePackageType(request.getPackageType()) == null) {
            return "Loại gói không hợp lệ. Phải thuộc một trong các giá trị: " + Arrays.toString(PackageType.values());
        }

        // Validate Duration
        if (request.getDurationDays() == null || request.getDurationDays() <= 0) {
            return "Thời hạn (số ngày) không được để trống và phải lớn hơn 0.";
        }

        if (request.getFeatureData() == null) {
            return "Dữ liệu tính năng của gói không được để trống.";
        }

        UpsertServicePackageFeeRequest.FeatureData features = request.getFeatureData();

        if (features.getMaxCounsellors() != null && features.getMaxCounsellors() < -1) {
            return "Số lượng tư vấn viên tối đa không được nhỏ hơn -1.";
        }

        if (features.getHasAiAssistant() == null) return "Vui lòng thiết lập quyền cho phép chat.";

        if (parseParentPostPermission(features.getParentPostPermission()) == null) {
            return "Quyền đăng bài không hợp lệ. Phải thuộc một trong các giá trị: " + Arrays.toString(ParentPostPermission.values());
        }

        // 2. Validate supportLevel (Ví dụ: "24/7", "Email only", "Priority Support")
        if (parseSupportLevel(features.getSupportLevel()) == null) {
            return "Mức độ hỗ trợ không hợp lệ. Phải thuộc một trong các giá trị: " + Arrays.toString(SupportLevel.values());
        }

        return null;
    }

    //validate policy của gói dùng thử ==> ko hỗ trợ add-on trả phí các tính năng trả tiền
    public static String validateTrialPackage(UpsertServicePackageFeeRequest request, Map<String, Object> businessMap) {
        if (request.getFeatureData() == null) {
            return "Gói dùng thử phải có dữ liệu tính năng.";
        }

        UpsertServicePackageFeeRequest.FeatureData features = request.getFeatureData();
        Integer maxCounsellors = features.getMaxCounsellors();
        Integer postLimit = features.getPostLimit();

        // Null checks
        if (maxCounsellors == null) {
            return "Gói dùng thử phải xác định số lượng tư vấn viên.";
        }

        if (postLimit == null) {
            return "Gói dùng thử phải xác định giới hạn bài đăng.";
        }

        if (maxCounsellors < 0 || postLimit < 0) {
            return "Gói dùng thử: Số lượng tư vấn viên và giới hạn bài đăng phải lớn hơn hoặc bằng 0.";
        }

        // Lấy giá trị tiêu chuẩn và tỉ lệ từ businessMap
        Map<String, Object> subscriptionPricing = (Map<String, Object>) businessMap.get("subscriptionPricing");
        if (subscriptionPricing == null) {
            return "Không tìm thấy cấu hình định giá.";
        }

        double trialRatioCap = (Double) subscriptionPricing.getOrDefault("trialRatioCap", 0.3);
        Map<String, Object> packageQuotas = (Map<String, Object>) subscriptionPricing.get("packageQuotas");

        if (packageQuotas == null) {
            return "Không tìm thấy cấu hình định mức gói.";
        }

        Integer standardCounsellor = getIntFromMap(packageQuotas, "standardCounsellor");
        Integer standardPostLimit = getIntFromMap(packageQuotas, "standardPostLimit");

        if (standardCounsellor == null || standardPostLimit == null) {
            return "Thiếu cấu hình định mức gói Tiêu chuẩn trong hệ thống.";
        }

        // Ràng buộc tỉ lệ với standard tier
        int maxTrialCounsellorByRatio = (int) Math.floor(standardCounsellor * trialRatioCap);
        if (maxCounsellors > maxTrialCounsellorByRatio) {
            return String.format(
                    "Với tỉ lệ cấu hình là %d%%, số lượng tư vấn viên dùng thử không được vượt quá %d.",
                    (int) (trialRatioCap * 100), maxTrialCounsellorByRatio
            );
        }

        int maxTrialPostLimitByRatio = (int) Math.floor(standardPostLimit * trialRatioCap);

        if (postLimit > maxTrialPostLimitByRatio) {
            return String.format(
                    "Với tỉ lệ cấu hình là %d%%, giới hạn bài đăng dùng thử không được vượt quá %d.",
                    (int) (trialRatioCap * 100), maxTrialPostLimitByRatio
            );
        }

        // Không được có tính năng trả phí
        if (Boolean.TRUE.equals(features.getHasAiAssistant())) {
            return "Gói dùng thử không được phép bao gồm trợ lý AI.";
        }

        if (features.getSupportLevel() != null && !SupportLevel.BASIC_SUPPORT.name().equals(features.getSupportLevel())) {
            return "Gói dùng thử chỉ hỗ trợ mức cơ bản.";
        }

        if (features.getParentPostPermission() == null || features.getParentPostPermission().equals(ParentPostPermission.VIEW_ONLY.name())) {
            features.setParentPostPermission(ParentPostPermission.VIEW_ONLY.name());
        } else if (!features.getParentPostPermission().equals(ParentPostPermission.CREATE_POST.name())) {
            return "Quyền hạn bài đăng không hợp lệ cho gói dùng thử.";
        }

        return null;
    }

    public static Integer getIntFromMap(Map<String, Object> map, String key) {
        if (map == null || key == null) return null;
        Object raw = map.get(key);
        if (raw == null) return null;
        if (raw instanceof Number) return ((Number) raw).intValue();
        if (raw instanceof String) {
            try {
                return Integer.parseInt(((String) raw).trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    public static void validatePricingInput(UpsertServicePackageFeeRequest request, Map<String, Object> business) {
        if (request == null) {
            throw new RuntimeException("Thiếu dữ liệu yêu cầu");
        }
        if (business == null) {
            throw new RuntimeException("Thiếu cấu hình doanh nghiệp");
        }
        if (request.getPackageType() == null || request.getPackageType().isBlank()) {
            throw new RuntimeException("Thiếu loại gói");
        }
        if (request.getFeatureData() == null) {
            throw new RuntimeException("Thiếu tính năng");
        }
    }

    public static ParentPostPermission parseParentPostPermission(String value) {
        String normalizedValue = normalize(value);
        if (normalizedValue == null) {
            return null;
        }

        return Arrays.stream(ParentPostPermission.values())
                .filter(r -> r.getValue().equalsIgnoreCase(normalizedValue) || r.name().equalsIgnoreCase(normalizedValue))
                .findFirst()
                .orElse(null);
    }

    public static SupportLevel parseSupportLevel(String value) {
        String normalizedValue = normalize(value);
        if (normalizedValue == null) {
            return null;
        }

        return Arrays.stream(SupportLevel.values())
                .filter(r -> r.getValue().equalsIgnoreCase(normalizedValue)
                        || r.name().equalsIgnoreCase(normalizedValue)
                        || r.getValue().contains(normalizedValue)
                )
                .findFirst()
                .orElse(null);
    }

    public static PackageType parsePackageType(String value) {
        String normalizedValue = normalize(value);
        if (normalizedValue == null) {
            return null;
        }

        return Arrays.stream(PackageType.values())
                .filter(r -> r.name().equalsIgnoreCase(normalizedValue))
                .findFirst()
                .orElse(null);
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
