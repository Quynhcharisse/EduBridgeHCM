package com.sp26se041.edubridgehcm.repositories;

import com.sp26se041.edubridgehcm.models.PaymentTransaction;
import com.sp26se041.edubridgehcm.models.School;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentTransactionRepo extends JpaRepository<PaymentTransaction, Integer> {

    // Tìm transaction theo mã giao dịch VNPay
    Optional<PaymentTransaction> findByVnpTxnRef(String vnpTxnRef);

    // Lấy tất cả giao dịch của một trường
    List<PaymentTransaction> findBySchoolOrderByCreatedAtDesc(School school);

    // Lấy tất cả giao dịch theo status
    List<PaymentTransaction> findByStatus(String status);
}

