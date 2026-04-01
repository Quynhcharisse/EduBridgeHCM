package com.sp26se041.edubridgehcm.repositories;

import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.models.AdmissionCampaign;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AdmissionCampaignRepo extends JpaRepository<AdmissionCampaign, Integer> {

    // Kiểm tra xem trường đã từng có chiến dịch nào chưa
    boolean existsBySchoolId(int schoolId);

    boolean existsByYearAndSchoolIdAndStatusIn(int year, int schoolId, List<Status> statuses);

    Optional<AdmissionCampaign> findBySchoolIdAndYearAndIdNot(int schoolId, int year, int id);

    Optional<AdmissionCampaign> findFirstBySchoolIdAndYearOrderByIdDesc(int schoolId, int year);

    List<AdmissionCampaign> findBySchoolIdOrderByYearDesc(int schoolId);
}
