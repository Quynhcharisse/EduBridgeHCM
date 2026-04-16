package com.sp26se041.edubridgehcm.validations.holiday;

import com.sp26se041.edubridgehcm.models.Campus;
import com.sp26se041.edubridgehcm.requests.CreateHolidayRequest;

import java.time.LocalDate;

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
        if (Boolean.FALSE.equals(request.getIsGlobal()) && request.getCampusId() == null) {
            return "Phải chọn cơ sở (Campus) nếu không phải lịch nghỉ toàn trường.";
        }
        // Kiểm tra quyền: Chỉ Primary Branch mới được tạo Global
        if (Boolean.TRUE.equals(request.getIsGlobal()) && !Boolean.TRUE.equals(actorCampus.getIsPrimaryBranch())) {
            return "Chỉ cơ sở chính mới có quyền thiết lập lịch nghỉ toàn trường.";
        }
        return null;
    }

    public static String updateHolidayValidation() {
        return null;
    }
}
