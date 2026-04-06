package com.sp26se041.edubridgehcm.validations.campus;

import com.sp26se041.edubridgehcm.enums.Role;
import com.sp26se041.edubridgehcm.enums.SessionType;
import com.sp26se041.edubridgehcm.models.Account;
import com.sp26se041.edubridgehcm.models.Campus;
import com.sp26se041.edubridgehcm.models.CampusScheduleTemplate;
import com.sp26se041.edubridgehcm.repositories.CampusScheduleTemplateRepo;
import com.sp26se041.edubridgehcm.requests.CampusScheduleTemplateRequest;
import com.sp26se041.edubridgehcm.utils.AuthRequestUtil;
import com.sp26se041.edubridgehcm.utils.SchoolConfigUtil;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public class CampusScheduleTemplateValidation {

    // regex to validate HH:mm format (00:00 to 23:59)
    private static final String TIME_REGEX = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$";

    public static String validateCampusScheduleTemplate(Integer templateId, CampusScheduleTemplateRequest request, String currentDay, Map<String, Object> workingConfig, CampusScheduleTemplateRepo campusScheduleTemplateRepo, Campus campus) {

        if (request.getStartTime() == null || !request.getStartTime().matches(TIME_REGEX) ||
                request.getEndTime() == null || !request.getEndTime().matches(TIME_REGEX)) {
            return "Invalid time format (Expected HH:mm).";
        }

        LocalTime start = LocalTime.parse(request.getStartTime());
        LocalTime end = LocalTime.parse(request.getEndTime());

        if (!start.isBefore(end)) {
            return "Start time must be earlier than end time.";
        }

        long durationInMinutes = Duration.between(start, end).toMinutes();
        if (durationInMinutes < 30) {
            return "A session must last at least 30 minutes.";
        }

        if (!isValidSessionType(request.getSessionType())) {
            return "Invalid session type";
        }

        String configError = SchoolConfigUtil.validateWithWorkingConfig(currentDay, start, end, workingConfig);
        if (configError != null) {
            return configError;
        }

        List<CampusScheduleTemplate> existingTemplates = campusScheduleTemplateRepo.findByCampusIdAndDayOfWeekAndActiveTrue(campus.getId(), currentDay.toUpperCase());

        boolean isOverLap = existingTemplates.stream()
                .filter(t -> !t.getId().equals(templateId))
                .anyMatch(t -> start.isBefore(t.getEndTime()) && end.isAfter(t.getStartTime()));

        if (isOverLap) {
            return "This time slot conflicts with an existing template.";
        }

        return null;
    }

    private static boolean isValidSessionType(String sessionType) {

        if (sessionType == null) return false;

        for (SessionType sesions : SessionType.values()) {
            if (sesions.name().equalsIgnoreCase(sessionType)) {
                return true;
            }
        }

        return false;
    }
}
