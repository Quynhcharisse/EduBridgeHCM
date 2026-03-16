package com.sp26se041.edubridgehcm.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BoardingType {
    // dịch vụ lưu trú của campus (không có, bán trú, nội trú, cả hai)
    NONE("none"),
    FULL_BOARDING("full boarding"),
    SEMI_BOARDING("semi boarding"),
    BOTH("both");

    private final String value;
}
