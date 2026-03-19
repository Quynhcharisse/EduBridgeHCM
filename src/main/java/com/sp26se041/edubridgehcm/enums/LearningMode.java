package com.sp26se041.edubridgehcm.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LearningMode {

    //cách học/khung thời gian học (day school, half day, boarding...)
    DAY_SCHOOL("day school"),
    BOARDING("boarding"),
    SEMI_BOARDING("semi boarding"),
    HALF_DAY("half day");

    private final String value;
}
