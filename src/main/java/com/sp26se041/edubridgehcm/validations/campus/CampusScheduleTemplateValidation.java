package com.sp26se041.edubridgehcm.validations.campus;

import com.sp26se041.edubridgehcm.repositories.CampusScheduleTemplateRepo;
import com.sp26se041.edubridgehcm.requests.CampusScheduleTemplateRequest;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public class CampusScheduleTemplateValidation {

    // regex to validate HH:mm format (00:00 to 23:59)
    private static final String TIME_REGEX = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$";

    public static String validateCampusScheduleTemplate(int templateId, CampusScheduleTemplateRequest request, Map<String, Object> workingConfig, CampusScheduleTemplateRepo campusScheduleTemplateRepo) {

        if (request.getStartTime() == null || !request.getStartTime().matches(TIME_REGEX) ||
                request.getEndTime() == null || !request.getEndTime().matches(TIME_REGEX)) {
            return "Invalid time format (Expected HH:mm).";
        }

        LocalTime start = LocalTime.parse(request.getStartTime());
        LocalTime end = LocalTime.parse(request.getEndTime());

        if (!start.isBefore(end)) {
            return "Start time must be earlier than end time.";
        }

        // 2. Operational Config Check
        String dayReq = request.getDayOfWeek() != null ? request.getDayOfWeek().toUpperCase() : "";

        if (workingConfig != null) {
            List<String> regularDays = (List<String>) workingConfig.get("regularDays");
            List<String> weekendDays = (List<String>) workingConfig.get("weekendDays");
            boolean isOpenSunday = Boolean.TRUE.equals(workingConfig.get("isOpenSunday"));

            // Check validity without throwing Enum Exception
            boolean isRegular = regularDays != null && regularDays.contains(dayReq);
            boolean isWeekend = weekendDays != null && weekendDays.contains(dayReq);
            boolean isSunOpen = "SUN".equals(dayReq) && isOpenSunday;

            if (!isRegular && !isWeekend && !isSunOpen) {
                return "The campus is not operational on " + dayReq + ".";
            }

            // 3. Work Shift Check
            List<Map<String, Object>> workShifts = (List<Map<String, Object>>) workingConfig.get("workShifts");
            if (workShifts != null && !workShifts.isEmpty()) {
                boolean isInShift = workShifts.stream().anyMatch(shift -> {
                    LocalTime sStart = LocalTime.parse(String.valueOf(shift.get("startTime")));
                    LocalTime sEnd = LocalTime.parse(String.valueOf(shift.get("endTime")));
                    return (start.equals(sStart) || start.isAfter(sStart)) &&
                            (end.equals(sEnd) || end.isBefore(sEnd));
                });

                if (!isInShift) {
                    return "The requested time slot falls outside of operational shifts.";
                }
            }
        }

        // 4. Duplicate Check (Using ID Not to exclude self during update)
        if (campusScheduleTemplateRepo.existsByCampusIdAndDayOfWeekAndStartTimeAndEndTimeAndIdNot(
                request.getCampusId(), dayReq, start, end, templateId)) {
            return "This time slot conflicts with an existing template.";
        }

        return null;
    }
}
