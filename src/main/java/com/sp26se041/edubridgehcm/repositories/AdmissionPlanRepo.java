package com.sp26se041.edubridgehcm.repositories;

import com.sp26se041.edubridgehcm.models.AdmissionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdmissionPlanRepo extends JpaRepository<AdmissionPlan, Integer> {
}

