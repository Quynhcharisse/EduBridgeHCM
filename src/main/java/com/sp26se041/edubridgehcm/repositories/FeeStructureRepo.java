package com.sp26se041.edubridgehcm.repositories;

import com.sp26se041.edubridgehcm.models.FeeStructure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeeStructureRepo extends JpaRepository<FeeStructure, Integer> {
}

