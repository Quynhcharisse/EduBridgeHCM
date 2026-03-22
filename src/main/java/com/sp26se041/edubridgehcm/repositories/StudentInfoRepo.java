package com.sp26se041.edubridgehcm.repositories;

import com.sp26se041.edubridgehcm.models.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentInfoRepo extends JpaRepository<StudentProfile, Long> {
}
