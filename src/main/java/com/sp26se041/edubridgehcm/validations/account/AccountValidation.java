package com.sp26se041.edubridgehcm.validations.account;

import com.sp26se041.edubridgehcm.enums.BoardingType;
import com.sp26se041.edubridgehcm.enums.Gender;
import com.sp26se041.edubridgehcm.enums.Relationship;
import com.sp26se041.edubridgehcm.enums.Role;
import com.sp26se041.edubridgehcm.models.Account;
import com.sp26se041.edubridgehcm.requests.UpdateProfileRequest;
import com.sp26se041.edubridgehcm.utils.AccountRestrictionUtil;

import java.util.Arrays;

public class AccountValidation {

    public static String updateProfileValidation(UpdateProfileRequest request, Account account) {

        if (account == null) {
            return "Account does not exist";
        }

        if (request == null) {
            return "Request body is required";
        }

        if (AccountRestrictionUtil.isRestricted(account)) {
            return "Your account is restricted";
        }

        if (account.getRole() == Role.ADMIN) {
            return "Admin does not support this profile update API";
        }

        if (account.getRole() == Role.PARENT) {

            if (request.getCounsellorData() != null || request.getCampusData() != null) {
                return "Only parentData is allowed for parent role";
            }

            if (request.getParentData() == null) {
                return "Require parent data";
            }

            String parentName = normalize(request.getParentData().getName());
            String parentPhone = normalize(request.getParentData().getPhone());
            String parentOccupation = normalize(request.getParentData().getOccupation());
            String parentWorkplace = normalize(request.getParentData().getWorkplace());
            String parentAddress = normalize(request.getParentData().getCurrentAddress());
            String idCardNumber = normalize(request.getParentData().getIdCardNumber());
            boolean isFirstLogin = account.getFirstLogin();

            if (isFirstLogin && idCardNumber == null) {
                return "Require parent id card number on first login";
            }

            if (idCardNumber != null && !isExactDigits(idCardNumber)) {
                return "Parent id card number must contain exactly 12 digits";
            }

            if (!isFirstLogin && idCardNumber != null && !idCardNumber.equals(account.getParent().getIdCardNumber())) {
                return "Parent id card number can only be updated on first login";
            }

            if (parentName == null) {
                return "Require parent name";
            }

            if (parseGender(request.getParentData().getGender()) == null) {
                return "Invalid parent gender";
            }

            if (parseRelationship(request.getParentData().getRelationship()) == null) {
                return "Invalid parent relationship";
            }

            if (parentPhone == null) {
                return "Require parent phone";
            }

            if (!isValidPhoneNumber(parentPhone)) {
                return "Parent phone number must contain exactly 10 digits and start with 03, 07, 08, or 09";
            }

            if (parentOccupation == null) {
                return "Require parent occupation";
            }

            if (hasMaxWords(parentOccupation)) {
                return "Parent occupation must not exceed 100 words";
            }

            if (parentWorkplace == null) {
                return "Require parent workplace";
            }

            if (hasMaxWords(parentWorkplace)) {
                return "Parent workplace must not exceed 100 words";
            }

            if (parentAddress == null) {
                return "Require parent address";
            }

            if (hasMaxWords(parentAddress)) {
                return "Parent address must not exceed 100 words";
            }

            return "";
        }

        if (account.getRole() == Role.COUNSELLOR) {

            if (request.getParentData() != null || request.getCampusData() != null) {
                return "Only counsellorData is allowed for counsellor role";
            }

            if (request.getCounsellorData() == null) {
                return "Require counsellor data";
            }

            if (normalize(request.getCounsellorData().getName()) == null) {
                return "Require counsellor name";
            }
            return "";
        }

        if (account.getRole() == Role.SCHOOL) {

            if (request.getParentData() != null || request.getCounsellorData() != null) {
                return "Only campusData is allowed for school role";
            }

            if (request.getCampusData() == null) {
                return "Require campus data";
            }

            if (normalize(request.getCampusData().getPhoneNumber()) == null) {
                return "Require campus phone number";
            }

            if (!isValidPhoneNumber(request.getCampusData().getPhoneNumber())) {
                return "Campus phone number must contain exactly 10 digits and start with 03, 07, 08, or 09";
            }

            if (normalize(request.getCampusData().getCity()) == null) {
                return "Require campus city";
            }

            if (normalize(request.getCampusData().getDistrict()) == null) {
                return "Require campus district";
            }


            if (parseBoardingType(normalize(request.getCampusData().getBoardingType())) == null) {
                return "Require campus boarding type";
            }

            if (normalize(request.getCampusData().getAddress()) == null) {
                return "Require campus address";
            }

            if (hasMaxWords(request.getCampusData().getAddress())) {
                return "Campus address must not exceed 100 words";
            }

            if (account.getCampus().getIsPrimaryBranch()) {
                if (request.getCampusData().getSchoolData() == null) {
                    return "Require school data for primary branch";
                }

                if (normalize(request.getCampusData().getSchoolData().getDescription()) == null) {
                    return "Require school description";
                }

                if (normalize(request.getCampusData().getSchoolData().getDescription()).length() > 500) {
                    return "School description must not exceed 500 characters";
                }

                if (normalize(request.getCampusData().getSchoolData().getHotline()) != null
                        && !isValidHotline(normalize(request.getCampusData().getSchoolData().getHotline()))) { // Dùng isValidHotline
                    return "School hotline is invalid (should start with 02, 03, 07, 08, 09, 1800, or 1900)";
                }

                if (normalize(request.getCampusData().getSchoolData().getLogoUrl()) == null) {
                    return "Require school logoUrl";
                }
                if (!normalize(request.getCampusData().getSchoolData().getLogoUrl()).startsWith("http")) {
                    return "School logoUrl must be a valid URL";
                }

                if (normalize(request.getCampusData().getSchoolData().getWebsiteUrl()) == null) {
                    return "Require school websiteUrl";
                }
                if (!normalize(request.getCampusData().getSchoolData().getWebsiteUrl()).startsWith("http")) {
                    return "School websiteUrl must be a valid URL";
                }
            }

            return "";
        }

        return "Role does not support profile update";
    }

