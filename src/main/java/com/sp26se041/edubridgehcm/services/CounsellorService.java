package com.sp26se041.edubridgehcm.services;

import com.sp26se041.edubridgehcm.requests.ChatMessageForChatBot;
import com.sp26se041.edubridgehcm.requests.UpdateConsultationOfflineRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalTime;

public interface CounsellorService {

    ResponseEntity<ResponseObject> chatWithChatbotForSchool(ChatMessageForChatBot messageForChatBot);

    ResponseEntity<ResponseObject> getCounsellorCalendar(LocalDate startDate, LocalDate endDate);

    ResponseEntity<ResponseObject> getConversations(String status, Long cursorId);

    ResponseEntity<ResponseObject> getChatHistory(String parentEmail, String counsellorEmail, int studentProfileId, Long cursorId);

    ResponseEntity<ResponseObject> getConsultationOfflineRequests(String status, int page, int size);

    ResponseEntity<ResponseObject> updateConsultationOfflineRequest(UpdateConsultationOfflineRequest request);

    ResponseEntity<ResponseObject> getCounsellorsInSlot(LocalTime appointmentTime, LocalDate appointmentDate);

    ResponseEntity<ResponseObject> getSlotsOfCampus(LocalDate startDate, LocalDate endDate);

    ResponseEntity<ResponseObject> getAdmissionCampaigns();

    ResponseEntity<ResponseObject> getAppointmentsByCampaign(Integer campaignId);
}
