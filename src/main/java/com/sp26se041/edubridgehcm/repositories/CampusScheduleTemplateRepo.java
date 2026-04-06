package com.sp26se041.edubridgehcm.repositories;

import com.sp26se041.edubridgehcm.models.CampusScheduleTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CampusScheduleTemplateRepo extends JpaRepository<CampusScheduleTemplate, Integer> {

    List<CampusScheduleTemplate> findByCampusIdAndDayOfWeekAndActiveTrue(int campusId, String dayOfWeek);

    List<CampusScheduleTemplate> findByCampusIdAndActiveTrueOrderByStartTimeAsc(Integer campusId);

    Page<CampusScheduleTemplate> findAllByActiveTrue(Pageable pageable);
}
