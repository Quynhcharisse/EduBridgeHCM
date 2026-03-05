package com.sp26se041.edubridgehcm.repositories;

import com.sp26se041.edubridgehcm.models.School;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SchoolRepo extends JpaRepository<School, Integer> {
}
