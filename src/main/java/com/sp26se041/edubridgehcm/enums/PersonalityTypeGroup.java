package com.sp26se041.edubridgehcm.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PersonalityTypeGroup {
    ANALYST("Nhà phân tích"),
    DIPLOMAT("Nhà ngoại giao"),
    SENTINEL("Người canh gác"),
    EXPLORER("Nhà thám hiểm");
    private final String value;
}
