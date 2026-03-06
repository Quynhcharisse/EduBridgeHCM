package com.sp26se041.edubridgehcm.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TargetScope {
    GLOBAL("global"),
    SCHOOL_WIDE("school wide"),
    CAMPUS_SPECIFIC("campus specific");

    private final String value;
}
