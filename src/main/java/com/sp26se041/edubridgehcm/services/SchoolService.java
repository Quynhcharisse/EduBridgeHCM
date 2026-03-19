package com.sp26se041.edubridgehcm.services;

import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.requests.CreateAccountCounsellorRequest;
import com.sp26se041.edubridgehcm.requests.CreateAdmissionCampaignTemplateRequest;
import com.sp26se041.edubridgehcm.requests.CreateCampusProgramOfferingRequest;
import com.sp26se041.edubridgehcm.requests.CreateCampusRequest;
import com.sp26se041.edubridgehcm.requests.CreateOpenDayEventRequest;
import com.sp26se041.edubridgehcm.requests.CreateProgramRequest;
import com.sp26se041.edubridgehcm.requests.UpdateAdmissionCampaignTemplateRequest;
import com.sp26se041.edubridgehcm.requests.UpdateCampusProgramOfferingRequest;
import com.sp26se041.edubridgehcm.requests.CurriculumRequest;
import com.sp26se041.edubridgehcm.requests.UpdateProgramRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import org.springframework.http.ResponseEntity;

public interface SchoolService {

    ResponseEntity<ResponseObject> createCampus(CreateCampusRequest request);

    ResponseEntity<ResponseObject> viewCampusList(int page, int pageSize);

    ResponseEntity<ResponseObject> createAccountCounsellor(CreateAccountCounsellorRequest request);

    ResponseEntity<ResponseObject> viewAccountCounsellorList(int page, int size);

    ResponseEntity<ResponseObject> createAdmissionCampaignTemplate(CreateAdmissionCampaignTemplateRequest request);

    ResponseEntity<ResponseObject> updateAdmissionCampaignTemplate(UpdateAdmissionCampaignTemplateRequest request);

    ResponseEntity<ResponseObject> changeAdmissionCampaignStatus(Integer id, Status targetStatus);

    ResponseEntity<ResponseObject> viewAdmissionCampaignTemplate(int year);

    ResponseEntity<ResponseObject> upsertCurriculum(CurriculumRequest request);

    ResponseEntity<ResponseObject> viewCurriculumList(int page, int pageSize);

    ResponseEntity<ResponseObject> createProgram(CreateProgramRequest request);

    ResponseEntity<ResponseObject> viewProgramList();

    ResponseEntity<ResponseObject> updateProgram(UpdateProgramRequest request);

    ResponseEntity<ResponseObject> createCampusProgramOffering(CreateCampusProgramOfferingRequest request);

    ResponseEntity<ResponseObject> viewCampusProgramOfferingList(int campusId, int page, int pageSize);

    ResponseEntity<ResponseObject> updateCampusProgramOffering(UpdateCampusProgramOfferingRequest request);

    ResponseEntity<ResponseObject> createOpenDayEvent(CreateOpenDayEventRequest request);

    ResponseEntity<ResponseObject> viewOpenDayEventList(int currentPage, int pageSize);
}
