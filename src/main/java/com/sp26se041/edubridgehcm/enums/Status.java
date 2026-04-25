package com.sp26se041.edubridgehcm.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.util.Set;

@Getter
@RequiredArgsConstructor
public enum Status {
    //With status account
    ACCOUNT_PENDING_VERIFY("pending verify"),
    ACCOUNT_ACTIVE("active"),
    ACCOUNT_RESTRICTED("restricted"),
    ACCOUNT_INACTIVE("inactive"),

    //With status campus
    ACTIVE("active"),
    VERIFIED("verified"),
    PUBLISH("publish"),

    //With status cho việc kiểm soát tính năng (Hạn ngạch/Gói) ==> để block tính năng
    FEATURE_LOCKED_NO_PACKAGE("locked_no_package"),
    FEATURE_LOCKED_QUOTA_FULL("locked_quota_full"),
    FEATURE_AVAILABLE("available"),

    //With status for admissions
    OPEN_ADMISSION_CAMPAIGN("open"),
    CANCELLED_ADMISSION_CAMPAIGN("cancelled"),
    DRAFT_ADMISSION_CAMPAIGN("draft"), //tạo mới ban đầu admission campaign

    OPEN("open"),
    UPCOMING_OFFERING("upcoming"),
    DRAFT("draft"),
    CLOSED("closed"), //schoo admin pause thu cong
    PAUSED("paused"),
    FULL("fulled"), //offering het quota
    EXPIRED("expired"),//campaign qua enddate

    OFFERING_DRAFT("offering_draft"),
    OFFERING_ACTIVE("offering_active"),
    OFFERING_INACTIVE("offering_inactive"),

    //With status for curriculum
    CUR_DRAFT("draft"),      // Bản nháp, chưa được dùng để tuyển sinh
    CUR_ACTIVE("active"),    // Đang áp dụng cho khóa học
    CUR_ARCHIVED("archived"), // Lưu trữ (phiên bản cũ đã bị thay thế)

    //With status for program
    PRO_ACTIVE("active"),    // Đang áp dụng cho khóa học
    PRO_DRAFT("draft"),
    PRO_INACTIVE("inactive"), // Không còn áp dụng cho khóa học nhưng vẫn giữ lại để tham chiếu lịch sử

    PAYMENT_PENDING("pending payment"),
    PAYMENT_SUCCESS("success payment"),
    PAYMENT_FAILED("failed payment"),

    POST_ACTIVE("active"),    // Đang áp dụng cho post
    POST_DISABLED("disabled"),

    // status for package service fee
    PACKAGE_ACTIVE("active"),
    PACKAGE_DRAFT("draft"),
    PACKAGE_INACTIVE_PENDING("pending deactive"),
    PACKAGE_DEACTIVATED("deactive"),
    PACKAGE_EXPIRED("expired"),

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
    EVENT_CANCELLED("cancelled"),

    //PERSONALITY TYPE STATUS
    PERSONALITY_TYPE_ACTIVE("active"),
    PERSONALITY_TYPE_INACTIVE("inactive"),

    //SUBJECT STATUS
    SUBJECT_ACTIVE("active"),
    SUBJECT_INACTIVE("inactive"),

    //with status ==> booking ==> đặt lịch tư vấn trạng thái của counsellor slot
    BOOKED("booked"),
    CANCELLED("cancelled"),
    AVAILABLE("available"),
    DISABLED("disabled"),
    SLOT_UNASSIGNED("slot_unassigned"),

    RESERVATION_OPEN("reservation_open"),
    RESERVATION_DRAFT("reservation_draft"),
    RESERVATION_PAYMENT_PENDING("reservation_payment_pending"),
    RESERVATION_PAYMENT_SUCCESS("reservation_payment_success"),
    RESERVATION_PAYMENT_FAILED("reservation_payment_failed"),
    RESERVATION_CANCELLED("reservation_cancelled"),
    RESERVATION_REJECTED("reservation_rejected");


    private final String value;

    public boolean isFeatureAvailable() {
        return this == FEATURE_AVAILABLE;
    }

    public String getDisplayMessage() {
        return switch (this) {
            case FEATURE_LOCKED_NO_PACKAGE -> "Cơ sở chưa đăng ký gói dịch vụ này. Vui lòng liên hệ để kích hoạt.";
            case FEATURE_LOCKED_QUOTA_FULL -> "Hạn ngạch đã sử dụng hết. Vui lòng nâng cấp gói để tiếp tục.";
            case FEATURE_AVAILABLE -> "Tính năng sẵn dùng.";
            default -> "";
        };
    }

    /**
     * Trạng thái đơn giữ chỗ còn hiệu lực để tính quota.
     */
    public static Set<Status> activeReservationStatuses() {
        return Set.of(
                RESERVATION_OPEN,
                RESERVATION_DRAFT,
                RESERVATION_PAYMENT_PENDING,
                RESERVATION_PAYMENT_SUCCESS
        );
    }
}
