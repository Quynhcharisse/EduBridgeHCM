package com.sp26se041.edubridgehcm.services;

import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.requests.AssignCounsellorIntoSlotsRequest;
import com.sp26se041.edubridgehcm.requests.CampusScheduleTemplateRequest;
import com.sp26se041.edubridgehcm.requests.CreateAccountCounsellorRequest;
import com.sp26se041.edubridgehcm.requests.CreateCampusProgramOfferingRequest;
import com.sp26se041.edubridgehcm.requests.UpdateCampusConfigRequest;
import com.sp26se041.edubridgehcm.requests.UpdateCampusProgramOfferingRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

public interface CampusService {

    ResponseEntity<ResponseObject> createCampusProgramOffering(CreateCampusProgramOfferingRequest request);

    ResponseEntity<ResponseObject> viewCampusProgramOfferingList(int campusId, int page, int pageSize);

    ResponseEntity<ResponseObject> updateCampusProgramOffering(UpdateCampusProgramOfferingRequest request);

    ResponseEntity<ResponseObject> closeCampusProgramOffering(int offeringId);

    ResponseEntity<ResponseObject> changeCampusProgramOfferingStatus(int offeringId, Status targetStatus);

    ResponseEntity<ResponseObject> createAccountCounsellor(CreateAccountCounsellorRequest request);

    ResponseEntity<ResponseObject> viewAccountCounsellorList(int page, int size);

    ResponseEntity<ResponseObject> updateCampusConfig(int campusId, UpdateCampusConfigRequest request);

    ResponseEntity<ResponseObject> getCampusConfig(int campusId);

    ResponseEntity<ResponseObject> upsertCampusScheduleTemplate(CampusScheduleTemplateRequest request);

    ResponseEntity<ResponseObject> viewCampusScheduleTemplateByEachCampus(Integer campusId);

    ResponseEntity<ResponseObject> syncCounsellorIntoSlots(AssignCounsellorIntoSlotsRequest request);

    ResponseEntity<ResponseObject> getAvailableSlots(LocalDate targetDate, Integer campusId);

    ResponseEntity<ResponseObject> getAssignedSlots(Integer campusId, Integer counsellorId);

    ResponseEntity<ResponseObject> getCounsellorAvailableList(Integer campusId);
}
