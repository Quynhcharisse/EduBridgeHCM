package com.sp26se041.edubridgehcm.enums;

import lombok.Getter;

import java.time.LocalTime;
import java.util.Locale;

@Getter
public enum WorkShiftKind {

    /** Ca sáng — từ sớm đến hết buổi sáng (trước khu vực nghỉ trưa sâu) */
    MORNING("Ca sáng", LocalTime.of(5, 0), LocalTime.of(12, 59)),

    /** Ca trưa — quanh giờ ăn / nghỉ; cận trên 13:30 để khớp trường nghỉ đến 13h–13h30 */
    NOON("Ca trưa", LocalTime.of(10, 0), LocalTime.of(13, 30)),

    /** Ca chiều — từ sau trưa; cận trên 18:30 để khớp làm đến ~18h */
    AFTERNOON("Ca chiều", LocalTime.of(13, 0), LocalTime.of(18, 30)),

    /** Ca tối — sau giờ HC */
    EVENING("Ca tối", LocalTime.of(18, 0), LocalTime.of(23, 59));

    private final String displayNameVi;
    /** Cận dưới (inclusive): giờ bắt đầu ca không được trước mốc này */
    private final LocalTime windowStartInclusive;
    /** Cận trên (inclusive): giờ kết thúc ca không được sau mốc này */
    private final LocalTime windowEndInclusive;

    WorkShiftKind(String displayNameVi, LocalTime windowStartInclusive, LocalTime windowEndInclusive) {
        this.displayNameVi = displayNameVi;
        this.windowStartInclusive = windowStartInclusive;
        this.windowEndInclusive = windowEndInclusive;
    }

    public static WorkShiftKind resolve(String raw) {
        if (raw == null) {
            return null;
        }
        String s = raw.trim();
        if (s.isEmpty()) {
            return null;
        }
        String upper = s.toUpperCase(Locale.ROOT);
        for (WorkShiftKind k : values()) {
            if (k.name().equals(upper)) {
                return k;
            }
        }
        String low = s.toLowerCase(Locale.ROOT);
        if (matches(low, "sáng", "sang", "morning", "ca_sang", "ca sang", "ca-sáng")) {
            return MORNING;
        }
        if (matches(low, "trưa", "trua", "noon", "lunch", "ca_trua", "ca trưa", "ca trua")) {
            return NOON;
        }
        if (matches(low, "chiều", "chieu", "afternoon", "ca_chieu", "ca chiều", "ca chieu")) {
            return AFTERNOON;
        }
        if (matches(low, "tối", "toi", "evening", "night", "ca_toi", "ca tối")) {
            return EVENING;
        }
        return null;
    }

    private static boolean matches(String low, String... tokens) {
        for (String t : tokens) {
            if (low.contains(t)) {
                return true;
            }
        }
        return false;
    }

    public void validateStartEndOrThrow(LocalTime start, LocalTime end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Thời gian bắt đầu và kết thúc ca không được để trống.");
        }
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException(displayNameVi + " (" + name() + "): giờ bắt đầu phải trước giờ kết thúc.");
        }
        if (start.isBefore(windowStartInclusive)) {
            throw new IllegalArgumentException(
                    displayNameVi + " (" + name() + "): giờ bắt đầu không được sớm hơn " + windowStartInclusive + " (khung cho phép "
                            + windowStartInclusive + " – " + windowEndInclusive + ").");
        }
        if (end.isAfter(windowEndInclusive)) {
            throw new IllegalArgumentException(
                    displayNameVi + " (" + name() + "): giờ kết thúc không được muộn hơn " + windowEndInclusive + " (khung cho phép "
                            + windowStartInclusive + " – " + windowEndInclusive + ").");
        }
    }
}
