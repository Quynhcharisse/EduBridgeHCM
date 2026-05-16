package com.sp26se041.edubridgehcm.repositories;

import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.models.AdmissionCampaign;
import com.sp26se041.edubridgehcm.models.AdmissionReservationForm;
import com.sp26se041.edubridgehcm.models.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AdmissionReservationFormRepo extends JpaRepository<AdmissionReservationForm, Integer> {

    int countByCampusProgramOfferingIdAndStatusIn(Integer offeringId, Collection<Status> statuses);

    int countByAdmissionCampaignIdAndStatusIn(Integer campaignId, Collection<Status> statuses);

    boolean existsByAdmissionCampaignAndStudentProfileAndStatusIn(
            AdmissionCampaign admissionCampaign, StudentProfile studentProfile, Collection<Status> statuses);

    List<AdmissionReservationForm> findByAdmissionCampaign_School_IdAndStatus(int schoolId, Status status);

    List<AdmissionReservationForm> findByAdmissionCampaign_School_Id(int schoolId);

    List<AdmissionReservationForm> findByStatusIn(Collection<Status> statuses);

    List<AdmissionReservationForm> findByStudentProfile_Parent_Account_IdAndStatus(
            Integer parentAccountId, Status status);

    List<AdmissionReservationForm> findByStudentProfile_Parent_Account_Id(Integer parentAccountId);


    boolean existsByStudentProfile_StudentCodeAndStatusAndAdmissionCampaign_Year(String studentProfileStudentCode, Status status, int admissionCampaignYear);

    List<AdmissionReservationForm> findByStudentProfileAndStatus(StudentProfile studentProfile, Status status);

    boolean existsByStudentProfile_Parent_IdAndType(Integer studentProfileParentId, String type);

    Optional<AdmissionReservationForm> findFirstByStudentProfile_Parent_IdAndType(Integer studentProfileParentId, String type);
}

