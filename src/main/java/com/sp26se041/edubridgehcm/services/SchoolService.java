package com.sp26se041.edubridgehcm.services;

import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.requests.CreateAccountCounsellorRequest;
import com.sp26se041.edubridgehcm.requests.CreateAdmissionCampaignTemplateRequest;
import com.sp26se041.edubridgehcm.requests.CreateCampusProgramOfferingRequest;
import com.sp26se041.edubridgehcm.requests.CreateCampusRequest;
import com.sp26se041.edubridgehcm.requests.UpdateAdmissionCampaignTemplateRequest;
import com.sp26se041.edubridgehcm.requests.UpdateCampusProgramOfferingRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import org.springframework.http.ResponseEntity;

public interface SchoolService {

    ResponseEntity<ResponseObject> createCampus(CreateCampusRequest request);

    ResponseEntity<ResponseObject> viewCampusList();

    ResponseEntity<ResponseObject> createAccountCounsellor(CreateAccountCounsellorRequest request);

    ResponseEntity<ResponseObject> viewAccountCounsellorList(int page, int size);

    ResponseEntity<ResponseObject> createAdmissionCampaignTemplate(CreateAdmissionCampaignTemplateRequest request);

    ResponseEntity<ResponseObject> updateAdmissionCampaignTemplate(UpdateAdmissionCampaignTemplateRequest request);

    ResponseEntity<ResponseObject> changeAdmissionCampaignStatus(Integer id, Status targetStatus);

    ResponseEntity<ResponseObject> viewAdmissionCampaignTemplate(int year);

    ResponseEntity<ResponseObject> createCampusProgramOffering(CreateCampusProgramOfferingRequest request);

    ResponseEntity<ResponseObject> viewCampusProgramOfferingList(int campusId);

    ResponseEntity<ResponseObject> updateCampusProgramOffering(UpdateCampusProgramOfferingRequest request);

}
