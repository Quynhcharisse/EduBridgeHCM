package com.sp26se041.edubridgehcm.controllers;

import com.sp26se041.edubridgehcm.enums.OfferingProgramAction;
import com.sp26se041.edubridgehcm.requests.AssignCounsellorIntoSlotsRequest;
import com.sp26se041.edubridgehcm.requests.CampusScheduleTemplateRequest;
import com.sp26se041.edubridgehcm.requests.ChatMessageForChatBot;
import com.sp26se041.edubridgehcm.requests.ConfirmPaymentDepositRequest;
import com.sp26se041.edubridgehcm.requests.CreateAccountCounsellorRequest;
import com.sp26se041.edubridgehcm.requests.CreateCampusProgramOfferingRequest;
import com.sp26se041.edubridgehcm.requests.ProcessApplicantRequest;
import com.sp26se041.edubridgehcm.requests.UpdateAdmissionReservationFormsRequest;
import com.sp26se041.edubridgehcm.requests.UpdateCampusConfigRequest;
import com.sp26se041.edubridgehcm.requests.UpdateCampusProgramOfferingRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.CampusService;
import com.sp26se041.edubridgehcm.services.WebSocketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/campus")
@RequiredArgsConstructor
@Tag(name = "Campus")
public class CampusController {

    private final CampusService campusService;

    private final WebSocketService webSocketService;

    @PostMapping("/offering")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> createCampusProgramOffering(@RequestBody CreateCampusProgramOfferingRequest request) {
        return campusService.createCampusProgramOffering(request);
    }

    @GetMapping("{campusId}/offering/list")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> viewCampusProgramOfferingList(@PathVariable int campusId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int pageSize) {
        return campusService.viewCampusProgramOfferingList(campusId, page, pageSize);
    }

    @GetMapping("/offering/quota-summary")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> getOfferingQuotaSummary(@RequestParam int campaignId) {
        return campusService.getOfferingQuotaSummary(campaignId);
    }

    @GetMapping("/offering/quota-breakdown")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> getOfferingQuotaBreakdown(@RequestParam int campaignId) {
        return campusService.getOfferingQuotaBreakdown(campaignId);
    }

    @PutMapping("/offering")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> updateCampusProgramOffering(@RequestBody UpdateCampusProgramOfferingRequest request) {
        return campusService.updateCampusProgramOffering(request);
    }

    @PutMapping("/{offeringId}/offering/status")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> changeCampusProgramOfferingStatus(@PathVariable int offeringId, @RequestParam OfferingProgramAction action) {
        return campusService.changeCampusProgramOfferingStatus(offeringId, action);
    }

    @PostMapping("/counsellor")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> createAccountCounsellor(@RequestBody CreateAccountCounsellorRequest request) {
        return campusService.createAccountCounsellor(request);
    }

    @GetMapping("/counsellor/list")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> viewAccountCounsellorList(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int pageSize) {
        return campusService.viewAccountCounsellorList(page, pageSize);
    }

    @GetMapping("/quota/request/summary/emailjs")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> getQuotaRequestSummary() {
        return campusService.getQuotaRequestSummary();
    }

    @PutMapping("/{campusId}/config")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> updateCampusConfig(@RequestBody UpdateCampusConfigRequest request) {
        return campusService.updateCampusConfig(request);
    }

    @GetMapping("/config")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> getCampusConfig() {
        return campusService.getCampusConfig();
    }

    @PostMapping("/schedule/templete")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> upsertCampusScheduleTemplate(@RequestBody CampusScheduleTemplateRequest request) {
        return campusService.upsertCampusScheduleTemplate(request);
    }

    @GetMapping("/{campusId}/schedule/templete/list")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> viewCampusScheduleTemplateByEachCampus() {
        return campusService.viewCampusScheduleTemplateByEachCampus();
    }

    @PostMapping("/counsellor/assign")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> assignCounsellorIntoSlots(@RequestBody AssignCounsellorIntoSlotsRequest request) {
        return campusService.syncCounsellorIntoSlots(request);
    }

    @GetMapping("/counsellor/slot/available")
    public ResponseEntity<ResponseObject> getAvailableSlots(@RequestParam LocalDate targetDate, @RequestParam(required = false) Integer campaignId) {
        return campusService.getAvailableSlots(targetDate, campaignId);
    }

    @GetMapping("/counsellor/slots/assigned")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> getAssignedSlots(@RequestParam(required = false) Integer counsellorId) {
        return campusService.getAssignedSlots(counsellorId);
    }

