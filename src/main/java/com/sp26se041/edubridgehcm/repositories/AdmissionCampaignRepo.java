package com.sp26se041.edubridgehcm.repositories;

import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.models.AdmissionCampaign;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AdmissionCampaignRepo extends JpaRepository<AdmissionCampaign, Integer> {

    boolean existsByYearAndSchoolIdAndStatusIn(int year, int schoolId, List<Status> statuses);

    List<AdmissionCampaign> findBySchoolIdAndYearOrderByStatusAsc(int schoolId, int year);

    List<AdmissionCampaign> findBySchoolIdOrderByYearDesc(int schoolId);

    List<AdmissionCampaign> findAllBySchoolIdAndYearAndIdNot(Integer schoolId, int year, int id);

    boolean existsBySchoolIdAndYearAndStatus(int id, int year, Status status);

    List<AdmissionCampaign> findBySchoolIdAndYear(int schoolId, int year);

    Optional<AdmissionCampaign> findBySchoolIdAndStatus(Integer schoolId, Status status);

    Optional<AdmissionCampaign> findBySchoolIdAndYearAndStatus(Integer schoolId, int year, Status status);
}
