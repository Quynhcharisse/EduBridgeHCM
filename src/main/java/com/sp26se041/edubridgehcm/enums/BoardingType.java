package com.sp26se041.edubridgehcm.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BoardingType {
    FULL_BOARDING("Nội trú"),
    SEMI_BOARDING("Bán trú"),
    BOTH("Cả hai (Nội trú & Bán trú)");
    private final String value;
}
