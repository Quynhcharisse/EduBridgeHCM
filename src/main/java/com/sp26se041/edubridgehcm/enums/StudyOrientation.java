package com.sp26se041.edubridgehcm.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StudyOrientation {
    DOMESTIC("Domestic Academic - National High School"),
    INTERNATIONAL("International Study Abroad"),
    DUAL_PATHWAY("Dual Degree Pathway - National & International"),
    VOCATIONAL("Vocational Skills Training");

    private final String value;
}

