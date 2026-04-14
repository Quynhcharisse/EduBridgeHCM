package com.sp26se041.edubridgehcm.repositories;

import com.sp26se041.edubridgehcm.models.Program;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgramRepo extends JpaRepository<Program, Integer> {

    int countByCurriculumId(Integer id);

    int countOfferingsById(Integer programId);

    Page<Program> findByCurriculum_School_Id(int schoolId, Pageable pageable);

    boolean existsByCurriculumId(int id);

    Program findByIdAndCurriculum_School_Id(Integer id, Integer schoolId);

    boolean existsByCurriculum_IdAndGraduationStandardIgnoreCase(Integer curriculumId, String graduationStandard);

    boolean existsByCurriculum_IdAndGraduationStandardIgnoreCaseAndIdNot(Integer curriculumId, String graduationStandard, Integer id);

    boolean existsByCurriculum_IdAndNameIgnoreCaseAndIdNot(Integer curriculumId, String name, Integer programId);

    boolean existsByCurriculum_IdAndNameIgnoreCase(Integer curriculumId, String name);
}

