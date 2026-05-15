package com.sp26se041.edubridgehcm.repositories;

import com.sp26se041.edubridgehcm.models.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Arrays;
import java.util.List;


public interface StudentInfoRepo extends JpaRepository<StudentProfile, Integer> {
    boolean existsByStudentNameIgnoreCaseAndParent_Account_Email(String studentName, String parentAccountEmail);

    boolean existsByStudentNameIgnoreCaseAndParent_Account_EmailAndIdNot(String studentName, String parentAccountEmail, Integer id);

    List<StudentProfile> findByParent_Account_Email(String parentAccountEmail);

}
