package com.sp26se041.edubridgehcm.repositories;


import com.sp26se041.edubridgehcm.models.PersonalityType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PersonalityTypeRepo extends JpaRepository<PersonalityType, Long> {
    boolean existsByCode(String code);
}
