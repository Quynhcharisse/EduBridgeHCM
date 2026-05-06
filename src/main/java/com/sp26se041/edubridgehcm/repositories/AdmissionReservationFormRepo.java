package com.sp26se041.edubridgehcm.repositories;

import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.models.AdmissionReservationForm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface AdmissionReservationFormRepo extends JpaRepository<AdmissionReservationForm, Integer> {
    int countByCampusProgramOfferingIdAndStatusIn(Integer offeringId, Collection<Status> activeStatuses);

    int countByCampusProgramOfferingIdAndStatusIsNull(Integer offeringId);

    int countByCampusProgramOffering_AdmissionCampaign_IdAndStatusIn(int campaignId, Collection<Status> activeStatuses);

    int countByCampusProgramOffering_AdmissionCampaign_IdAndStatusIsNull(int campaignId);

    List<AdmissionReservationForm> findByCampusProgramOffering_AdmissionCampaign_IdAndStatusIn(int campaignId, Collection<Status> statuses);

    List<AdmissionReservationForm> findByCampusProgramOffering_AdmissionCampaign_IdAndStatusIsNull(int campaignId);
}

