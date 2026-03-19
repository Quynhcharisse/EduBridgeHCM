package com.sp26se041.edubridgehcm.repositories;

import com.sp26se041.edubridgehcm.models.Curriculum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CurriculumRepo extends JpaRepository<Curriculum, Integer> {

    List<Curriculum> findByGroupCodeAndEnrollmentYearAndIsLatestTrue(String groupCode, int enrollmentYear);

    Page<Curriculum> findBySchoolIdOrderByEnrollmentYearDescVersionDesc(Integer schoolId, Pageable pageable);
}
