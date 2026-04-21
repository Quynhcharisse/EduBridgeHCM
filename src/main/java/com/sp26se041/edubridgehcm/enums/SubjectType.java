package com.sp26se041.edubridgehcm.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SubjectType {
    REGULAR_SUBJECT("regular"),
    FOREIGN_LANGUAGE_SUBJECT("foreign_language"),
    THPT_SUBJECT("thpt");
    private final String value;
}
