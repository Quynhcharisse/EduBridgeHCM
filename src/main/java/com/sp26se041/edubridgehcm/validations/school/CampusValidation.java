package com.sp26se041.edubridgehcm.validations.school;

import com.sp26se041.edubridgehcm.enums.BoardingType;
import com.sp26se041.edubridgehcm.repositories.AccountRepo;
import com.sp26se041.edubridgehcm.repositories.CampusRepo;
import com.sp26se041.edubridgehcm.requests.CreateCampusRequest;

import java.text.Normalizer;
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

    public static String toSafeObjectKey(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "";
        }

        // 1. normalize Unicode (tách dấu ra)
        String normalized = Normalizer.normalize(input.trim(), Normalizer.Form.NFD);

        // 2. remove dấu (accent)
        String noAccent = normalized.replaceAll("\\p{M}", "");

        // 3. xử lý riêng đ/Đ
        noAccent = noAccent.replace("đ", "d").replace("Đ", "d");

        // 4. lowercase
        String lower = noAccent.toLowerCase(Locale.ROOT);

        // 5. replace ký tự không hợp lệ -> _
        String safe = lower.replaceAll("[^a-z0-9]+", "_");

        // 6. cleanup: nhiều _ -> 1
        safe = safe.replaceAll("_+", "_");

        // 7. remove _ đầu/cuối
        safe = safe.replaceAll("^_+|_+$", "");

        return safe;
    }

    public static String mapBoardingDescription(BoardingType type) {
        return switch (type) {

            case FULL_BOARDING ->
                    "Cơ sở này cung cấp dịch vụ nội trú toàn phần, nơi học sinh sinh hoạt tại trường với chỗ ở, bữa ăn và sự chăm sóc toàn diện hằng ngày.";

            case SEMI_BOARDING ->
                    "Cơ sở này cung cấp dịch vụ bán trú, cho phép học sinh ở lại trường vào ban ngày để dùng bữa, được hỗ trợ học tập và tham gia các hoạt động ngoại khóa mà không lưu trú qua đêm.";

            case BOTH ->
                    "Cơ sở này cung cấp cả dịch vụ nội trú toàn phần và bán trú, mang đến lựa chọn linh hoạt về lưu trú và chăm sóc ban ngày để đáp ứng nhu cầu đa dạng của học sinh.";
        };
    }

    public static String generateCampusName(Integer schoolId, CampusRepo campusRepo) {
        int currentCount = campusRepo.countBySchoolId(schoolId);
        if (currentCount == 0) {
            return "Cơ sở 1 (Cơ sở chính)";
        }
        return "Cơ sở " + (currentCount + 1);
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
