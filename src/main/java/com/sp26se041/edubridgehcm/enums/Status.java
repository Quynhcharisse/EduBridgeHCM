package com.sp26se041.edubridgehcm.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Status {
    //With status account
    ACCOUNT_PENDING_VERIFY("pending verify"),
    ACCOUNT_ACTIVE("active"),
    ACCOUNT_RESTRICTED("restricted"),
    ACCOUNT_INACTIVE("inactive"),

    //With status school & campus
    VERIFIED("verified"),
    REJECTED("rejected"),

    //With status for admissions
    OPEN("open"), //tao moi
    CLOSED("closed"), //schoo/admin pause thu cong
    PAUSED("paused"), //school/admin dong thu cong
    FULL("fulled"), //offering het quota
    EXPIRED("expired"),//campaign qua enddate

    //With status consultation appointment
    CONSULTATION_PENDING("pending"),
    CONSULTATION_CONFIRMED("confirmed"),
    CONSULTATION_IN_PROGRESS("in-progress"),
    CONSULTATION_COMPLETED("completed"),
    CONSULTATION_CANCELLED("cancelled"),
    CONSULTATION_NO_SHOW("no-show"),

    //With status conservation
    CONVERSATION_ACTIVE("active"),
    CONVERSATION_BLOCKED("blocked"),

    //With status message
    MESSAGE_SENT("sent"),
    MESSAGE_READ("read");

    private final String value;
}
