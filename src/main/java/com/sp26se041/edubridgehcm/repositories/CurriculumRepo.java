package com.sp26se041.edubridgehcm.repositories;

import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.models.Curriculum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CurriculumRepo extends JpaRepository<Curriculum, Integer> {

    Page<Curriculum> findBySchoolIdOrderByApplicationYearDesc(Integer schoolId, Pageable pageable);

    Curriculum findByGroupCodeAndApplicationYearAndCurriculumStatus(String groupCode, int enrollmentYear, Status status);

    boolean existsByGroupCodeAndCurriculumStatusAndIdNot(String targetGroupCode, Status status, int i);

    boolean existsByParentIdAndCurriculumStatus(Integer parentId, Status status);
}
