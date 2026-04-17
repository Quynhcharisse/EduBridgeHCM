package com.sp26se041.edubridgehcm.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum HolidayImpactLevel {
    STUDENT_ONLY("Học sinh nghỉ - Tư vấn viên vẫn làm việc tại trường và tiếp khách bình thường"),
    STAFF_ONLY("Nhân viên nghỉ - Học sinh vẫn học, nhưng hệ thống khóa các slot đặt lịch hẹn tư vấn."),
    ALL_SHUTDOWN("Nghỉ toàn trường - Khóa toàn bộ hoạt động dạy học và lịch hẹn tư vấn trên hệ thống."),
    ONLINE_ONLY("Làm việc từ xa - Cho phép đặt lịch tư vấn nhưng chỉ qua hình thức trực tuyến");

    private final String value;
}
