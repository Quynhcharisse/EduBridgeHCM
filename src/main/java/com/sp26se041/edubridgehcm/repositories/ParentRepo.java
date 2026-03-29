package com.sp26se041.edubridgehcm.repositories;

import com.sp26se041.edubridgehcm.models.Parent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParentRepo extends JpaRepository<Parent, Integer> {
    Optional<Parent> findByAccount_Email(String accountEmail);
}
