package com.sp26se041.edubridgehcm.controllers;

import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.requests.AssignCounsellorIntoSlotsRequest;
import com.sp26se041.edubridgehcm.requests.CampusScheduleTemplateRequest;
import com.sp26se041.edubridgehcm.requests.CreateAccountCounsellorRequest;
import com.sp26se041.edubridgehcm.requests.CreateCampusProgramOfferingRequest;
import com.sp26se041.edubridgehcm.requests.CreateConversationRequest;
import com.sp26se041.edubridgehcm.requests.UpdateCampusConfigRequest;
import com.sp26se041.edubridgehcm.requests.UpdateCampusProgramOfferingRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.CampusService;
import com.sp26se041.edubridgehcm.services.WebSocketService;
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
    public ResponseEntity<ResponseObject> viewCampusProgramOfferingList(@PathVariable int campusId,
                                                                        @RequestParam(defaultValue = "0") int page,
                                                                        @RequestParam(defaultValue = "10") int pageSize) {
        return campusService.viewCampusProgramOfferingList(campusId, page, pageSize);
    }

    @PutMapping("/offering/list")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> updateCampusProgramOffering(@RequestBody UpdateCampusProgramOfferingRequest request) {
        return campusService.updateCampusProgramOffering(request);
    }

    @PutMapping("/{offeringId}/offering/status")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> changeCampusProgramOfferingStatus(@PathVariable int offeringId, @RequestParam Status targetStatus) {
        return campusService.changeCampusProgramOfferingStatus(offeringId, targetStatus);
    }

    @PutMapping("/{offeringId}/offering/close")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> closeCampusProgramOffering(@PathVariable int offeringId) {
        return campusService.closeCampusProgramOffering(offeringId);
    }

    @PostMapping("/counsellor")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> createAccountCounsellor(@RequestBody CreateAccountCounsellorRequest request) {
        return campusService.createAccountCounsellor(request);
    }

    @GetMapping("/counsellor/list")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> viewAccountCounsellorList(@RequestParam(defaultValue = "0") int page,
                                                                    @RequestParam(defaultValue = "10") int pageSize) {
        return campusService.viewAccountCounsellorList(page, pageSize);
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
    public ResponseEntity<ResponseObject> getAvailableSlots(LocalDate targetDate) {
        return campusService.getAvailableSlots(targetDate);
    }

    @GetMapping("/counsellor/slots/assigned")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> getAssignedSlots(
            @RequestParam Integer campusId,
            @RequestParam(required = false) Integer counsellorId) {
        return campusService.getAssignedSlots(counsellorId);
    }

    @GetMapping("/counsellor/available/list")
    @PreAuthorize("hasAnyRole('SCHOOL')")
    public ResponseEntity<ResponseObject> getCounsellorAvailableList() {
        return campusService.getCounsellorAvailableList();
    }

    @GetMapping("/counsellor/list/export")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<Resource> exportCounsellorList() throws IOException {
        return campusService.exportCounsellorList();
    }

    @GetMapping("/schedule/template/list/export")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<Resource> exportCampusScheduleMatrix() throws IOException {
        return campusService.exportCampusScheduleMatrix();
    }

    @GetMapping("/document")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> getDocuments(){
        return campusService.getDocuments();
    }

    @GetMapping("/message/history/admin")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> getHistoryChatWithAdmin(@RequestParam(required = false) Long cursorId){
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

    @PutMapping("/messages/read/{conversationId}")
    public ResponseEntity<ResponseObject> readMessages(@PathVariable Long conversationId) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        return webSocketService.markConversationAsRead(conversationId, email);
    }
}
