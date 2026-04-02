package com.sp26se041.edubridgehcm.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FeeUnit {

    YEAR("year"),           // Học phí theo năm (Phổ biến nhất)
    SEMESTER("semester"),   // Học phí theo học kỳ
    QUARTER("quarter"),     // Học phí theo quý (Một số trường quốc tế chia 4 đợt)
    MONTH("month");         // Học phí theo tháng (Thường cho các khóa học ngắn hạn/ngoại khóa)

    private final String value;
}
