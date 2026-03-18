package com.sp26se041.edubridgehcm.repositories;

import com.sp26se041.edubridgehcm.models.OpenDayEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OpenDayEventRepo extends JpaRepository<OpenDayEvent, Long> {
    Page<OpenDayEvent> findByCampusId(int id, Pageable pageable);
}
