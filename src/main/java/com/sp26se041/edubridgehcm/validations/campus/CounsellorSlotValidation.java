package com.sp26se041.edubridgehcm.validations.campus;

import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.models.Campus;
import com.sp26se041.edubridgehcm.models.CampusScheduleTemplate;
import com.sp26se041.edubridgehcm.models.Counsellor;
import com.sp26se041.edubridgehcm.models.CounsellorSlot;
import com.sp26se041.edubridgehcm.requests.AssignCounsellorIntoSlotsRequest;
import com.sp26se041.edubridgehcm.utils.SchoolConfigUtil;

import java.util.List;
import java.util.Map;

public class CounsellorSlotValidation {

    public static String validateAssignRequest(
            AssignCounsellorIntoSlotsRequest request,
            Campus campus,
            CampusScheduleTemplate template,
            List<Counsellor> counsellors,
            List<CounsellorSlot> allCurrentSlots) {

        if (request.getStartDate() == null || request.getEndDate() == null) {
            return "Start date and end date cannot be empty.";
        }
        if (request.getStartDate().isAfter(request.getEndDate())) {
            return "Start date cannot be after end date.";
        }
        if (counsellors == null || counsellors.size() != request.getCounsellorIds().size()) {
            return "One or more counsellors do not exist in the system.";
        }

        Map<String, Integer> policy = SchoolConfigUtil.getCampusPolicy(campus.getPolicyDetail());

        if ("ASSIGN".equalsIgnoreCase(request.getAction())) {

            // Check Thời lượng ca (Duration)
            Integer requiredDuration = policy.get("slotDurationInMinutes");
            if (requiredDuration != null) {
                long actualDuration = java.time.Duration.between(template.getStartTime(), template.getEndTime()).toMinutes();
                if (actualDuration != requiredDuration) {
                    return String.format("Template duration (%d minutes) does not match campus policy (%d minutes).",
                            actualDuration, requiredDuration);
                }
            }

            // Check số lượng Min/Max
            int minRequired = policy.getOrDefault("minCounsellorPerSlot", 1);
            int maxAllowed = 5; // Có thể lấy từ policy.getOrDefault("maxCounsellorPerSlot", 5)

            // Lấy danh sách chuyên viên ĐÃ CÓ trong ca này (cùng template, cùng ngày)
            List<Integer> existingCounsellorIds = allCurrentSlots.stream()
                    .filter(s -> s.getCampusScheduleTemplate().getId().equals(template.getId())
                            && s.getStartDate().equals(request.getStartDate())
                            && s.getEndDate().equals(request.getEndDate()))
                    .map(s -> s.getCounsellor().getId())
                    .toList();

            // Tính số lượng thực tế sau khi gán (tránh đếm trùng ID đã tồn tại)
            long newUniqueAssignees = request.getCounsellorIds().stream()
                    .filter(id -> !existingCounsellorIds.contains(id))
                    .count();

            int totalAfterAssign = existingCounsellorIds.size() + (int) newUniqueAssignees;

            if (totalAfterAssign < minRequired) {
                return String.format("Số lượng chuyên viên chưa đủ. Ca này yêu cầu tối thiểu %d người.", minRequired);
            }
            if (totalAfterAssign > maxAllowed) {
                return String.format("Ca này đã đầy. Tối đa chỉ cho phép %d người (Hiện tại: %d, Mới: %d).",
                        maxAllowed, existingCounsellorIds.size(), newUniqueAssignees);
            }
        }

        // --- 3. Kiểm tra quyền sở hữu (Ownership) ---
        for (Counsellor c : counsellors) {
            if (!c.getCampus().getId().equals(campus.getId())) {
                return String.format("Counsellor %s does not belong to this campus.", c.getName());
            }
        }

        return null;
    }

    public static void validateNoActiveConsultation(CounsellorSlot slot) {
        // Danh sách các trạng thái "đang bận" không được phép xóa/unassign
        List<Status> activeStatuses = List.of(
                Status.CONSULTATION_PENDING,
                Status.CONSULTATION_CONFIRMED,
                Status.CONSULTATION_IN_PROGRESS
        );

        // Kiểm tra trong danh sách đăng ký của Slot đó
        boolean hasActiveRequests = slot.getConsultationOfflineRequests().stream()
                .anyMatch(req -> activeStatuses.contains(req.getStatus()));

        if (hasActiveRequests) {
            throw new IllegalArgumentException(
                    String.format("Cannot unassign %s. There are still Pending, Confirmed, or In-Progress appointments.",
                            slot.getCounsellor().getName()));
        }
    }

}
