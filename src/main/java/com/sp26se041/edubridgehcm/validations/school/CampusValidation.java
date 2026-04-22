package com.sp26se041.edubridgehcm.validations.school;

import com.sp26se041.edubridgehcm.enums.BoardingType;
import com.sp26se041.edubridgehcm.repositories.AccountRepo;
import com.sp26se041.edubridgehcm.repositories.CampusRepo;
import com.sp26se041.edubridgehcm.requests.CreateCampusRequest;

import java.util.Locale;

public class CampusValidation {

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";

    public static String validateCreateCampus(CreateCampusRequest request, AccountRepo accountRepo, CampusRepo campusRepo, int schoolId) {

        if (request == null) {
            return "Dữ liệu yêu cầu không được để trống";
        }

        String email = normalize(request.getEmail());

        if (email == null) {
            return "Email không được để trống";
        }

        if (email.length() > 100) {
            return "Email không được vượt quá 100 ký tự";
        }

        if (!email.matches(EMAIL_REGEX)) return "Định dạng Email không hợp lệ";

        if (accountRepo.findByEmail(email).isPresent()) {
            return "Email này đã được sử dụng trên hệ thống";
        }

        String address = normalize(request.getAddress());
        if (address == null) {
            return "Địa chỉ không được để trống";
        }

        if (address.length() > 250) {
            return "Địa chỉ không được vượt quá 250 ký tự";
        }

        String phoneErr = validateHotline(request.getPhone());

        if (phoneErr != null) return "Số điện thoại cơ sở " + phoneErr;

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

    public static String validateHotline(String hotline) {

        if (hotline == null || hotline.isBlank()) {
            return "không được để trống";
        }

        // Loại bỏ khoảng trắng, dấu chấm, dấu gạch ngang
        String cleanHotline = hotline.replaceAll("[\\s.\\-]", "");

        // Regex:
        // Nhóm 1: (0|84) + (đầu số 2,3,5,7,8,9) + (7 đến 9 chữ số sau đó) -> Cho di động và số bàn
        // Nhóm 2: 1800 + (4 đến 6 chữ số)
        // Nhóm 3: 1900 + (4 đến 6 chữ số)
        String regex = "^((0|84)(3|5|7|8|9|2)([0-9]{7,9})|1800[0-9]{4,6}|1900[0-9]{4,6})$";

        if (!cleanHotline.matches(regex)) {
            return "Định dạng không hợp lệ";
        }

        return null;
    }
}
