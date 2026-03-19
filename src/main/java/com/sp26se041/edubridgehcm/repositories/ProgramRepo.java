package com.sp26se041.edubridgehcm.repositories;

import com.sp26se041.edubridgehcm.models.Program;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgramRepo extends JpaRepository<Program, Integer> {

    int countByCurriculumId(Integer id);

    int countOfferingsById(Integer programId);

    Page<Program> findByCurriculum_School_Id(int schoolId, Pageable pageable);
}

