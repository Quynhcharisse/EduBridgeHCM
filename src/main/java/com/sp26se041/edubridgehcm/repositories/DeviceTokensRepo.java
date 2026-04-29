package com.sp26se041.edubridgehcm.repositories;

import com.sp26se041.edubridgehcm.models.DeviceTokens;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DeviceTokensRepo extends JpaRepository<DeviceTokens, Integer> {

    Optional<DeviceTokens> findByToken(String token);

    Optional<DeviceTokens> findByTokenAndIsActiveTrue(String token);

    List<DeviceTokens> findAllByUserIdAndIsActiveTrue(Integer userId);

    List<DeviceTokens> findAllByUserIdInAndIsActiveTrue(List<Integer> userIds);
}
