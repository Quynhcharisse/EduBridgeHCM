package com.sp26se041.edubridgehcm.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CurriculumType {
    NATIONAL("national"),
    INTERNATIONAL("international"),
    INTEGRATED("integrated");

    private final String description;
}
