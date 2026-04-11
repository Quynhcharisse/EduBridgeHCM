package com.sp26se041.edubridgehcm.validations.admin;

import com.sp26se041.edubridgehcm.enums.ParentPostPermission;
import com.sp26se041.edubridgehcm.enums.SupportLevel;
import com.sp26se041.edubridgehcm.requests.UpsertServicePackageFeeRequest;
import org.springframework.util.StringUtils;

import java.util.Arrays;

public class SubscriptionValidation {

    public static String upsertSubscriptionValidation(UpsertServicePackageFeeRequest request) {

        if (!StringUtils.hasText(request.getName())) {
            return "Package name is required";
        }

        if (request.getName().length() > 100) {
            return "Package name is too long (max 100 characters).";
        }

        if (request.getPrice() == null || request.getPrice() < 0) {
            return "Price is required and cannot be negative.";
        }

        // Validate Duration
        if (request.getDurationDays() == null || request.getDurationDays() <= 0) {
            return "Duration days is required and must be greater than 0.";
        }

        // 2. Validate Features (Cực kỳ quan trọng vì đây là cục JSON)
        if (request.getFeatureData() == null) {
            return "Feature data is required.";
        }

        UpsertServicePackageFeeRequest.FeatureData features = request.getFeatureData();

        if (features.getMaxCounsellors() != null && features.getMaxCounsellors() < -1) {
            return "Max counsellors cannot be less than -1.";
        }

        if (features.getMaxAdmissions() != null && features.getMaxAdmissions() < -1) {
            return "Max admissions cannot be less than -1.";
        }

        if (features.getAllowChat() == null) return "Allow chat setting is required.";

        if (features.getIsFeatured() == null) return "Is featured setting is required.";

        if (parseParentPostPermission(features.getParentPostPermission()) == null) {
            return "Parent post permission is required. Must be one of: " + Arrays.toString(ParentPostPermission.values());
        }

        if (features.getTopRanking() == null || features.getTopRanking() < 0) {
            return "Top ranking is required and must be a non-negative integer.";
        }

        // 2. Validate supportLevel (Ví dụ: "24/7", "Email only", "Priority Support")
        if (parseSupportLevel(features.getSupportLevel()) == null) {
            return "Support level description is required.. Must be one of: " + Arrays.toString(SupportLevel.values());
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
