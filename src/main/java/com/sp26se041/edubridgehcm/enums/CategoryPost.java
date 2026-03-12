package com.sp26se041.edubridgehcm.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CategoryPost {
    PLATFORM_NEWS("platform news"),
    OFFICIAL_DECISION("official decision"),
    SCHOOL_NEWS("school news"),
    ADMISSION_POLICY("admission policy");

    private final String value;
}
