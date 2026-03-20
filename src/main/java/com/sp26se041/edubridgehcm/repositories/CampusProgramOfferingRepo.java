package com.sp26se041.edubridgehcm.repositories;

import com.sp26se041.edubridgehcm.enums.LearningMode;
import com.sp26se041.edubridgehcm.models.CampusProgramOffering;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampusProgramOfferingRepo extends JpaRepository<CampusProgramOffering, Integer> {

    Page<CampusProgramOffering> findByCampusIdOrderByIdDesc(Integer campusId, Pageable pageable);

    Page<CampusProgramOffering> findByAdmissionCampaignSchoolIdOrderByIdDesc(Integer schoolId, Pageable pageable);

    boolean existsByAdmissionCampaignIdAndCampusIdAndProgramIdAndLearningMode(Integer admissionCampaignId, Integer campusId, Integer programId, LearningMode learningMode);

    boolean existsByAdmissionCampaignIdAndCampusIdAndProgramIdAndLearningModeAndIdNot(Integer admissionCampaignId, Integer campusId, Integer programId, LearningMode learningMode, Integer id);
}

