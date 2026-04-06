package com.sp26se041.edubridgehcm.controllers;

import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.requests.AssignCounsellorIntoSlotsRequest;
import com.sp26se041.edubridgehcm.requests.CampusScheduleTemplateRequest;
import com.sp26se041.edubridgehcm.requests.CreateAccountCounsellorRequest;
import com.sp26se041.edubridgehcm.requests.CreateCampusProgramOfferingRequest;
import com.sp26se041.edubridgehcm.requests.UpdateCampusConfigRequest;
import com.sp26se041.edubridgehcm.requests.UpdateCampusProgramOfferingRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.CampusService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/campus")
@RequiredArgsConstructor
@Tag(name = "Campus")
public class CampusController {

    private final CampusService campusService;

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
    public ResponseEntity<ResponseObject> updateCampusConfig(@PathVariable int campusId, @RequestBody UpdateCampusConfigRequest request) {
        return campusService.updateCampusConfig(campusId, request);
    }

    @GetMapping("/{campusId}/config")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> getCampusConfig(@PathVariable int campusId) {
        return campusService.getCampusConfig(campusId);
    }

    @PostMapping("/schedule/templete")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> upsertCampusScheduleTemplate(@RequestBody CampusScheduleTemplateRequest request) {
        return campusService.upsertCampusScheduleTemplate(request);
    }

    @GetMapping("/{campusId}/schedule/templete/list")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> viewCampusScheduleTemplateByEachCampus(@PathVariable Integer campusId) {
        return campusService.viewCampusScheduleTemplateByEachCampus(campusId);
    }

    @PostMapping("/counsellor/assign")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> assignCounsellorIntoSlots(@RequestBody AssignCounsellorIntoSlotsRequest request) {
        return campusService.syncCounsellorIntoSlots(request);
    }

    @GetMapping("/counsellor/slot/available")
    @PreAuthorize("hasAnyRole('SCHOOL', 'PARENT', 'COUNSELLOR')")
    public ResponseEntity<ResponseObject> getAvailableSlots(LocalDate targetDate, Integer campusId) {
        return campusService.getAvailableSlots(targetDate, campusId);
    }

    @GetMapping("/counsellor/slots/assigned")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> getAssignedSlots(
            @RequestParam Integer campusId,
            @RequestParam(required = false) Integer counsellorId) {
        return campusService.getAssignedSlots(campusId, counsellorId);
    }

    @GetMapping("/counsellor/available/list")
    @PreAuthorize("hasAnyRole('SCHOOL')")
    public ResponseEntity<ResponseObject> getCounsellorAvailableList(Integer campusId) {
        return campusService.getCounsellorAvailableList(campusId);
    }
}
