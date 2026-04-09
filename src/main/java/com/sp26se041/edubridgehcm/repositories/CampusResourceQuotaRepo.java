package com.sp26se041.edubridgehcm.repositories;

import com.sp26se041.edubridgehcm.enums.ResourceType;
import com.sp26se041.edubridgehcm.models.CampusResourceQuota;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CampusResourceQuotaRepo extends JpaRepository<CampusResourceQuota, Integer> {

    Optional<CampusResourceQuota> findByCampusIdAndResourceType(int campusId, ResourceType resourceType);
}
