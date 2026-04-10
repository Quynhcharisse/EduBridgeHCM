package com.sp26se041.edubridgehcm.repositories;

import com.sp26se041.edubridgehcm.models.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface PaymentTransactionRepo extends JpaRepository<PaymentTransaction, Integer> {
    Optional<PaymentTransaction> findByVnpTxnRef(String txnRef);
}

