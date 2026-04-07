package com.sp26se041.edubridgehcm.validations.admin;

import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.models.SchoolRegistrationRequest;
import com.sp26se041.edubridgehcm.repositories.SchoolRepo;

public class VerifyRegistrationValidation {

    public static String validationVerifyRegistration(int requestId, SchoolRegistrationRequest request, SchoolRepo schoolRepo) {

        if (request == null) {
            return "No registration request with ID found: " + requestId;
        }

        if (request.getStatus() != Status.ACCOUNT_PENDING_VERIFY) {
            return "This request has been processed previously.";
        }

        if (schoolRepo.existsByTaxCode(request.getTaxCode().trim())) {
            return "This tax identification number already exists.";
        }

        if(schoolRepo.existsByName(request.getSchoolName().trim())) {
            return "School name already exists.";
        }

        return "";
    }
}
