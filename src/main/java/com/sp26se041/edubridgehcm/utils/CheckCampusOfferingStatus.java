package com.sp26se041.edubridgehcm.utils;

import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.models.CampusProgramOffering;
import com.sp26se041.edubridgehcm.repositories.CampusProgramOfferingRepo;

import java.time.LocalDate;

public class CheckCampusOfferingStatus {

    public static CampusProgramOffering checkOfferingStatus(CampusProgramOffering offering, CampusProgramOfferingRepo campusProgramOfferingRepo) {

        LocalDate today = LocalDate.now();

        boolean condition = !today.isBefore(offering.getOpenDate()) && !today.isAfter(offering.getCloseDate());

        offering.setStatus(condition ? Status.OFFERING_ACTIVE : Status.OFFERING_INACTIVE);
        return campusProgramOfferingRepo.save(offering);
    }
}
