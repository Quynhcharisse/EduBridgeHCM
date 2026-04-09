package com.sp26se041.edubridgehcm.validations.campus;

import com.sp26se041.edubridgehcm.enums.ResourceType;
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
            return "Email is already in use";
        }

        // 2. KIỂM TRA QUOTA (Logic mới)
        // Lấy hạn ngạch của Campus cho loại COUNSELLOR
        var quotaOpt = quotaRepo.findByCampusIdAndResourceType(campusId, ResourceType.COUNSELLOR);

        if (quotaOpt.isEmpty()) {
            return "This campus has not been allocated a quota for Counsellors.";
        }

        int maxQuota = quotaOpt.get().getMaxQuota();

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
