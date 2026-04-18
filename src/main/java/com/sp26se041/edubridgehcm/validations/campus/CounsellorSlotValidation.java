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

        if (request.getCounsellorIds() == null || request.getCounsellorIds().isEmpty()) {
            return "Danh sách chuyên viên tư vấn (counsellorIds) không được để trống.";
        }

        if (request.getStartDate() == null || request.getEndDate() == null) {
            return "Ngày bắt đầu và ngày kết thúc không được để trống.";
        }
        if (request.getStartDate().isAfter(request.getEndDate())) {
            return "Ngày bắt đầu không được sau ngày kết thúc.";
        }
        if (counsellors == null || counsellors.size() != request.getCounsellorIds().size()) {
            return "Một hoặc nhiều chuyên viên tư vấn không tồn tại trong hệ thống.";
        }

        String normalizedAction = normalizeCounsellorSlotSyncAction(request.getAction());
        if (normalizedAction == null) {
            return "Tham số action phải là GÁN (ASSIGN) hoặc HỦY GÁN (UNASSIGN).";
        }
        boolean isAssign = "ASSIGN".equals(normalizedAction);

        if (isAssign) {
            if (!SchoolConfigUtil.isWithinAcademicTerms(request.getStartDate(), operatingSettings)) {
                return "Ngày bắt đầu gán lịch (" + request.getStartDate() + ") nằm ngoài phạm vi học kỳ.";
            }

            if (!SchoolConfigUtil.isWithinAcademicTerms(request.getEndDate(), operatingSettings)) {
                return "Ngày kết thúc gán lịch (" + request.getEndDate() + ") nằm ngoài phạm vi học kỳ.";
            }
        }

        Map<String, Integer> policy = SchoolConfigUtil.getNumericPolicyFromOperationMap(operatingSettings);

        if (isAssign) {

            // Check số lượng tối thiểu/tối đa (ưu tiên số trong cấu hình hiệu lực HQ + campus)
            int minRequired = policy.getOrDefault("minCounsellorPerSlot", 1);
            Integer maxCap = SchoolConfigUtil.resolveMaxCounsellorsPerSlot(policy);
            if (maxCap != null && maxCap < minRequired) {
                return String.format(
                        "Cấu hình vận hành không hợp lệ: tối đa tư vấn viên mỗi khung (%d) không được nhỏ hơn tối thiểu (%d). Hãy chỉnh maxCounsellorsPerSlot / minCounsellorPerSlot.",
                        maxCap, minRequired);
            }

            // Lấy danh sách chuyên viên ĐÃ CÓ trong ca này (cùng mẫu lịch, cùng ngày); bỏ qua SLOT_UNASSIGNED (đã gỡ khỏi lịch)
            List<Integer> existingCounsellorIds = allCurrentSlots.stream()
                    .filter(s -> s.getStatus() != Status.SLOT_UNASSIGNED
                            && s.getCampusScheduleTemplate().getId().equals(template.getId())
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
            if (maxCap != null && totalAfterAssign > maxCap) {
                return String.format(
                        "Ca này đã đầy. Theo cấu hình (maxCounsellorsPerSlot) tối đa %d người cùng khung (Hiện có: %d, đang thêm: %d).",
                        maxCap, existingCounsellorIds.size(), newUniqueAssignees);
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

    public static String normalizeCounsellorSlotSyncAction(String action) {
        if (action == null || action.isBlank()) {
            return null;
        }
        String u = action.trim().toUpperCase();
        if ("ASSIGN".equals(u) || "UNASSIGN".equals(u)) {
            return u;
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
