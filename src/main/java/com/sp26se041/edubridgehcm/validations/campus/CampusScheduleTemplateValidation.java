package com.sp26se041.edubridgehcm.validations.campus;

import com.sp26se041.edubridgehcm.enums.SessionType;
import com.sp26se041.edubridgehcm.models.Campus;
import com.sp26se041.edubridgehcm.models.CampusScheduleTemplate;
import com.sp26se041.edubridgehcm.repositories.CampusScheduleTemplateRepo;
import com.sp26se041.edubridgehcm.requests.CampusScheduleTemplateRequest;
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
            return "Định dạng thời gian không hợp lệ (Yêu cầu HH:mm).";
        }

        LocalTime start = LocalTime.parse(request.getStartTime());
        LocalTime end = LocalTime.parse(request.getEndTime());

        if (!start.isBefore(end)) {
            return "Thời gian bắt đầu phải trước thời gian kết thúc.";
        }

        long durationInMinutes = Duration.between(start, end).toMinutes();
        if (durationInMinutes < 30) {
            return "Một tiết học/phiên làm việc phải kéo dài ít nhất 30 phút.";
        }

        if (!isValidSessionType(request.getSessionType())) {
            return "Loại buổi học không hợp lệ.";
        }

        String configError = SchoolConfigUtil.validateWithWorkingConfig(currentDay, start, end, request.getSessionType(), workingConfig);
        if (configError != null) {
            return configError;
        }

        List<CampusScheduleTemplate> existingTemplates = campusScheduleTemplateRepo.findByCampusIdAndDayOfWeekAndActiveTrue(campus.getId(), currentDay.toUpperCase());

        boolean isOverLap = existingTemplates.stream()
                .filter(t -> !t.getId().equals(templateId))
                .anyMatch(t -> start.isBefore(t.getEndTime()) && end.isAfter(t.getStartTime()));

        if (isOverLap) {
            return "Khung giờ này đã trùng lặp với một lịch trình khác đã tồn tại.";
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
