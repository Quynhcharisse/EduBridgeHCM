package com.sp26se041.edubridgehcm.validations.school;

import com.sp26se041.edubridgehcm.models.PlatformConfig;
import com.sp26se041.edubridgehcm.repositories.PlatformConfigRepo;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

public class PreviewSubscriptionValidation {

    @SuppressWarnings("unchecked")
    public static Map<String, Object> asMap(Object value) {
        if (value instanceof Map<?, ?> raw) {
            return (Map<String, Object>) raw;
        }
        return Collections.emptyMap();
    }

    public static Map<String, Object> getBusinessMap(PlatformConfigRepo platformConfigRepo) {
        PlatformConfig config = platformConfigRepo.findByKey("business").orElse(null);
        if (config == null || config.getValue() == null) {
            throw new RuntimeException("Chưa cấu hình business.");
        }
        return asMap(config.getValue());
    }

    public static BigDecimal resolveBusinessRate(String key, PlatformConfigRepo platformConfigRepo) {
        Map<String, Object> business = getBusinessMap(platformConfigRepo);
        Object rawValue = business.get(key);
        if (!(rawValue instanceof Number number)) {
            throw new RuntimeException("Thiếu cấu hình " + key + ".");
        }
        return BigDecimal.valueOf(number.doubleValue());
    }
}
