package com.sp26se041.edubridgehcm.repositories;

import com.sp26se041.edubridgehcm.models.SchoolRegistrationRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SchoolRegistrationRequestRepo extends JpaRepository<SchoolRegistrationRequest, Integer> {

    List<SchoolRegistrationRequest> findAllByOrderByCreatedAtDesc();
}
