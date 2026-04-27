package com.sp26se041.edubridgehcm.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ImportType {

    ALLOWED_METHODS("allowedMethods"),
    ADMISSION_PROCESSES("admissionProcesses"),
    METHOD_DOCUMENTS("byMethod"),
    MANDATORY_ALL("mandatoryAll");

    private final String sheetName;
}
