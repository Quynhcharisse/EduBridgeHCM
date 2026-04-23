package com.sp26se041.edubridgehcm.utils;

import com.sp26se041.edubridgehcm.requests.CurriculumRequest;

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

    public static String generateDraftName(CurriculumRequest request) {
        return String.format("Hệ %s - Khối %d (Bản nháp)",
                request.getSubTypeName(),
                DEFAULT_GRADE);
    }

    public static String generatePublishedName(String subTypeName, Integer year) {
        return String.format("Hệ %s - Khối %d (%d)",
                subTypeName,
                DEFAULT_GRADE,
                year);
    }

    public static String generateGroupCode(CurriculumRequest request) {
        String typePrefix = request.getCurriculumType().toUpperCase();
        String abbr = getAbbreviation(request.getSubTypeName());
        return String.format("%s_%d_%s", typePrefix, DEFAULT_GRADE, abbr);
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
