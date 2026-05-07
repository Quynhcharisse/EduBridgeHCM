package com.sp26se041.edubridgehcm.utils;

import com.sp26se041.edubridgehcm.models.ConsultationOfflineRequest;
import com.sp26se041.edubridgehcm.repositories.ConsultationOfflineRequestRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class ConsultationLockCleanupWorker {

    private final ConsultationOfflineRequestRepo consultationOfflineRequestRepo;

    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.MINUTES)
    @Transactional
    public void cleanExpiredLocks() {

        LocalDateTime now = LocalDateTime.now();

        List<ConsultationOfflineRequest> expired =
                consultationOfflineRequestRepo
                        .findByLockedUntilIsNotNullAndLockedUntilBefore(
                                now
                        );

        if (expired.isEmpty()) return;

        expired.forEach(r -> {
            r.setCounsellor(null);
            r.setLockedUntil(null);
        });

        consultationOfflineRequestRepo.saveAll(expired);

    }
}
