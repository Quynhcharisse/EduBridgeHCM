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
            return "Email is required";
        }

        if (email.length() > 100) {
            return "Email exceeds 100 characters";
        }

        if (!isValidEmail(email)) {
            return "Email is invalid";
        }

        if (accountRepo.findByEmail(email).isPresent()) {
            return "This email is already registered in the system.";
        }

        if (counsellorRepo.existsByAccount_Email(email)) {
            return "This email is already assigned to another counsellor.";
        }

        // 2. KIỂM TRA MUA GÓI & HẠN NGẠCH
        // Lấy hạn ngạch của Campus cho loại COUNSELLOR
        var quotaOpt = quotaRepo.findByCampusIdAndResourceType(campusId, ResourceType.COUNSELLOR);

        // Trường hợp 1: Không tìm thấy bản ghi Quota -> Có thể chưa mua gói hoặc chưa phân bổ
        if (quotaOpt.isEmpty()) {
            return "Feature Locked: This campus has not subscribed to a service package or has not been allocated a counsellor quota.";
        }

        CampusResourceQuota quota = quotaOpt.get();
        int maxQuota = quota.getMaxQuota();

        // Trường hợp 2: Gói cước có tồn tại nhưng maxQuota được set = 0 (Gói không cho phép tạo)
        if (maxQuota <= 0) {
            return "Current service package does not support counsellor creation. Please upgrade your package.";
        }

        // Đếm số lượng Counsellor hiện có của Campus này
        // Lưu ý: Bạn nên đếm những account đang ACTIVE hoặc tồn tại trong bảng Counsellor
        long currentCount = counsellorRepo.countByCampusId(campusId);

        if (currentCount >= maxQuota) {
            return "The counsellor quota for this campus has been reached (" + maxQuota + ").";
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
