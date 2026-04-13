package com.sp26se041.edubridgehcm.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SessionType {
    MORNING("morning"),
    AFTERNOON("afternoon"),
    EVENING("evening");
    private final String value;
}
