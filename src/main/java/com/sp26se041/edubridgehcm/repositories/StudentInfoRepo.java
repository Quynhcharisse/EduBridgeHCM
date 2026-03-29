package com.sp26se041.edubridgehcm.repositories;

import com.sp26se041.edubridgehcm.models.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;


public interface StudentInfoRepo extends JpaRepository<StudentProfile, Integer> {
    boolean existsByStudentNameAndParent_Account_Email(String studentName, String parentAccountEmail);

    boolean existsByStudentNameIgnoreCaseAndParent_Account_Email(String studentName, String parentAccountEmail);
}
