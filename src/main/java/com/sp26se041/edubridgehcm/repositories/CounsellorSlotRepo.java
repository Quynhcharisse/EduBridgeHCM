package com.sp26se041.edubridgehcm.repositories;

import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.models.CounsellorSlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface CounsellorSlotRepo extends JpaRepository<CounsellorSlot, Integer> {

    //lấy các Slot đang hoạt động
    List<CounsellorSlot> findByStartDateLessThanEqualAndEndDateGreaterThanEqualAndCampusScheduleTemplate_DayOfWeekAndCampusScheduleTemplate_Campus_IdAndCampusScheduleTemplate_ActiveTrue(
            LocalDate dateForStart,
            LocalDate dateForEnd,
            String dayOfWeek,
            Integer campusId
    );

    // Tìm các slot theo trạng thái trong khoảng ngày của một Campus cụ thể (hoặc Global)
    List<CounsellorSlot> findByStatusAndCounsellorCampusSchoolIdAndCounsellorCampusIdAndStartDateBetween(
            Status status, Integer schoolId, Integer campusId, LocalDate start, LocalDate end);

    // Nếu campusId là null (Global)
    List<CounsellorSlot> findByStatusAndCounsellorCampusSchoolIdAndStartDateBetween(
            Status status, Integer schoolId, LocalDate start, LocalDate end);

    List<CounsellorSlot> findByCampusScheduleTemplate_Campus_IdAndCounsellor_Id(Integer campusId, Integer counsellorId);

    List<CounsellorSlot> findByCampusScheduleTemplate_Campus_Id(Integer campusId);

    List<CounsellorSlot> findByCampusScheduleTemplate_Id(Integer templateId);

    // Trường hợp 1: Dành cho Holiday của riêng 1 Campus
    long countByCounsellorCampusSchoolIdAndCounsellorCampusIdAndStartDateBetweenAndStatusIn(
            Integer schoolId,
            Integer campusId,
            LocalDate start,
            LocalDate end,
            List<Status> statuses
    );

    // Trường hợp 2: Dành cho Holiday toàn trường (Global)
    long countByCounsellorCampusSchoolIdAndStartDateBetweenAndStatusIn(
            Integer schoolId,
            LocalDate start,
            LocalDate end,
            List<Status> statuses
    );

    List<CounsellorSlot> findByCounsellorIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(Integer counsellorId, LocalDate startDateIsLessThan, LocalDate endDateIsGreaterThan);
}
