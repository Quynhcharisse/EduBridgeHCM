package com.sp26se041.edubridgehcm.requests;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

/**
 * Điều chuyển lịch hẹn tư vấn offline từ slot nguồn sang slot đích (cùng khung mẫu giờ + thứ, cùng chiến dịch khi có).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReassignConsultationsRequest {

    Integer fromSlotId;

    Integer toSlotId;

    /** Nếu null hoặc rỗng: tất cả yêu cầu đang "cam kết" (chờ / đã xác nhận / đang diễn ra) trên slot nguồn. */
    List<Long> consultationRequestIds;
}
