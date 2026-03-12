package com.sp26se041.edubridgehcm.repositories;

import com.sp26se041.edubridgehcm.models.AdmissionCampaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface AdmissionPlanRepo extends JpaRepository<AdmissionCampaign, Integer> {
}

