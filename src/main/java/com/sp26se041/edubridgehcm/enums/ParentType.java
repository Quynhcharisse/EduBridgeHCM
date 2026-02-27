package com.sp26se041.edubridgehcm.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ParentType {
    CAMPUS("campus"),
    POST("post");
    private final String value;
}
