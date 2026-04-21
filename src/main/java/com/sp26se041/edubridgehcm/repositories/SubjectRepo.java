package com.sp26se041.edubridgehcm.repositories;

import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.enums.SubjectType;
import com.sp26se041.edubridgehcm.models.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SubjectRepo extends JpaRepository<Subject,Long> {

    Optional<Subject> findByName(String name);
    boolean existsByName(String name);

    List<Subject> findAllByTypeIn(Collection<SubjectType> types);
}
