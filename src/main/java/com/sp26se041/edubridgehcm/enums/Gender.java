package com.sp26se041.edubridgehcm.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Gender {
    GIRL("girl"),
    BOY("boy"),
    MALE("male"),
    FEMALE("female");

    private final String value;
}
