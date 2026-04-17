package com.sp26se041.edubridgehcm.validations.holiday;

import com.sp26se041.edubridgehcm.enums.HolidayImpactLevel;
import com.sp26se041.edubridgehcm.models.Campus;
import com.sp26se041.edubridgehcm.requests.CreateHolidayRequest;
import com.sp26se041.edubridgehcm.requests.UpdateHolidayRequest;

import java.time.LocalDate;
import java.util.Arrays;

public class HolidayValidation {

    public static String createHolidayValidation(CreateHolidayRequest request, Campus actorCampus) {

        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            return "Tiêu đề ngày nghỉ không được để trống.";
        }
        if (request.getStartDate() == null || request.getEndDate() == null) {
            return "Ngày bắt đầu và kết thúc là bắt buộc.";
        }

        if (request.getStartDate().isAfter(request.getEndDate())) {
            return "Ngày bắt đầu không thể sau ngày kết thúc.";
        }
        if (request.getStartDate().isBefore(LocalDate.now())) {
            return "Không thể tạo ngày nghỉ cho quá khứ.";
        }

        if (request.getEndDate().isAfter(LocalDate.now().plusYears(2))) {
            return "Ngày nghỉ không được vượt quá 2 năm tính từ thời điểm hiện tại.";
        }

        try {
            HolidayImpactLevel.valueOf(request.getHolidayImpactLevel());
        } catch (Exception e) {
            return "Mức độ ảnh hưởng (Impact Level) không hợp lệ. Các giá trị cho phép: " +
                    Arrays.toString(HolidayImpactLevel.values());
        }

        if (Boolean.TRUE.equals(request.getIsGlobal())) {
            // Chỉ Primary Branch mới được tạo Global Holiday
            if (!Boolean.TRUE.equals(actorCampus.getIsPrimaryBranch())) {
                return "Chỉ cơ sở chính mới có quyền thiết lập lịch nghỉ toàn trường.";
            }
        } else {
            if (request.getCampusId() == null) {
                return "Phải chọn cơ sở (Campus) nếu không phải lịch nghỉ toàn trường.";
            }
        }

        return null;
    }

    public static String updateHolidayValidation(UpdateHolidayRequest request, Campus actorCampus) {

        if (request.getId() == null) return "ID ngày nghỉ không được để trống.";

        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) return "Tiêu đề không được để trống.";

        if (request.getStartDate() == null || request.getEndDate() == null)
            return "Ngày bắt đầu và kết thúc là bắt buộc.";

        if (request.getStartDate().isAfter(request.getEndDate())) return "Ngày bắt đầu không thể sau ngày kết thúc.";

        // Chỉ được sửa các ngày nghỉ trong tương lai
        if (request.getStartDate().isBefore(LocalDate.now())) {
            return "Không thể cập nhật ngày nghỉ về quá khứ.";
        }

        try {
            HolidayImpactLevel.valueOf(request.getHolidayImpactLevel());
        } catch (Exception e) {
            return "Mức độ ảnh hưởng không hợp lệ.";
        }

        return null;
    }
}
