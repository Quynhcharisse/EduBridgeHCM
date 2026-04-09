package com.sp26se041.edubridgehcm.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CategoryTemplate {

    SCHOOL_INFO_TEMPLATE("school_info_template"),
    CAMPUS_INF0_TEMPLATE("campus_inf0_template"),;

    private final String value;

}
