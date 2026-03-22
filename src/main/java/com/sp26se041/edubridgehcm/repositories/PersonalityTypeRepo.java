package com.sp26se041.edubridgehcm.repositories;


import com.sp26se041.edubridgehcm.models.PersonalityType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PersonalityTypeRepo extends JpaRepository<PersonalityType, Long> {
    boolean existsByCode(String code);

    Optional<PersonalityType> findByCode(String code);
}
