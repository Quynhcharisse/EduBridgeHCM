package com.sp26se041.edubridgehcm.requests;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateHolidayRequest {

    String title;

    LocalDate startDate;

    LocalDate endDate;

    // true: Áp dụng toàn trường (campus_id sẽ null)
    // false: Chỉ áp dụng cho 1 campus cụ thể
    Boolean isGlobal;

    // ID của campus nhận lịch nghỉ (Bắt buộc nếu isGlobal = false)
    Integer campusId;

    // true: Tư vấn viên nghỉ (ẩn slot)
    // false: Chỉ học sinh nghỉ (vẫn hiện slot)
    Boolean applyToConsultant;

    // Mặc định false. Nếu Backend báo có lịch đã đặt (Conflict),
    // Frontend sẽ gửi lại request với forceCreate = true để xác nhận hủy.
    Boolean forceCreate; //==> Cờ xác nhận (Xử lý Level 2)
}
