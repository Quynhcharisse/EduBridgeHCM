package com.sp26se041.edubridgehcm.repositories;

import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.models.AdmissionReservationForm;
import com.sp26se041.edubridgehcm.models.Campus;
import com.sp26se041.edubridgehcm.models.CampusProgramOffering;
import com.sp26se041.edubridgehcm.models.StudentProfile;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface AdmissionReservationFormRepo extends JpaRepository<AdmissionReservationForm, Integer> {
    int countByCampusProgramOfferingIdAndStatusIn(Integer offeringId, Collection<Status> activeStatuses);

    int countByCampusProgramOfferingIdAndStatusIsNull(Integer offeringId);

    int countByCampusProgramOffering_AdmissionCampaign_IdAndStatusIn(int campaignId, Collection<Status> activeStatuses);

    int countByCampusProgramOffering_AdmissionCampaign_IdAndStatusIsNull(int campaignId);

    boolean existsByCampusProgramOfferingAndStudentProfileAndStatus(CampusProgramOffering campusProgramOffering, StudentProfile studentProfile, Status status);


    List<AdmissionReservationForm> findByStudentProfile_Parent_Account_IdAndStatus(Integer studentProfileParentAccountId, Status status);

    List<AdmissionReservationForm> findByStudentProfile_Parent_Account_Id(Integer studentProfileParentAccountId);

    List<AdmissionReservationForm> findByCampusProgramOffering_CampusAndStatus(Campus campusProgramOfferingCampus, Status status);

    List<AdmissionReservationForm> findByCampusProgramOffering_Campus(Campus campusProgramOfferingCampus);
}

