package com.sp26se041.edubridgehcm.models;

import com.sp26se041.edubridgehcm.enums.Status;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.NullMarked;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "payment_transaction")
@FieldDefaults(level = AccessLevel.PRIVATE)
@NullMarked
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @ManyToOne
    @JoinColumn(name = "school_id")
    School school;

    @ManyToOne
    @JoinColumn(name = "school_subscription_id")
    SchoolSubscription schoolSubscription;

    // VNPay transaction information
    @Column(name = "vnp_txn_ref")
    String vnpTxnRef; // mã giao dịch của hệ thống

    @Column(name = "vnp_transaction_no")
    String vnpTransactionNo; // mã giao dịch của VNPay

    @Column(name = "vnp_amount")
    Long vnpAmount; // số tiền thanh toán (VNĐ * 100)

    @Column(name = "vnp_order_info")
    String vnpOrderInfo; // thông tin đơn hàng

    @Column(name = "vnp_response_code")
    String vnpResponseCode; // mã phản hồi (00 = success)

    @Column(name = "vnp_bank_code")
    String vnpBankCode; // mã ngân hàng

    @Column(name = "vnp_card_type")
    String vnpCardType; // loại thẻ (ATM, VISA, MASTERCARD...)

    @Column(name = "vnp_pay_date")
    String vnpPayDate; // thời gian thanh toán (yyyyMMddHHmmss)

    @Enumerated(EnumType.STRING)
    Status status;

    @Column(name = "created_at")
    LocalDateTime createdAt; // thời điểm tạo giao dịch

    @Column(name = "ip_address")
    String ipAddress; // IP của người thực hiện thanh toán

    @Column(name = "notes", columnDefinition = "TEXT")
    String notes; // note về giao dịch
}
