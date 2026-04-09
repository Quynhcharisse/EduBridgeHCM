package com.sp26se041.edubridgehcm.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResourceType {
    COUNSELLOR("counsellor"),
    ADMISSION("admission");

    private final String value;
}
