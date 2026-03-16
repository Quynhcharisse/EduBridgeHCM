package com.sp26se041.edubridgehcm.repositories;

import com.sp26se041.edubridgehcm.models.CampusProgramOffering;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CampusProgramOfferingRepo extends JpaRepository<CampusProgramOffering, Integer> {

    List<CampusProgramOffering> findByCampusId(Integer campusId);

    List<CampusProgramOffering> findByAdmissionCampaignSchoolId(Integer schoolId);
}

