package com.sp26se041.edubridgehcm.repositories;

import com.sp26se041.edubridgehcm.models.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubjectRepo extends JpaRepository<Subject,Long> {
    Optional<Subject> findByName(String name);
}
