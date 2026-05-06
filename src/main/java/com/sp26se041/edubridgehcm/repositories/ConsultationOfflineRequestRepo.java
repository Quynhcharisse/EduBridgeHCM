package com.sp26se041.edubridgehcm.repositories;

import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.models.Campus;
import com.sp26se041.edubridgehcm.models.ConsultationOfflineRequest;
import com.sp26se041.edubridgehcm.models.Counsellor;
import com.sp26se041.edubridgehcm.models.CounsellorSlot;
import com.sp26se041.edubridgehcm.models.Parent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;

public interface ConsultationOfflineRequestRepo extends JpaRepository<ConsultationOfflineRequest, Long> {
    int countByAppointmentDateAndAppointmentTimeAndCampus(LocalDate appointmentDate, LocalTime appointmentTime, Campus campus);

    boolean existsByParentAndCampusAndAppointmentDateAndStatusIn(Parent parent, Campus campus, LocalDate appointmentDate, Collection<Status> statuses);

    Page<ConsultationOfflineRequest> findAllByParentAndStatus(Parent parent, Status status, Pageable pageable);

    Page<ConsultationOfflineRequest> findAllByCampusAndStatus(Campus campus, Status status, Pageable pageable);

    boolean existsByCounsellorSlotAndAppointmentDateAndAppointmentTimeAndStatusIn(CounsellorSlot counsellorSlot, LocalDate appointmentDate, LocalTime appointmentTime, Collection<Status> statuses);

    @Query("""
            SELECT c FROM ConsultationOfflineRequest c
            WHERE c.parent = :parent
            ORDER BY
                CASE 
                    WHEN c.status = 'CONSULTATION_PENDING' THEN 1
                    WHEN c.status = 'CONSULTATION_CONFIRMED' THEN 2
                    WHEN c.status = 'CONSULTATION_IN_PROGRESS' THEN 3
                    WHEN c.status = 'CONSULTATION_COMPLETED' THEN 4
                    WHEN c.status = 'CONSULTATION_CANCELLED' THEN 5
                    WHEN c.status = 'CONSULTATION_NO_SHOW' THEN 6
                    ELSE 7
                END,
            
                CASE 
                    WHEN c.status IN ('CONSULTATION_PENDING', 'CONSULTATION_CONFIRMED')
                    THEN c.appointmentDate
                END ASC,
            
                CASE 
                    WHEN c.status IN ('CONSULTATION_PENDING', 'CONSULTATION_CONFIRMED')
                    THEN c.appointmentTime
                END ASC,
            
                CASE 
                    WHEN c.status NOT IN ('CONSULTATION_PENDING', 'CONSULTATION_CONFIRMED')
                    THEN c.appointmentDate
                END DESC,
            
                CASE 
                    WHEN c.status NOT IN ('CONSULTATION_PENDING', 'CONSULTATION_CONFIRMED')
                    THEN c.appointmentTime
                END DESC
            """)
    Page<ConsultationOfflineRequest> findAllWithTimelineSort(
            @Param("parent") Parent parent,
            Pageable pageable
    );

    // Thống kê tư vấn theo campus + khoảng thời gian
    List<ConsultationOfflineRequest> findByCampusIdAndAppointmentDateBetween(Integer campusId, LocalDate appointmentDateAfter, LocalDate appointmentDateBefore);

    long countByAppointmentDateAndAppointmentTimeAndCampusAndStatusIn(LocalDate appointmentDate, LocalTime appointmentTime, Campus campus, Collection<Status> statuses);

    List<ConsultationOfflineRequest> findByParentIdAndCampusIdAndAppointmentDateBetween(Integer parentId, Integer campusId, LocalDate appointmentDateAfter, LocalDate appointmentDateBefore);

    boolean existsByCounsellorSlotAndAppointmentDateAndAppointmentTimeAndStatusInAndIdNot(CounsellorSlot counsellorSlot, LocalDate appointmentDate, LocalTime appointmentTime, Collection<Status> statuses, Long id);

    List<ConsultationOfflineRequest> findByCounsellorAndAppointmentDateAndAppointmentTime(Counsellor counsellor, LocalDate appointmentDate, LocalTime appointmentTime);

    List<ConsultationOfflineRequest> findByCounsellorSlotId(Integer slotId);

    // Thống kê tư vấn theo nhiều campus (dành cho HQ xem tổng)
    List<ConsultationOfflineRequest> findByCampusIdInAndAppointmentDateBetween(
            List<Integer> campusIds, LocalDate from, LocalDate to);
}
