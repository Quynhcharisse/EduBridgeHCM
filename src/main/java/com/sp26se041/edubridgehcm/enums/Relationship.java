package com.sp26se041.edubridgehcm.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Relationship {
    FATHER("father"),
    MOTHER("mother"),
    GRANDFATHER("grandfather"),
    GRANDMOTHER("grandmother"),
    GUARDIAN("guardian"),
    SIBLING("sibling");
    private final String value;
}
