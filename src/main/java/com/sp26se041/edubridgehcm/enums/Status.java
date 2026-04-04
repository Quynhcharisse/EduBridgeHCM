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
    OPEN_ADMISSION_CAMPAIGN("open"), //tao moi
    CANCELLED_ADMISSION_CAMPAIGN("cancelled"),
    DRAFT_ADMISSION_CAMPAIGN("draft"), //tạo mới ban đầu admission campaign

    OPEN("open"),
    DRAFT("draft"),
    CLOSED("closed"), //schoo admin pause thu cong
    PAUSED("paused"), //school admin dong thu cong
    FULL("fulled"), //offering het quota
    EXPIRED("expired"),//campaign qua enddate

    //With status for curriculum
    CUR_DRAFT("draft"),      // Bản nháp, chưa được dùng để tuyển sinh
    CUR_ACTIVE("active"),    // Đang áp dụng cho khóa học
    CUR_ARCHIVED("archived"), // Lưu trữ (phiên bản cũ đã bị thay thế)

    //With status for program
    PRO_ACTIVE("active"),    // Đang áp dụng cho khóa học
    PRO_DRAFT("draft"),
    PRO_INACTIVE("inactive"), // Không còn áp dụng cho khóa học nhưng vẫn giữ lại để tham chiếu lịch sử

    //With status consultation appointment
    CONSULTATION_PENDING("pending"),
    CONSULTATION_CONFIRMED("confirmed"),
    CONSULTATION_IN_PROGRESS("in-progress"),
    CONSULTATION_COMPLETED("completed"),
    CONSULTATION_CANCELLED("cancelled"),
    CONSULTATION_NO_SHOW("no-show"),

    //With status conservation
    CONVERSATION_ACTIVE("active"),
    CONVERSATION_PENDING("pending"),
    CONVERSATION_BLOCKED("blocked"),

    //With status message
    MESSAGE_SENT("sent"),
    MESSAGE_READ("read"),

    //With status event
    EVENT_UPCOMING("upcoming"),
    EVENT_ONGOING("ongoing"),
    EVENT_FINISHED("finished"),
    EVENT_CANCELLED("cancelled");

    private final String value;
}
