package com.sp26se041.edubridgehcm.validations.auth;

import com.sp26se041.edubridgehcm.enums.Role;
import com.sp26se041.edubridgehcm.requests.RegisterRequest;
import com.sp26se041.edubridgehcm.utils.ConfigSystemUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class RegisterValidation {

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";

    private static final String PHONE_REGEX = "^\\d{10}$";

    private static final String URL_REGEX = "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$";

    public static String registerValidation(RegisterRequest request, Map<String, Object> mediaConfig) {

        if (request == null) return "Dữ liệu yêu cầu không được để trống";

        if (request.getEmail() == null || request.getEmail().isBlank()) return "Email không được để trống";

        if (!Pattern.matches(EMAIL_REGEX, request.getEmail())) return "Định dạng Email không hợp lệ";

        if (request.getRole() == null || request.getRole().isBlank()) return "Vai trò người dùng không được để trống";

        Role role;

        try {
            role = Role.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            return "Vai trò không hợp lệ. Chỉ chấp nhận PARENT hoặc SCHOOL";
        }

        // 2. Phân luồng Validate theo Role
        if (role.equals(Role.SCHOOL)) {
            return validateSchoolRequest(request, mediaConfig);
        } else if (role.equals(Role.PARENT)) {
            return validateParentRequest(request);
        }

        return null;
    }

    private static String validateSchoolRequest(RegisterRequest request, Map<String, Object> mediaConfig) {

        if (request.getSchoolRequest() == null) return "Thông tin đăng ký trường học là bắt buộc";

        if (isBlank(request.getSchoolRequest().getSchoolName())) return "Tên trường không được để trống";

        if (isBlank(request.getSchoolRequest().getCampusAddress())) return "Địa chỉ cơ sở không được để trống";

        if (isBlank(request.getSchoolRequest().getTaxCode())) return "Mã số thuế không được để trống";

        if (isBlank(request.getSchoolRequest().getRepresentativeName()))
            return "Tên người đại diện không được để trống";

        if (isBlank(request.getSchoolRequest().getBusinessLicenseUrl()))
            return "Ảnh/Tệp giấy phép kinh doanh là bắt buộc";

        if (isBlank(request.getSchoolRequest().getCampusPhone())) return "Số điện thoại cơ sở không được để trống";

        if (!request.getSchoolRequest().getCampusPhone().matches(PHONE_REGEX))
            return "Số điện thoại cơ sở phải bao gồm 10 chữ số";

        if (!isBlank(request.getSchoolRequest().getHotline()) && !request.getSchoolRequest().getHotline().matches(PHONE_REGEX)) {
            return "Số Hotline phải có đủ 10 số";
        }

        String logoFormatError = validateMediaFile(request.getSchoolRequest().getLogoUrl(), mediaConfig, "imgFormat");
        if (logoFormatError != null) return "Logo trường: " + logoFormatError;

        String licenseFormatError = validateMediaFile(request.getSchoolRequest().getBusinessLicenseUrl(), mediaConfig, "docFormat");
        if (licenseFormatError != null) return "Giấy phép kinh doanh: " + licenseFormatError;

        // 4. Validate định dạng URL Website
        if (!isBlank(request.getSchoolRequest().getWebsiteUrl()) && !Pattern.matches(URL_REGEX, request.getSchoolRequest().getWebsiteUrl())) {
            return "Định dạng địa chỉ Website không hợp lệ (phải bắt đầu bằng http:// hoặc https://)";
        }

        if (request.getSchoolRequest().getFoundingDate() != null) {
            if (request.getSchoolRequest().getFoundingDate().isAfter(LocalDate.now())) {
                return "Ngày thành lập không thể lớn hơn ngày hiện tại";
            }
        } else {
            return "Vui lòng chọn ngày thành lập trường";
        }

        return null;
    }

    private static String validateParentRequest(RegisterRequest request) {
        if (isBlank(request.getAvatar())) {
            return "Ảnh đại diện từ tài khoản Google là bắt buộc";
        }
        return null;
    }

    private static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    private static String validateMediaFile(String fileUrl, Map<String, Object> mediaConfig, String formatKey) {
        if (isBlank(fileUrl)) return null;

        // Sử dụng Util để lấy List<String> đuôi file đã cấu hình (ví dụ: .jpg, .png)
        List<String> allowedFormats = ConfigSystemUtil.getAllowedFormats(mediaConfig, formatKey);

        if (!allowedFormats.isEmpty()) {
            boolean isValid = allowedFormats.stream().anyMatch(fileUrl.toLowerCase()::endsWith);
            if (!isValid) {
                return "không hỗ trợ. Chỉ chấp nhận: " + String.join(", ", allowedFormats);
            }
        }
        return null;
    }
}
