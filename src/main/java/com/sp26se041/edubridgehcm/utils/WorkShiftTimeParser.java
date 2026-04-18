package com.sp26se041.edubridgehcm.utils;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class WorkShiftTimeParser {

    private WorkShiftTimeParser() {
    }

    public static LocalTime parseFlexible(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Giờ không được để trống.");
        }
        String s = raw.trim();
        try {
            return LocalTime.parse(s);
        } catch (DateTimeParseException ignored) {
            // continue
        }
        try {
            return LocalTime.parse(s, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (DateTimeParseException ignored) {
            // continue
        }
        try {
            return LocalTime.parse(s, DateTimeFormatter.ofPattern("H:mm"));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Không đọc được giờ: \"" + raw + "\" (dùng dạng ví dụ 07:30 hoặc 7:30).");
        }
    }
}
