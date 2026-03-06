package com.sp26se041.edubridgehcm.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Relationship {
    FATHER("father"),
    MOTHER("mother"),
    GUARDIAN("guardian"),
    SIBLING("sibling"),
    OTHER("other");
    private final String value;
}
