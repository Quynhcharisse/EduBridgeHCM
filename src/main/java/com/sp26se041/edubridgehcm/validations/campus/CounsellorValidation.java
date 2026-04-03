package com.sp26se041.edubridgehcm.validations.campus;

import com.sp26se041.edubridgehcm.repositories.AccountRepo;
import com.sp26se041.edubridgehcm.requests.CreateAccountCounsellorRequest;

public class CounsellorValidation {

    public static String validateCreateCounsellor(CreateAccountCounsellorRequest request, AccountRepo accountRepo) {

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
