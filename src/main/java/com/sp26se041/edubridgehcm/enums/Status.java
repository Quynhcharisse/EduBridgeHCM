package com.sp26se041.edubridgehcm.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Status {
    //With status account
    ACCOUNT_ACTIVE("active"),
    ACCOUNT_PENDING_VERIFY("pending verify"),

    //With status school & campus
    VERIFIED("verified"),
    UNVERIFIED("unverified"),

    //With status consultation appointment
    PENDING("pending"),
    CONFIRMED("confirmed"),
    IN_PROGRESS("in-progress"),
    COMPLETED("completed"),
    CANCELLED("cancelled"),
    NO_SHOW("no-show");

    private final String value;
}
