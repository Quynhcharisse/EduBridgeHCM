package com.sp26se041.edubridgehcm.validations.admin;

import com.sp26se041.edubridgehcm.enums.ParentPostPermission;
import com.sp26se041.edubridgehcm.enums.SupportLevel;
import com.sp26se041.edubridgehcm.requests.UpsertServicePackageFeeRequest;
import org.springframework.util.StringUtils;

import java.util.Arrays;

public class SubscriptionValidation {

    public static String upsertSubscriptionValidation(UpsertServicePackageFeeRequest request) {

        if (!StringUtils.hasText(request.getName())) {
            return "Tên gói dịch vụ không được để trống.";
        }

        if (request.getName().length() > 100) {
            return "Tên gói dịch vụ quá dài (tối đa 100 ký tự).";
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

        if (features.getIsFeatured() == null) return "Vui lòng thiết lập trạng thái gói nổi bật (Featured).";

        if (parseParentPostPermission(features.getParentPostPermission()) == null) {
            return "Quyền đăng bài không hợp lệ. Phải thuộc một trong các giá trị: " + Arrays.toString(ParentPostPermission.values());
        }

        if (features.getTopRanking() == null || features.getTopRanking() < 0) {
            return "Thứ tự ưu tiên (Top ranking) là bắt buộc và phải là số nguyên không âm.";
        }

        // 2. Validate supportLevel (Ví dụ: "24/7", "Email only", "Priority Support")
        if (parseSupportLevel(features.getSupportLevel()) == null) {
            return "Mức độ hỗ trợ không hợp lệ. Phải thuộc một trong các giá trị: " + Arrays.toString(SupportLevel.values());
        }

        return null;
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

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
