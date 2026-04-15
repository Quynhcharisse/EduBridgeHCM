package com.sp26se041.edubridgehcm.repositories;

import com.sp26se041.edubridgehcm.models.Campus;
import com.sp26se041.edubridgehcm.models.SchoolHoliday;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SchoolHolidayRepo extends JpaRepository<SchoolHoliday, Integer> {

    List<SchoolHoliday> findAllBySchoolIdAndCampusIdIn(Integer schoolId, List<Integer> campusId);
}
