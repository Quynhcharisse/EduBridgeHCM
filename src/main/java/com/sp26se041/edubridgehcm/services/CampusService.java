package com.sp26se041.edubridgehcm.services;

import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.requests.AssignCounsellorIntoSlotsRequest;
import com.sp26se041.edubridgehcm.requests.CreateAccountCounsellorRequest;
import com.sp26se041.edubridgehcm.requests.CreateCampusProgramOfferingRequest;
import com.sp26se041.edubridgehcm.requests.CreateCampusScheduleTemplateRequest;
import com.sp26se041.edubridgehcm.requests.UnAssignCounsellorIntoSlotsRequest;
import com.sp26se041.edubridgehcm.requests.UpdateCampusConfigRequest;
import com.sp26se041.edubridgehcm.requests.UpdateCampusProgramOfferingRequest;
import com.sp26se041.edubridgehcm.requests.UpdateCampusScheduleTemplateRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import org.springframework.http.ResponseEntity;

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

    ResponseEntity<ResponseObject> createCampusScheduleTemplate(CreateCampusScheduleTemplateRequest request);

    ResponseEntity<ResponseObject> updateCampusScheduleTemplate(UpdateCampusScheduleTemplateRequest request);

    ResponseEntity<ResponseObject> viewCampusScheduleTemplateList();

    ResponseEntity<ResponseObject> deleteCampusScheduleTemplateList(int id);

    ResponseEntity<ResponseObject> assignCounsellorIntoSlots(AssignCounsellorIntoSlotsRequest request);

    ResponseEntity<ResponseObject> unAssignCounsellorIntoSlots(UnAssignCounsellorIntoSlotsRequest request);

    ResponseEntity<ResponseObject> viewAssignCounsellorIntoSlotList();
}
