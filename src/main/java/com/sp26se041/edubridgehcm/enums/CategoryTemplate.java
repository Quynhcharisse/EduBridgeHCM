package com.sp26se041.edubridgehcm.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CategoryTemplate {
    SCHOOL_INFO_TEMPLATE("school_info_template"),
    CAMPUS_INFO_TEMPLATE("campus_info_template"),;
    private final String value;

}
