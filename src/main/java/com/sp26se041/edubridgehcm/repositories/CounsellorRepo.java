package com.sp26se041.edubridgehcm.repositories;

import com.sp26se041.edubridgehcm.models.Counsellor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CounsellorRepo extends JpaRepository<Counsellor, Integer> {
}
