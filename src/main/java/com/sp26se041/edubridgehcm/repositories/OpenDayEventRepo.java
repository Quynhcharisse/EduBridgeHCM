package com.sp26se041.edubridgehcm.repositories;

import com.sp26se041.edubridgehcm.models.OpenDayEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;

public interface OpenDayEventRepo extends JpaRepository<OpenDayEvent, Long> {
    Page<OpenDayEvent> findByCampusId(int id, Pageable pageable);
    boolean existsByCampusIdAndEventDateAndStartTimeLessThanAndEndTimeGreaterThan(
            int campusId,
            LocalDate eventDate,
            LocalTime endTime,
            LocalTime startTime
    );
}
