package com.sp26se041.edubridgehcm.repositories;

import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.models.AdmissionCampaign;
import com.sp26se041.edubridgehcm.models.AdmissionReservationForm;
import com.sp26se041.edubridgehcm.models.Campus;
import com.sp26se041.edubridgehcm.models.CampusProgramOffering;
import com.sp26se041.edubridgehcm.models.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface AdmissionReservationFormRepo extends JpaRepository<AdmissionReservationForm, Integer> {

    // ── Offering-level queries (phase 2) ──────────────────────────────────────
    int countByCampusProgramOfferingIdAndStatusIn(Integer offeringId, Collection<Status> statuses);

    boolean existsByCampusProgramOfferingAndStudentProfileAndStatus(
            CampusProgramOffering campusProgramOffering, StudentProfile studentProfile, Status status);

    List<AdmissionReservationForm> findByCampusProgramOffering_IdInAndStudentProfileAndStatusIn(
            Collection<Integer> campusProgramOfferingIds,
            StudentProfile studentProfile,
            Collection<Status> statuses
    );

    // ── Campaign-level queries (phase 1 + school actor) ───────────────────────
    int countByAdmissionCampaignIdAndStatusIn(Integer campaignId, Collection<Status> statuses);

    boolean existsByAdmissionCampaignAndStudentProfileAndStatusIn(
            AdmissionCampaign admissionCampaign, StudentProfile studentProfile, Collection<Status> statuses);

    // School actor: xem tất cả hồ sơ theo school (qua campaign)
    List<AdmissionReservationForm> findByAdmissionCampaign_School_IdAndStatus(int schoolId, Status status);

    List<AdmissionReservationForm> findByAdmissionCampaign_School_Id(int schoolId);

    // ── Scheduler queries ─────────────────────────────────────────────────────
    List<AdmissionReservationForm> findByStatusIn(Collection<Status> statuses);

    // ── Parent-level queries ──────────────────────────────────────────────────
    List<AdmissionReservationForm> findByStudentProfile_Parent_Account_IdAndStatus(
            Integer parentAccountId, Status status);

    List<AdmissionReservationForm> findByStudentProfile_Parent_Account_Id(Integer parentAccountId);
}

