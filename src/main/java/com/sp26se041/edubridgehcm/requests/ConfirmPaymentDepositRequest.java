package com.sp26se041.edubridgehcm.requests;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConfirmPaymentDepositRequest {
    int formId;
    // "CONFIRM" → DEPOSITED | "REJECT_PAYMENT" → quay lại OFFERING_SELECTED để PH upload lại
    String action;
    // Bắt buộc khi action = REJECT_PAYMENT
    String rejectReason;
}
