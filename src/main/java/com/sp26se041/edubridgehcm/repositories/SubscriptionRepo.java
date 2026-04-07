package com.sp26se041.edubridgehcm.repositories;

import com.sp26se041.edubridgehcm.models.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepo extends JpaRepository<Subscription, Integer> {
}
