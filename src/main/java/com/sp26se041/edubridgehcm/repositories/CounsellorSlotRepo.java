package com.sp26se041.edubridgehcm.repositories;

import com.sp26se041.edubridgehcm.models.CounsellorSlot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CounsellorSlotRepo extends JpaRepository<CounsellorSlot, Integer> {

    boolean existsByCampusScheduleTemplateId(int templateId);
}
