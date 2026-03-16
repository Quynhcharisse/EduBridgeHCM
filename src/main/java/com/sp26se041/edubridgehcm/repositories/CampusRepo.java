package com.sp26se041.edubridgehcm.repositories;

import com.sp26se041.edubridgehcm.models.Campus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CampusRepo extends JpaRepository<Campus, Integer> {

    List<Campus> findBySchoolId(Integer schoolId);

    Optional<Campus> findByIdAndSchoolId(Integer id, Integer schoolId);
}
