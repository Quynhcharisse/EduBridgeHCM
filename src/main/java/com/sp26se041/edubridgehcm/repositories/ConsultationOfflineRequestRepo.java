package com.sp26se041.edubridgehcm.repositories;

import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.models.Campus;
import com.sp26se041.edubridgehcm.models.ConsultationOfflineRequest;
import com.sp26se041.edubridgehcm.models.CounsellorSlot;
import com.sp26se041.edubridgehcm.models.Parent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ConsultationOfflineRequestRepo extends JpaRepository<ConsultationOfflineRequest, Long> {
    int countByAppointmentDateAndAppointmentTimeAndCampus(LocalDate appointmentDate, LocalTime appointmentTime, Campus campus);

    boolean existsByParentAndCampusAndAppointmentDateAndStatusIn(Parent parent, Campus campus, LocalDate appointmentDate, Collection<Status> statuses);

    Page<ConsultationOfflineRequest> findAllByParentAndStatus(Parent parent, Status status, Pageable pageable);

    Optional<ConsultationOfflineRequest> findAllByCampusIdAndAppointmentDateAndAppointmentTimeOrderByCreatedDateDesc(Integer campusId, LocalDate appointmentDate, LocalTime appointmentTime);

    Page<ConsultationOfflineRequest> findAllByCampusAndStatus(Campus campus, Status status, Pageable pageable);

    boolean existsByCounsellorSlotAndAppointmentDateAndAppointmentTimeAndStatusIn(CounsellorSlot counsellorSlot, LocalDate appointmentDate, LocalTime appointmentTime, Collection<Status> statuses);
}
