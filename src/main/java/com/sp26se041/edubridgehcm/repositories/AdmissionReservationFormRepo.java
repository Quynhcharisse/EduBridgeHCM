package com.sp26se041.edubridgehcm.repositories;

import com.sp26se041.edubridgehcm.models.AdmissionReservationForm;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdmissionReservationFormRepo extends JpaRepository<AdmissionReservationForm, Integer> {

    int countByCampusProgramOfferingId(Integer campusProgramOfferingId);

    int countByCampusProgramOffering_AdmissionCampaign_Id(int id);
}

