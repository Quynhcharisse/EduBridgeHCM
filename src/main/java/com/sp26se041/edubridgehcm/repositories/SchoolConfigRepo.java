package com.sp26se041.edubridgehcm.repositories;

import com.sp26se041.edubridgehcm.models.SchoolConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SchoolConfigRepo extends JpaRepository<SchoolConfig, Integer> {

    Optional<SchoolConfig> findBySchoolIdAndKey(int schoolId, String key);
}

