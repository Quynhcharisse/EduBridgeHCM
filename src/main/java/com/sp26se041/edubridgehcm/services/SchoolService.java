package com.sp26se041.edubridgehcm.services;

import com.sp26se041.edubridgehcm.requests.CreateAdmissionCampaignTemplateRequest;
import com.sp26se041.edubridgehcm.requests.CreateCampusRequest;
import com.sp26se041.edubridgehcm.requests.CreateOpenDayEventRequest;
import com.sp26se041.edubridgehcm.requests.CreateSubscriptionRequest;
import com.sp26se041.edubridgehcm.requests.CurriculumRequest;
import com.sp26se041.edubridgehcm.requests.ProgramRequest;
import com.sp26se041.edubridgehcm.requests.UpdateAdmissionCampaignTemplateRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

public interface SchoolService {

    ResponseEntity<ResponseObject> createCampus(CreateCampusRequest request);

    ResponseEntity<ResponseObject> viewCampusList(int page, int pageSize);

    ResponseEntity<ResponseObject> createAdmissionCampaignTemplate(CreateAdmissionCampaignTemplateRequest request);

    ResponseEntity<ResponseObject> cloneAdmissionCampaign(int id);

    ResponseEntity<ResponseObject> updateAdmissionCampaignTemplate(UpdateAdmissionCampaignTemplateRequest request);

    ResponseEntity<ResponseObject> publishAdmissionCampaignStatus(int id);

    ResponseEntity<ResponseObject> cancelAdmissionCampaign(int id, String reason);

    ResponseEntity<ResponseObject> viewAdmissionCampaignTemplate(int year);

    ResponseEntity<ResponseObject> upsertCurriculum(CurriculumRequest request);

    ResponseEntity<ResponseObject> handleCurriculumAction(int id, String action);

    ResponseEntity<ResponseObject> viewCurriculumList(int page, int pageSize);

    ResponseEntity<ResponseObject> viewProgramList(int page, int pageSize);

    ResponseEntity<ResponseObject> upsertProgram(ProgramRequest request);

    ResponseEntity<ResponseObject> cloneProgram(int id);

    ResponseEntity<ResponseObject> handleProgramAction(int id, String action);

    ResponseEntity<ResponseObject> viewSchoolList();

    ResponseEntity<ResponseObject> viewSchoolDetail(int schoolId);

    ResponseEntity<ResponseObject> createOpenDayEvent(CreateOpenDayEventRequest request);

    ResponseEntity<ResponseObject> viewOpenDayEventList(int currentPage, int pageSize);

    ResponseEntity<ResponseObject> viewCampusScheduleTemplateListBySchool(int page, int pageSize);

    ResponseEntity<Resource> exportCampusList() throws IOException;

    ResponseEntity<Resource> exportAdmissionCampaign(int year) throws IOException;

    ResponseEntity<ResponseObject> viewCurrentSubscription();

    ResponseEntity<ResponseObject> createSubscription(CreateSubscriptionRequest request, HttpServletRequest httpRequest);

    ResponseEntity<ResponseObject> handleVNPayCallback(HttpServletRequest request);

    ResponseEntity<ResponseObject> searchNearby(Double lat, Double lng, Double radius);
}