    @GetMapping("/counsellor/available/list")
    @PreAuthorize("hasAnyRole('SCHOOL')")
    public ResponseEntity<ResponseObject> getCounsellorAvailableList() {
        return campusService.getCounsellorAvailableList();
    }

    @Operation(summary = "Xuất danh sách tư vấn viên ra file Excel")
    @ApiResponse(responseCode = "200", content = @Content(mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
    @GetMapping(value = "/counsellor/list/export", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<Resource> exportCounsellorList() throws IOException {
        return campusService.exportCounsellorList();
    }

    @Operation(summary = "Xuất thời khoá biểu mẫu ra file Excel")
    @ApiResponse(responseCode = "200", content = @Content(mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
    @GetMapping(value = "/schedule/template/list/export", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<Resource> exportCampusScheduleMatrix() throws IOException {
        return campusService.exportCampusScheduleMatrix();
    }

    @GetMapping("/document")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> getDocuments() {
        return campusService.getDocuments();
    }

    @GetMapping("/message/history/admin")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> getHistoryChatWithAdmin(@RequestParam(required = false) Long cursorId) {
        return campusService.getChatHistoryWithAdmin(cursorId);
    }

    @PostMapping("/conversation")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> createConversationWithAdmin() {
        return campusService.createConversationWithAdmin();
    }

    @GetMapping("/conversation")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> getConversationWithAdmin() {
        return campusService.getConversation();
    }

    @GetMapping("/consultation/stats")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> getConsultationStats(@RequestParam(defaultValue = "THIS_MONTH") String period, @RequestParam(required = false) LocalDate from, @RequestParam(required = false) LocalDate to) {
        return campusService.getConsultationStats(period, from, to);
    }

    @PutMapping("/messages/read/{conversationId}")
    public ResponseEntity<ResponseObject> readMessages(@PathVariable Long conversationId) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        return webSocketService.markConversationAsRead(conversationId, email);
    }

    @PostMapping("/chat-with-AI-chatbot")
    @PreAuthorize("hasAnyRole('SCHOOL')")
    public ResponseEntity<ResponseObject> chatWithAIChatBotSchool(@RequestBody ChatMessageForChatBot chatMessageForChatBot) {
        return campusService.chatWithChatbotForSchool(chatMessageForChatBot);
    }

    @PutMapping("/process/admission/reservation/form")
    @PreAuthorize("hasAnyRole('SCHOOL')")
    public ResponseEntity<ResponseObject> processApplicant(@RequestBody ProcessApplicantRequest request) {
        return campusService.processApplicant(request);
    }

    @PutMapping("/confirm/admission/reservation/payment")
    @PreAuthorize("hasAnyRole('SCHOOL')")
    public ResponseEntity<ResponseObject> confirmPaymentDeposit(@RequestBody ConfirmPaymentDepositRequest request) {
        return campusService.confirmPaymentDeposit(request);
    }

    @GetMapping("/admission/reservation/form")
    @PreAuthorize("hasAnyRole('SCHOOL')")
    public ResponseEntity<ResponseObject> getAdmissionReservationForm(@RequestParam(required = false) String status) {
        return campusService.getAdmissionReservationForms(status);
    }

    @PutMapping("/approve/auto/admission/reservation/form")
    @PreAuthorize("hasAnyRole('SCHOOL')")
    public ResponseEntity<ResponseObject> approveAutoAdmissionReservationForms(@RequestParam Integer admissionCampaignId) {
        return campusService.approveAutoAdmissionReservationForms(admissionCampaignId);
    }

    @PutMapping("/admission/reservation/forms")
    @PreAuthorize("hasAnyRole('SCHOOL')")
    public ResponseEntity<ResponseObject> updateAdmissionReservationForms(@RequestBody UpdateAdmissionReservationFormsRequest request) {
        return campusService.updateAdmissionReservationForms(request);
    }

    @GetMapping("/admission/campaign")
    @PreAuthorize("hasAnyRole('SCHOOL')")
    public ResponseEntity<ResponseObject> getAdmissionCampaigns() {
        return campusService.getAdmissionCampaigns();
    }

    @GetMapping("/admission/form/export")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<Resource> exportAdmissionForms(
            @RequestParam(required = false) List<String> status,
            @RequestParam(required = false) Long campaignId) throws IOException {
        return campusService.exportAdmissionForms(status, campaignId);
    }

    @GetMapping("/book/consultation/offline")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> getConsultationOfflineRequests(@RequestParam String status ,@RequestParam int page, @RequestParam int pageSize){
        return campusService.getConsultationOfflineRequests(status, page, pageSize);
    }

}
