package com.sp26se041.edubridgehcm.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GradeLevel {
    GRADE_06("grade_06"),
    GRADE_07("grade_07"),
    GRADE_08("grade_08"),
    GRADE_09("grade_09");
    private final String value;
}
