package com.sp26se041.edubridgehcm.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ConsultationAction {
    CONFIRM("confirm"),
    START("start"),
    END("end"),
    CANCEL("cancel"),
    NO_SHOW("no_show");
    private final String value;
}
