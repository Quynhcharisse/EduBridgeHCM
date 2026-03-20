package com.sp26se041.edubridgehcm.utils;

import com.sp26se041.edubridgehcm.requests.CurriculumRequest;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

public class CurriculumNamingUtil {

    private static final int DEFAULT_GRADE = 10;

    public static String getAbbreviation(String input) {
        if (input == null || input.isBlank()) return "NA";

        String temp = Normalizer.normalize(input, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String normalized = pattern.matcher(temp).replaceAll("")
                .replace("Đ", "D")
                .replace("đ", "d");

        StringBuilder abbr = new StringBuilder();
        for (String word : normalized.split("\\s+")) {
            if (!word.isEmpty()) {
                abbr.append(word.charAt(0));
            }
        }
        return abbr.toString().toUpperCase();
    }

    public static String generateName(CurriculumRequest request) {
        return String.format("Hệ %s - Khối %d (%d)",
                request.getSubTypeName(), // Tên phụ (VD: Song Ngữ)
                DEFAULT_GRADE,            // Mặc định là 10
                request.getEnrollmentYear());
    }

    public static String generateGroupCode(CurriculumRequest request) {
        String typePrefix = request.getCurriculumType().toUpperCase();
        String abbr = getAbbreviation(request.getSubTypeName());

        // Hardcode số 10 vào format vì nhóm chỉ làm khối 10
        return String.format("%s_%d_%s", typePrefix, DEFAULT_GRADE, abbr);
    }

    public static String formatLongVersion(Long version) {
        if (version == null) return "N/A";

        // 1. Chuyển Long thành String
        String versionStr = String.valueOf(version);

        // 2. Định nghĩa Format đầu vào (phải khớp với yyyyMMddHHmmss)
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

        // 3. Parse ngược lại thành LocalDateTime
        LocalDateTime dateTime = LocalDateTime.parse(versionStr, inputFormatter);

        // 4. Trả về định dạng dễ đọc (Ví dụ: 18/03/2026 22:45)
        return dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public static String extractSubTypeNameFromName(String name) {
        if (name == null || !name.startsWith("Hệ ")) return "";
        int start = "Hệ ".length();
        int end = name.indexOf(" - Khối");
        if (end > start) {
            return name.substring(start, end).trim();
        }
        return "";
    }
}
