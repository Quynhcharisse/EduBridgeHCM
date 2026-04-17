package com.sp26se041.edubridgehcm.validations.campus;

import com.sp26se041.edubridgehcm.enums.ResourceType;
import com.sp26se041.edubridgehcm.models.CampusResourceQuota;
import com.sp26se041.edubridgehcm.repositories.AccountRepo;
import com.sp26se041.edubridgehcm.repositories.CampusResourceQuotaRepo;
import com.sp26se041.edubridgehcm.repositories.CounsellorRepo;
import com.sp26se041.edubridgehcm.requests.CreateAccountCounsellorRequest;

public class CounsellorValidation {

    public static String validateCreateCounsellor(CreateAccountCounsellorRequest request,
                                                  AccountRepo accountRepo,
                                                  CampusResourceQuotaRepo quotaRepo,
                                                  CounsellorRepo counsellorRepo,
                                                  int campusId
    ) {
        String email = normalize(request.getEmail());
        if (email == null) {
            return "Email không được để trống";
        }

        if (email.length() > 100) {
            return "Email không được vượt quá 100 ký tự";
        }

        if (!isValidEmail(email)) {
            return "Địa chỉ email không hợp lệ";
        }

        if (accountRepo.findByEmail(email).isPresent()) {
            return "Email này đã được đăng ký trên hệ thống.";
        }

        if (counsellorRepo.existsByAccount_Email(email)) {
            return "Email này đã được chỉ định cho một tư vấn viên khác.";
        }

        // --- 2. KIỂM TRA MUA GÓI & HẠN NGẠCH ---
        // Lấy hạn ngạch của cơ sở (Campus) cho loại TƯ VẤN VIÊN (COUNSELLOR)
        var quotaOpt = quotaRepo.findByCampusIdAndResourceType(campusId, ResourceType.COUNSELLOR);

        // Trường hợp 1: Không tìm thấy bản ghi Quota -> Chưa mua gói hoặc chưa được phân bổ
        if (quotaOpt.isEmpty()) {
            return "Tính năng bị khóa: Cơ sở này chưa đăng ký gói dịch vụ hoặc chưa được phân bổ hạn ngạch tư vấn viên.";
        }

        CampusResourceQuota quota = quotaOpt.get();
        int maxQuota = quota.getMaxQuota();

        // Trường hợp 2: Gói cước tồn tại nhưng maxQuota = 0 (Gói không cho phép tạo thêm)
        if (maxQuota <= 0) {
            return "Gói dịch vụ hiện tại không hỗ trợ tạo tư vấn viên. Vui lòng nâng cấp gói dịch vụ của bạn.";
        }

        // Đếm số lượng Tư vấn viên hiện có của cơ sở
        long currentCount = counsellorRepo.countByCampusId(campusId);

        if (currentCount >= maxQuota) {
            return "Cơ sở đã đạt giới hạn hạn ngạch tư vấn viên (" + maxQuota + ").";
        }

        return null;
    }

    private static boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}