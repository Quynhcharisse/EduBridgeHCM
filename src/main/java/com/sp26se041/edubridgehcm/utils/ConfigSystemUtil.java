package com.sp26se041.edubridgehcm.utils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ConfigSystemUtil {

    public static List<String> getAllowedFormats(Map<String, Object> mediaConfig, String typeKey) {
        if (mediaConfig == null || !mediaConfig.containsKey(typeKey)) return Collections.emptyList();

        List<Map<String, String>> formats = (List<Map<String, String>>) mediaConfig.get(typeKey);
        return formats.stream()
                .map(f -> f.get("format").toLowerCase())
                .toList();
    }

    // Lấy dung lượng tối đa (đơn vị MB hoặc KB tùy bạn quy định)
    public static Long getMaxSize(Map<String, Object> mediaConfig, String sizeKey) {
        if (mediaConfig == null || !mediaConfig.containsKey(sizeKey)) return Long.MAX_VALUE;
        Object size = mediaConfig.get(sizeKey);
        return size instanceof Number ? ((Number) size).longValue() : Long.MAX_VALUE;
    }
}
