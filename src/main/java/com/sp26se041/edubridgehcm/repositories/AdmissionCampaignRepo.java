package com.sp26se041.edubridgehcm.repositories;

import com.sp26se041.edubridgehcm.models.AdmissionCampaign;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdmissionCampaignRepo extends JpaRepository<AdmissionCampaign, Integer> {

    List<AdmissionCampaign> findBySchoolId(Integer schoolId);
}