    public static Gender parseGender(String value) {
        String normalizedValue = normalize(value);
        if (normalizedValue == null) {
            return null;
        }

        return Arrays.stream(Gender.values())
                .filter(g -> g.getValue().equalsIgnoreCase(normalizedValue) || g.name().equalsIgnoreCase(normalizedValue))
                .findFirst()
                .orElse(null);
    }

    public static Relationship parseRelationship(String value) {
        String normalizedValue = normalize(value);
        if (normalizedValue == null) {
            return null;
        }

        return Arrays.stream(Relationship.values())
                .filter(r -> r.getValue().equalsIgnoreCase(normalizedValue) || r.name().equalsIgnoreCase(normalizedValue))
                .findFirst()
                .orElse(null);
    }

    public static BoardingType parseBoardingType(String value) {
        String normalizedValue = normalize(value);
        if (normalizedValue == null) {
            return null;
        }

        return Arrays.stream(BoardingType.values())
                .filter(r -> r.getValue().equalsIgnoreCase(normalizedValue) || r.name().equalsIgnoreCase(normalizedValue))
                .findFirst()
                .orElse(null);
    }

    public static boolean isValidPhoneNumber(String value) {
        String normalizedValue = normalize(value);
        return normalizedValue != null && normalizedValue.matches("^(03|07|08|09)\\d{8}$");
    }

    public static boolean isValidHotline(String value) {
        String normalizedValue = normalize(value);
        return normalizedValue != null && normalizedValue.matches("^(02|03|07|08|09|18|19)\\d{6,9}$");
    }

    public static boolean isExactDigits(String value) {
        String normalizedValue = normalize(value);
        return normalizedValue != null && normalizedValue.matches("^\\d{" + 12 + "}$");
    }

    public static boolean hasMaxWords(String value) {
        String normalizedValue = normalize(value);
        if (normalizedValue == null) {
            return true;
        }

        return normalizedValue.split("\\s+").length > 100;
    }

    public static String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
