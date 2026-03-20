package com.sp26se041.edubridgehcm.repositories;

import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.models.Curriculum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CurriculumRepo extends JpaRepository<Curriculum, Integer> {

    Page<Curriculum> findBySchoolIdOrderByEnrollmentYearDescVersionDesc(Integer schoolId, Pageable pageable);

    List<Curriculum> findAllByGroupCodeAndEnrollmentYearAndIsLatestTrue(String groupCode, int enrollmentYear);

    Curriculum findByGroupCodeAndEnrollmentYearAndCurriculumStatus(String groupCode, int enrollmentYear, Status status);
}
