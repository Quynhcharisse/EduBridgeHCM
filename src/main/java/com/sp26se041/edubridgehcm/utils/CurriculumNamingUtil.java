package com.sp26se041.edubridgehcm.utils;

import com.sp26se041.edubridgehcm.requests.CreateCurriculumRequest;

import java.text.Normalizer;
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

    public static String generateName(CreateCurriculumRequest request) {
        return String.format("Hệ %s - Khối %d (%d)",
                request.getSubTypeName(), // Tên phụ (VD: Song Ngữ)
                DEFAULT_GRADE,            // Mặc định là 10
                request.getEnrollmentYear());
    }

    public static String generateGroupCode(CreateCurriculumRequest request) {
        String typePrefix = request.getCurriculumType().toUpperCase();
        String abbr = getAbbreviation(request.getSubTypeName());

        // Hardcode số 10 vào format vì nhóm chỉ làm khối 10
        return String.format("%s_%d_%s", typePrefix, DEFAULT_GRADE, abbr);
    }
}
