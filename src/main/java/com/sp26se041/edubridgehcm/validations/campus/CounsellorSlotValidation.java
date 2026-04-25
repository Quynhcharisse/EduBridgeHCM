package com.sp26se041.edubridgehcm.validations.campus;

import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.models.Campus;
import com.sp26se041.edubridgehcm.models.CampusScheduleTemplate;
import com.sp26se041.edubridgehcm.models.Counsellor;
import com.sp26se041.edubridgehcm.models.ConsultationOfflineRequest;
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
            List<CounsellorSlot> allCurrentSlots,
            Integer assignmentCampaignId) {

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

            // Cùng khung theo template + khoảng ngày + (nếu có) cùng chiến dịch — nhiều CV được phép cùng khung.
            List<Integer> existingCounsellorIds = allCurrentSlots.stream()
                    .filter(s -> s.getStatus() != Status.SLOT_UNASSIGNED
                            && s.getCampusScheduleTemplate().getId().equals(template.getId())
                            && s.getStartDate().equals(request.getStartDate())
                            && s.getEndDate().equals(request.getEndDate()))
                    .filter(s -> {
                        if (assignmentCampaignId == null) {
                            return true;
                        }
                        Integer cid = s.getAdmissionCampaign() == null ? null : s.getAdmissionCampaign().getId();
                        return cid == null || cid.equals(assignmentCampaignId);
                    })
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

    /**
     * Trạng thái coi là đã cam kết với phụ huynh: không xóa slot / hủy gán trực tiếp;
     * cần điều chuyển (re-assign) hoặc xử lý nghiệp vụ khác trước.
     */
    public static final List<Status> BLOCKING_CONSULTATION_STATUSES_FOR_UNASSIGN = List.of(
            Status.CONSULTATION_PENDING,
            Status.CONSULTATION_CONFIRMED,
            Status.CONSULTATION_IN_PROGRESS
    );

    public static boolean hasBlockingConsultationRequests(CounsellorSlot slot) {
        List<ConsultationOfflineRequest> reqs = slot.getConsultationOfflineRequests();
        if (reqs == null || reqs.isEmpty()) {
            return false;
        }
        return reqs.stream().anyMatch(req -> BLOCKING_CONSULTATION_STATUSES_FOR_UNASSIGN.contains(req.getStatus()));
    }

    /**
     * Hủy gán / xóa slot: chặn khi đã có phụ huynh đăng ký ở trạng thái chờ, đã xác nhận hoặc đang diễn ra — cần điều chuyển (re-assign) trước.
     */
    public static void assertNoBlockingConsultationForUnassignDelete(CounsellorSlot slot) {
        if (hasBlockingConsultationRequests(slot)) {
            throw new IllegalArgumentException(
                    "Không thể hủy lịch vì đã có phụ huynh đăng ký. Vui lòng dời lịch cho phụ huynh trước.");
        }
    }

    /**
     * Khi sửa khung mẫu (template), slot đã gán hoặc lịch hẹn đang xử lý không được đồng bộ tự động — chặn cập nhật.
     */
    public static String reasonTemplateUpdateBlocked(List<CounsellorSlot> slotsLinkedToTemplate) {
        if (slotsLinkedToTemplate == null || slotsLinkedToTemplate.isEmpty()) {
            return null;
        }
        for (CounsellorSlot slot : slotsLinkedToTemplate) {
            if (slot.getStatus() != null && slot.getStatus() != Status.SLOT_UNASSIGNED) {
                return "Không thể sửa khung lịch khi đã có slot gán theo khung này (slot không còn trạng thái chưa gán). "
                        + "Hãy gỡ slot hoặc xử lý xong lịch trước khi chỉnh khung mẫu.";
            }
            List<ConsultationOfflineRequest> reqs = slot.getConsultationOfflineRequests();
            if (reqs != null && reqs.stream().anyMatch(r -> BLOCKING_CONSULTATION_STATUSES_FOR_UNASSIGN.contains(r.getStatus()))) {
                return "Không thể sửa khung lịch khi còn lịch hẹn ở trạng thái Chờ xác nhận, Đã xác nhận hoặc Đang diễn ra gắn với slot của khung này.";
            }
        }
        return null;
    }
}
