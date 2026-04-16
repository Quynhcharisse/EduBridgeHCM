package com.sp26se041.edubridgehcm.repositories;

import com.sp26se041.edubridgehcm.models.SchoolHoliday;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface SchoolHolidayRepo extends JpaRepository<SchoolHoliday, Integer> {

    List<SchoolHoliday> findAllBySchoolIdAndCampusIdIn(Integer schoolId, List<Integer> campusId);

    boolean existsBySchoolIdAndCampusIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(Integer schoolId, Integer campusId, LocalDate endDate, LocalDate startDate);
}
