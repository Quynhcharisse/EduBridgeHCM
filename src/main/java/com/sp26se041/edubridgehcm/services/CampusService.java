package com.sp26se041.edubridgehcm.services;

import com.sp26se041.edubridgehcm.enums.OfferingProgramAction;

import com.sp26se041.edubridgehcm.requests.AssignCounsellorIntoSlotsRequest;
import com.sp26se041.edubridgehcm.requests.CampusScheduleTemplateRequest;
import com.sp26se041.edubridgehcm.requests.ChatMessageForChatBot;
import com.sp26se041.edubridgehcm.requests.CreateAccountCounsellorRequest;
import com.sp26se041.edubridgehcm.requests.CreateCampusProgramOfferingRequest;
import com.sp26se041.edubridgehcm.requests.ProcessApplicantRequest;
import com.sp26se041.edubridgehcm.requests.UpdateCampusConfigRequest;
import com.sp26se041.edubridgehcm.requests.UpdateCampusProgramOfferingRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.time.LocalDate;

public interface CampusService {

    ResponseEntity<ResponseObject> getConsultationStats(String period, LocalDate from, LocalDate to);

    ResponseEntity<ResponseObject> createCampusProgramOffering(CreateCampusProgramOfferingRequest request);

    ResponseEntity<ResponseObject> viewCampusProgramOfferingList(Integer campusId, int page, int pageSize);

    ResponseEntity<ResponseObject> getOfferingQuotaSummary(int campaignId);

    ResponseEntity<ResponseObject> getOfferingQuotaBreakdown(int campaignId);

    ResponseEntity<ResponseObject> updateCampusProgramOffering(UpdateCampusProgramOfferingRequest request);

    ResponseEntity<ResponseObject> changeCampusProgramOfferingStatus(int offeringId, OfferingProgramAction action);

    ResponseEntity<ResponseObject> createAccountCounsellor(CreateAccountCounsellorRequest request);

    ResponseEntity<ResponseObject> viewAccountCounsellorList(int page, int size);

    ResponseEntity<ResponseObject> getQuotaRequestSummary();

    ResponseEntity<ResponseObject> updateCampusConfig(UpdateCampusConfigRequest request);

    ResponseEntity<ResponseObject> getCampusConfig();

    ResponseEntity<ResponseObject> upsertCampusScheduleTemplate(CampusScheduleTemplateRequest request);

    ResponseEntity<ResponseObject> viewCampusScheduleTemplateByEachCampus();

    ResponseEntity<ResponseObject> syncCounsellorIntoSlots(AssignCounsellorIntoSlotsRequest request);

    ResponseEntity<ResponseObject> getAvailableSlots(LocalDate targetDate, Integer campaignId);

    ResponseEntity<ResponseObject> getAssignedSlots(Integer counsellorId);

    ResponseEntity<ResponseObject> getCounsellorAvailableList();

    ResponseEntity<Resource> exportCounsellorList() throws IOException;

    ResponseEntity<Resource> exportCampusScheduleMatrix() throws IOException;

    ResponseEntity<ResponseObject> getDocuments();

    ResponseEntity<ResponseObject> getChatHistoryWithAdmin(Long cursorId);

    ResponseEntity<ResponseObject> createConversationWithAdmin();

    ResponseEntity<ResponseObject> getConversation();

    ResponseEntity<ResponseObject> chatWithChatbotForSchool(ChatMessageForChatBot messageForChatBot);

    ResponseEntity<ResponseObject> processApplicant(ProcessApplicantRequest request);

    ResponseEntity<ResponseObject> getAdmissionReservationForms(String status);

}
