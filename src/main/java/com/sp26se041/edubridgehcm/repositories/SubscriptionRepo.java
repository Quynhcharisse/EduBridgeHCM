package com.sp26se041.edubridgehcm.repositories;

import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.models.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface SubscriptionRepo extends JpaRepository<Subscription, Integer> {

    List<Subscription> findAllByPackageStatus(Status status);

    // Tìm các gói có priceApplyDate <= hiện tại và có newPrice
    List<Subscription> findAllByPriceApplyDateLessThanEqual(LocalDate applyDate);
}
