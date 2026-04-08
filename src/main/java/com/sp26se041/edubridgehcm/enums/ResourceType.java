package com.sp26se041.edubridgehcm.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResourceType {
    // dịch vụ lưu trú của campus (không có, bán trú, nội trú, cả hai)
    COUNSELLOR("counsellor"),
    ADMISSION("admission");

    private final String value;
}
