package com.sp26se041.edubridgehcm.validations.school;

import com.sp26se041.edubridgehcm.enums.BoardingType;
import com.sp26se041.edubridgehcm.repositories.AccountRepo;
import com.sp26se041.edubridgehcm.repositories.CampusRepo;
import com.sp26se041.edubridgehcm.requests.CreateCampusRequest;

import java.util.Locale;

public class CampusValidation {

    public static String validateCreateCampus(CreateCampusRequest request, AccountRepo accountRepo, CampusRepo campusRepo, int schoolId) {
        if (request == null) {
            return "Dữ liệu yêu cầu không được để trống";
        }

        if (normalize(request.getEmail()) == null) {
            return "Email không được để trống";
        }

        if (normalize(request.getEmail()).length() > 100) {
            return "Email không được vượt quá 100 ký tự";
        }

        if (accountRepo.findByEmail(normalize(request.getEmail())).isPresent()) {
            return "Email này đã được sử dụng trên hệ thống";
        }

        if (normalize(request.getAddress()) == null) {
            return "Địa chỉ không được để trống";
        }

        if (normalize(request.getAddress()).length() > 250) {
            return "Địa chỉ không được vượt quá 250 ký tự";
        }

        if (normalize(request.getPhone()) == null) {
            return "Số điện thoại không được để trống";
        }

        if (!normalize(request.getPhone()).matches("^0\\d{9}$")) {
            return "Số điện thoại không hợp lệ (phải bắt đầu bằng số 0 và có đúng 10 chữ số)";
        }

        if (normalize(request.getCity()) == null) {
            return "Vui lòng chọn Tỉnh/Thành phố";
        }

        if (normalize(request.getDistrict()) == null) {
            return "Vui lòng chọn Quận/Huyện";
        }

        if (normalize(request.getWard()) == null) {
            return "Vui lòng chọn Phường/Xã";
        }

        if (parseBoardingType(request.getBoardingType()) == null) {
            return "Loại hình nội trú không hợp lệ. Các giá trị chấp nhận: NONE, FULL_BOARDING, SEMI_BOARDING, BOTH";
        }

        return null;
    }

    public static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public static BoardingType parseBoardingType(String value) {
        String normalized = normalize(value);
        if (normalized == null) {
            return null;
        }

        String enumKey = normalized.toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');

        try {
            return BoardingType.valueOf(enumKey);
        } catch (IllegalArgumentException ignored) {
            for (BoardingType boardingType : BoardingType.values()) {
                if (boardingType.getValue().equalsIgnoreCase(normalized)) {
                    return boardingType;
                }
            }
            return null;
        }
    }
}
