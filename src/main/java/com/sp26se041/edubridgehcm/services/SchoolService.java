package com.sp26se041.edubridgehcm.services;

import com.sp26se041.edubridgehcm.requests.CreateAccountCounsellorRequest;
import com.sp26se041.edubridgehcm.requests.CreateAdmissionCampaignTemplateRequest;
import com.sp26se041.edubridgehcm.requests.CreateCampusProgramOfferingRequest;
import com.sp26se041.edubridgehcm.requests.CreateCampusRequest;
import com.sp26se041.edubridgehcm.requests.UpdateAdmissionCampaignTemplateRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

public interface SchoolService {

    ResponseEntity<ResponseObject> createCampus(CreateCampusRequest request, HttpServletRequest httpServletRequest);

    ResponseEntity<ResponseObject> viewCampusList(HttpServletRequest httpServletRequest);

    ResponseEntity<ResponseObject> createAccountCounsellor(CreateAccountCounsellorRequest request);

    ResponseEntity<ResponseObject> viewAccountCounsellorList(int page, int size);

    ResponseEntity<ResponseObject> createAdmissionCampaignTemplate(CreateAdmissionCampaignTemplateRequest request, HttpServletRequest httpServletRequest);

    ResponseEntity<ResponseObject> updateAdmissionCampaignTemplate(UpdateAdmissionCampaignTemplateRequest request, HttpServletRequest httpServletRequest);

    ResponseEntity<ResponseObject> viewAdmissionCampaignTemplate(int year, HttpServletRequest request);

    ResponseEntity<ResponseObject> createCampusProgramOffering(CreateCampusProgramOfferingRequest request, HttpServletRequest httpServletRequest);

    ResponseEntity<ResponseObject> viewCampusProgramOfferingList(int campusId, HttpServletRequest httpServletRequest);
}
