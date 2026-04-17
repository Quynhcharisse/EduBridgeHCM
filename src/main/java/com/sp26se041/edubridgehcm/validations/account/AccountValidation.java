package com.sp26se041.edubridgehcm.validations.account;

import com.sp26se041.edubridgehcm.enums.BoardingType;
import com.sp26se041.edubridgehcm.enums.Gender;
import com.sp26se041.edubridgehcm.enums.Relationship;
import com.sp26se041.edubridgehcm.enums.Role;
import com.sp26se041.edubridgehcm.models.Account;
import com.sp26se041.edubridgehcm.requests.UpdateProfileRequest;
import com.sp26se041.edubridgehcm.utils.AccountRestrictionUtil;
import com.sp26se041.edubridgehcm.utils.ConfigSystemUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class AccountValidation {

    public static String updateProfileValidation(UpdateProfileRequest request, Account account, Map<String, Object> mediaConfig) {

        if (account == null) {
            return "Tài khoản không tồn tại";
        }

        if (request == null) {
            return "Dữ liệu yêu cầu không được để trống";
        }

        if (AccountRestrictionUtil.isRestricted(account)) {
            return "Tài khoản của bạn hiện đang bị hạn chế chức năng";
        }

        if (account.getRole() == Role.ADMIN) {
            return "Quản trị viên không hỗ trợ cập nhật hồ sơ qua chức năng này";
        }

        // --- VALIDATE PHỤ HUYNH ---
        if (account.getRole() == Role.PARENT) {

            if (request.getCounsellorData() != null || request.getCampusData() != null) {
                return "Vai trò phụ huynh chỉ được phép cập nhật dữ liệu phụ huynh";
            }

            if (request.getParentData() == null) {
                return "Yêu cầu cung cấp thông tin phụ huynh";
            }

            String parentName = normalize(request.getParentData().getName());
            String parentPhone = normalize(request.getParentData().getPhone());
            String parentOccupation = normalize(request.getParentData().getOccupation());
            String parentWorkplace = normalize(request.getParentData().getWorkplace());
            String parentAddress = normalize(request.getParentData().getCurrentAddress());
            String idCardNumber = normalize(request.getParentData().getIdCardNumber());
            boolean isFirstLogin = account.getFirstLogin();

            if (isFirstLogin && idCardNumber == null) {
                return "Vui lòng nhập số CCCD/CMND trong lần đăng nhập đầu tiên";
            }

            if (idCardNumber != null && !isExactDigits(idCardNumber)) {
                return "Số CCCD phải bao gồm chính xác 12 chữ số";
            }

            if (!isFirstLogin && idCardNumber != null && !idCardNumber.equals(account.getParent().getIdCardNumber())) {
                return "Số CCCD chỉ có thể được cập nhật trong lần đăng nhập đầu tiên";
            }

            if (parentName == null) {
                return "Vui lòng nhập họ và tên phụ huynh";
            }

            if (parseGender(request.getParentData().getGender()) == null) {
                return "Giới tính không hợp lệ";
            }

            if (parseRelationship(request.getParentData().getRelationship()) == null) {
                return "Mối quan hệ không hợp lệ";
            }

            if (parentPhone == null) {
                return "Vui lòng nhập số điện thoại phụ huynh";
            }

            if (!isValidPhoneNumber(parentPhone)) {
                return "Số điện thoại phụ huynh không hợp lệ (phải đủ 10 chữ số và bắt đầu bằng số 0)";
            }

            if (parentOccupation == null) {
                return "Vui lòng cung cấp thông tin nghề nghiệp";
            }

            if (hasMaxWords(parentOccupation)) {
                return "Thông tin nghề nghiệp không được vượt quá 100 từ";
            }

            if (parentWorkplace == null) {
                return "Vui lòng cung cấp thông tin nơi làm việc";
            }

            if (hasMaxWords(parentWorkplace)) {
                return "Thông tin nơi làm việc không được vượt quá 100 từ";
            }

            if (parentAddress == null) {
                return "Vui lòng cung cấp địa chỉ hiện tại";
            }

            if (hasMaxWords(parentAddress)) {
                return "Địa chỉ không được vượt quá 100 từ";
            }

            return "";
        }

        // --- VALIDATE TƯ VẤN VIÊN ---
        if (account.getRole() == Role.COUNSELLOR) {

            if (request.getParentData() != null || request.getCampusData() != null) {
                return "Vai trò tư vấn viên chỉ được phép cập nhật dữ liệu tư vấn";
            }

            if (request.getCounsellorData() == null) {
                return "Yêu cầu cung cấp thông tin tư vấn viên";
            }

            if (normalize(request.getCounsellorData().getName()) == null) {
                return "Vui lòng nhập họ và tên tư vấn viên";
            }
            return "";
        }

        // --- VALIDATE TRƯỜNG HỌC (CAMPUS) ---
        if (account.getRole() == Role.SCHOOL) {

            if (request.getParentData() != null || request.getCounsellorData() != null) {
                return "Vai trò trường học chỉ được phép cập nhật dữ liệu cơ sở";
            }

            if (request.getCampusData() == null) {
                return "Yêu cầu cung cấp thông tin cơ sở";
            }

            if (normalize(request.getCampusData().getPhoneNumber()) == null) {
                return "Vui lòng nhập số điện thoại cơ sở";
            }

            if (!isValidPhoneNumber(request.getCampusData().getPhoneNumber())) {
                return "Số điện thoại cơ sở không hợp lệ (phải đủ 10 chữ số và bắt đầu bằng số 0)";
            }

            if (normalize(request.getCampusData().getCity()) == null) {
                return "Vui lòng chọn Tỉnh/Thành phố";
            }

            if (normalize(request.getCampusData().getDistrict()) == null) {
                return "Vui lòng chọn Quận/Huyện";
            }

            if (parseBoardingType(normalize(request.getCampusData().getBoardingType())) == null) {
                return "Vui lòng chọn loại hình nội trú";
            }

            if (normalize(request.getCampusData().getAddress()) == null) {
                return "Vui lòng nhập địa chỉ cơ sở";
            }

            if (hasMaxWords(request.getCampusData().getAddress())) {
                return "Địa chỉ cơ sở không được vượt quá 100 từ";
            }

            if (account.getCampus().getIsPrimaryBranch()) {
                if (request.getCampusData().getSchoolData() == null) {
                    return "Cơ sở chính yêu cầu phải có thông tin chung của trường";
                }

                if (normalize(request.getCampusData().getSchoolData().getDescription()) == null) {
                    return "Vui lòng nhập mô tả giới thiệu trường";
                }

                if (normalize(request.getCampusData().getSchoolData().getDescription()).length() > 2000) {
                    return "Mô tả trường không được vượt quá 2000 ký tự";
                }

                if (normalize(request.getCampusData().getSchoolData().getHotline()) != null
                        && !isValidHotline(normalize(request.getCampusData().getSchoolData().getHotline()))) {
                    return "Số Hotline không hợp lệ (phải từ 8-11 chữ số và bắt đầu bằng 0, 18 hoặc 19)";
                }

                if (normalize(request.getCampusData().getSchoolData().getLogoUrl()) == null) {
                    return "Vui lòng cung cấp Logo trường";
                }

                String logoError = validateFileFormat(request.getCampusData().getSchoolData().getLogoUrl(), mediaConfig, "imgFormat");
                if (logoError != null) {
                    return "Logo trường: " + logoError;
                }

                if (normalize(request.getCampusData().getSchoolData().getWebsiteUrl()) == null) {
                    return "Vui lòng nhập địa chỉ Website trường";
                }
                if (!normalize(request.getCampusData().getSchoolData().getWebsiteUrl()).startsWith("http")) {
                    return "Địa chỉ Website trường phải là một đường dẫn (URL) hợp lệ";
                }
            }

            return "";
        }

        return "Vai trò hiện tại không hỗ trợ cập nhật thông tin hồ sơ";
    }

    private static String validateFileFormat(String url, Map<String, Object> mediaConfig, String typeKey) {
        String normalizedUrl = normalize(url);
        if (normalizedUrl == null) return "Đường dẫn không được để trống";

        if (!normalizedUrl.startsWith("http")) {
            return "Đường dẫn phải là một URL hợp lệ";
        }

        List<String> allowedFormats = ConfigSystemUtil.getAllowedFormats(mediaConfig, typeKey);

        if (!allowedFormats.isEmpty()) {
            boolean isValid = allowedFormats.stream()
                    .anyMatch(ext -> normalizedUrl.toLowerCase().endsWith(ext.toLowerCase()));

            if (!isValid) {
                return "định dạng không hỗ trợ. Chỉ chấp nhận: " + String.join(", ", allowedFormats);
            }
        }

        return null;
    }

    public static Gender parseGender(String value) {
        String normalizedValue = normalize(value);
        if (normalizedValue == null) return null;

        return Arrays.stream(Gender.values())
                .filter(g -> g.getValue().equalsIgnoreCase(normalizedValue) || g.name().equalsIgnoreCase(normalizedValue))
                .findFirst()
                .orElse(null);
    }

    public static Relationship parseRelationship(String value) {
        String normalizedValue = normalize(value);
        if (normalizedValue == null) return null;

        return Arrays.stream(Relationship.values())
                .filter(r -> r.getValue().equalsIgnoreCase(normalizedValue) || r.name().equalsIgnoreCase(normalizedValue))
                .findFirst()
                .orElse(null);
    }

    public static BoardingType parseBoardingType(String value) {
        String normalizedValue = normalize(value);
        if (normalizedValue == null) return null;

        return Arrays.stream(BoardingType.values())
                .filter(r -> r.getValue().equalsIgnoreCase(normalizedValue) || r.name().equalsIgnoreCase(normalizedValue))
                .findFirst()
                .orElse(null);
    }

    public static boolean isValidPhoneNumber(String value) {
        String normalizedValue = normalize(value);
        return normalizedValue != null && normalizedValue.matches("^0\\d{9}$");
    }

    public static boolean isValidHotline(String value) {
        String normalizedValue = normalize(value);
        return normalizedValue != null && normalizedValue.matches("^(0|18|19)\\d{7,10}$");
    }

    public static boolean isExactDigits(String value) {
        String normalizedValue = normalize(value);
        return normalizedValue != null && normalizedValue.matches("^\\d{12}$");
    }

    public static boolean hasMaxWords(String value) {
        String normalizedValue = normalize(value);
        if (normalizedValue == null) return false;
        return normalizedValue.split("\\s+").length > 100;
    }

    public static String normalize(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}