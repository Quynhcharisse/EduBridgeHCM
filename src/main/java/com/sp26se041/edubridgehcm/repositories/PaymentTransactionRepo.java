package com.sp26se041.edubridgehcm.repositories;

import com.sp26se041.edubridgehcm.models.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface PaymentTransactionRepo extends JpaRepository<PaymentTransaction, Integer>, JpaSpecificationExecutor<PaymentTransaction> {

    Optional<PaymentTransaction> findByVnpTxnRef(String txnRef);

    boolean existsByVnpTxnRef(String txnRef);

    List<PaymentTransaction> findAllByOrderByCreatedAtAsc();
}

