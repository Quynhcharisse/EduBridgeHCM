package com.sp26se041.edubridgehcm.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BoardingType {
    DAY_STUDENT("day student"),
    SEMI_BOARDING("semi boarding"),
    FULL_BOARDING("full boarding");
    private final String value;
}
