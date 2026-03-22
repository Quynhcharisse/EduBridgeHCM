package com.sp26se041.edubridgehcm.repositories;

import com.sp26se041.edubridgehcm.models.Major;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MajorRepo extends JpaRepository<Major, Long> {
    Optional<Major> findByName(String name);
}
