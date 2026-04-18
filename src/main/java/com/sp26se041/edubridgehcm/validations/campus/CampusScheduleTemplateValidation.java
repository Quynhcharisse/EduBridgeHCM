package com.sp26se041.edubridgehcm.validations.campus;

import com.sp26se041.edubridgehcm.enums.SessionType;
import com.sp26se041.edubridgehcm.models.Campus;
import com.sp26se041.edubridgehcm.models.CampusScheduleTemplate;
import com.sp26se041.edubridgehcm.repositories.CampusScheduleTemplateRepo;
import com.sp26se041.edubridgehcm.utils.SchoolConfigUtil;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public class CampusScheduleTemplateValidation {

    // regex to validate HH:mm format (00:00 to 23:59)
    private static final String TIME_REGEX = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$";

    public static String validateCampusScheduleTemplate(
            Integer templateId,
            String startTime,
            String endTime,
            String sessionType,
            String currentDay,
            Map<String, Object> workingConfig,
            CampusScheduleTemplateRepo campusScheduleTemplateRepo,
            Campus campus,
            Integer slotDurationMinutesFromPolicy,
            Integer bufferBetweenSlotsMinutesFromPolicy) {

        if (startTime == null || !startTime.matches(TIME_REGEX) ||
                endTime == null || !endTime.matches(TIME_REGEX)) {
            return "Định dạng thời gian không hợp lệ (Yêu cầu HH:mm).";
        }

        LocalTime start = LocalTime.parse(startTime);
        LocalTime end = LocalTime.parse(endTime);

        if (!start.isBefore(end)) {
            return "Thời gian bắt đầu phải trước thời gian kết thúc.";
        }

        long durationInMinutes = Duration.between(start, end).toMinutes();
        if (durationInMinutes < 30) {
            return "Một tiết học/phiên làm việc phải kéo dài ít nhất 30 phút.";
        }

        if (slotDurationMinutesFromPolicy != null && slotDurationMinutesFromPolicy > 0) {
            int buffer = bufferBetweenSlotsMinutesFromPolicy != null ? Math.max(0, bufferBetweenSlotsMinutesFromPolicy) : 0;
            if (buffer == 0) {
                if (durationInMinutes % slotDurationMinutesFromPolicy != 0) {
                    return "Độ dài khung giờ (" + durationInMinutes + " phút) phải là bội số của thời lượng mỗi ca tư vấn ("
                            + slotDurationMinutesFromPolicy + " phút) theo cấu hình vận hành.";
                }
            } else {
                int step = slotDurationMinutesFromPolicy + buffer;
                if ((durationInMinutes + buffer) % step != 0) {
                    return "Độ dài khung giờ (" + durationInMinutes + " phút) không khớp mô hình tiết "
                            + slotDurationMinutesFromPolicy + " phút + nghỉ " + buffer + " phút giữa các tiết. "
                            + "(Độ dài ca + " + buffer + ") phải chia hết cho " + step + " phút (bước = tiết + nghỉ).";
                }
            }
        }

        if (!isValidSessionType(sessionType)) {
            return "Loại buổi học không hợp lệ.";
        }

        String configError = SchoolConfigUtil.validateWithWorkingConfig(currentDay, start, end, sessionType, workingConfig);
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
