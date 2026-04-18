package com.sp26se041.edubridgehcm.utils;

import com.sp26se041.edubridgehcm.enums.WorkShiftKind;
import com.sp26se041.edubridgehcm.requests.SchoolConfigRequest;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public final class WorkShiftConfigValidator {

    private WorkShiftConfigValidator() {
    }

    public static Map<String, Object> toPersistedShiftMap(SchoolConfigRequest.WorkShift shift) {
        WorkShiftKind kind = WorkShiftKind.resolve(shift.getName());
        if (kind == null) {
            throw new IllegalArgumentException(
                    "Loại ca không hợp lệ: \"" + shift.getName()
                            + "\". Chọn một trong: ca sáng, ca trưa, ca chiều, ca tối.");
        }
        LocalTime start = WorkShiftTimeParser.parseFlexible(shift.getStartTime());
        LocalTime end = WorkShiftTimeParser.parseFlexible(shift.getEndTime());
        kind.validateStartEndOrThrow(start, end);

        Map<String, Object> s = new HashMap<>();
        s.put("name", kind.name());
        s.put("startTime", formatHm(start));
        s.put("endTime", formatHm(end));
        return s;
    }

    private static String formatHm(LocalTime t) {
        return String.format("%02d:%02d", t.getHour(), t.getMinute());
    }
}
