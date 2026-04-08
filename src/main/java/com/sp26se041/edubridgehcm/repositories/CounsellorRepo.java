package com.sp26se041.edubridgehcm.repositories;

import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.models.Counsellor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CounsellorRepo extends JpaRepository<Counsellor, Integer> {

    Optional<Counsellor> findByAccountIdAndCampusId(Integer accountId, Integer campusId);

    Page<Counsellor> findByCampusId(Integer campusId, Pageable pageable);

    Page<Counsellor> findByCampusIdOrderByIdDesc(Integer campusId, Pageable pageable);

    List<Counsellor> findByCampus_IdAndAccount_Status(Integer campusId, Status status);

    long countByCampusId(Integer campusId); // đếm số lượng tư vấn viên của riêng 1 chi nhánh

    long countByCampusSchoolId(Integer schoolId);//đếm tổng của cả hệ thống trường (School)
}
