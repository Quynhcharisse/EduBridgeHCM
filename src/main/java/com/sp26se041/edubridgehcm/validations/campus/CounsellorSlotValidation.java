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
            Map<String, Object> operatingSettings,
            AssignCounsellorIntoSlotsRequest request,
            Campus campus,
            CampusScheduleTemplate template,
            List<Counsellor> counsellors,
            List<CounsellorSlot> allCurrentSlots) {

        if (request.getStartDate() == null || request.getEndDate() == null) {
            return "Ngày bắt đầu và ngày kết thúc không được để trống.";
        }
        if (request.getStartDate().isAfter(request.getEndDate())) {
            return "Ngày bắt đầu không được sau ngày kết thúc.";
        }
        if (counsellors == null || counsellors.size() != request.getCounsellorIds().size()) {
            return "Một hoặc nhiều chuyên viên tư vấn không tồn tại trong hệ thống.";
        }

        if (!SchoolConfigUtil.isWithinAcademicTerms(request.getStartDate(), operatingSettings)) {
            return "Ngày bắt đầu gán lịch (" + request.getStartDate() + ") nằm ngoài phạm vi học kỳ.";
        }

        if (!SchoolConfigUtil.isWithinAcademicTerms(request.getEndDate(), operatingSettings)) {
            return "Ngày kết thúc gán lịch (" + request.getEndDate() + ") nằm ngoài phạm vi học kỳ.";
        }

        Map<String, Integer> policy = SchoolConfigUtil.getCampusPolicy(campus.getPolicyDetail());

        if ("ASSIGN".equalsIgnoreCase(request.getAction())) {

            // Check số lượng tối thiểu/tối đa
            int minRequired = policy.getOrDefault("minCounsellorPerSlot", 1);
            int maxAllowed = 5;

            // Lấy danh sách chuyên viên ĐÃ CÓ trong ca này (cùng mẫu lịch, cùng ngày)
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
                return String.format("Ca này đã đầy. Tối đa chỉ cho phép %d người (Hiện tại: %d, Mới thêm: %d).",
                        maxAllowed, existingCounsellorIds.size(), newUniqueAssignees);
            }
        }

        // --- 3. Kiểm tra quyền sở hữu (Ownership) ---
        for (Counsellor c : counsellors) {
            if (!c.getCampus().getId().equals(campus.getId())) {
                return String.format("Chuyên viên %s không thuộc cơ sở này.", c.getName());
            }
        }

        return null;
    }

    public static void validateNoActiveConsultation(CounsellorSlot slot) {
        // Danh sách các trạng thái "đang bận" không được phép xóa/gỡ lịch
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
                    String.format("Không thể gỡ lịch của %s. Hiện vẫn còn các lịch hẹn đang ở trạng thái Chờ, Đã xác nhận hoặc Đang tiến hành.",
                            slot.getCounsellor().getName()));
        }
    }
}
