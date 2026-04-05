package com.sp26se041.edubridgehcm.repositories;

import com.sp26se041.edubridgehcm.models.CampusScheduleTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalTime;

public interface CampusScheduleTemplateRepo extends JpaRepository<CampusScheduleTemplate, Integer> {

    boolean existsByCampusIdAndDayOfWeekAndStartTimeAndEndTimeAndIdNot(int campusId, String dayOfWeek, LocalTime startTime, LocalTime endTime, int template);
}
