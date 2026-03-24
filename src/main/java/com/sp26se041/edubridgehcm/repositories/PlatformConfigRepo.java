package com.sp26se041.edubridgehcm.repositories;

import com.sp26se041.edubridgehcm.models.PlatformConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlatformConfigRepo extends JpaRepository<PlatformConfig, Integer> {
    Optional<PlatformConfig> findByKey(String order);
}
