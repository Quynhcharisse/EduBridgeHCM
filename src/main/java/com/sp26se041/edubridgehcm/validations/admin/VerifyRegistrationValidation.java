package com.sp26se041.edubridgehcm.validations.admin;

import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.models.SchoolRegistrationRequest;
import com.sp26se041.edubridgehcm.repositories.SchoolRepo;

public class VerifyRegistrationValidation {

    public static String validationVerifyRegistration(int requestId, SchoolRegistrationRequest request, SchoolRepo schoolRepo) {

        if (request == null) {
            return "Không tìm thấy yêu cầu đăng ký nào với mã ID: " + requestId;
        }

        if (request.getStatus() != Status.ACCOUNT_PENDING_VERIFY) {
            return "Yêu cầu đăng ký này đã được xử lý trước đó.";
        }

        if (schoolRepo.existsByTaxCode(request.getTaxCode().trim())) {
            return "Mã số thuế này đã tồn tại trên hệ thống.";
        }

        if (schoolRepo.existsByName(request.getSchoolName().trim())) {
            return "Tên trường này đã tồn tại trên hệ thống.";
        }

        return "";
    }
}
