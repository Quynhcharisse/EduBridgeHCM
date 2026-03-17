package com.sp26se041.edubridgehcm.repositories;

import com.sp26se041.edubridgehcm.models.AdmissionCampaign;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AdmissionCampaignRepo extends JpaRepository<AdmissionCampaign, Integer> {

    // Kiểm tra xem trường đã từng có chiến dịch nào chưa
    boolean existsBySchoolId(int schoolId);

    // Kiểm tra xem năm đó trường đã tạo template chưa
    boolean existsBySchoolIdAndYear(int schoolId, int year);

    List<AdmissionCampaign> findBySchoolIdAndYear(int schoolId, int year);

    Optional<AdmissionCampaign> findFirstBySchoolIdAndYearOrderByIdDesc(int schoolId, int year);

    List<AdmissionCampaign> findBySchoolIdOrderByYearDesc(int schoolId);
}
