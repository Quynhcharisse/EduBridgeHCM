package com.sp26se041.edubridgehcm.controllers;

import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.requests.CreateAccountCounsellorRequest;
import com.sp26se041.edubridgehcm.requests.CreateAdmissionCampaignTemplateRequest;
import com.sp26se041.edubridgehcm.requests.CreateCampusProgramOfferingRequest;
import com.sp26se041.edubridgehcm.requests.CreateCampusRequest;
import com.sp26se041.edubridgehcm.requests.CreateOpenDayEventRequest;
import com.sp26se041.edubridgehcm.requests.CurriculumRequest;
import com.sp26se041.edubridgehcm.requests.ProgramRequest;
import com.sp26se041.edubridgehcm.requests.UpdateAdmissionCampaignTemplateRequest;
import com.sp26se041.edubridgehcm.requests.UpdateCampusConfigRequest;
import com.sp26se041.edubridgehcm.requests.UpdateCampusProgramOfferingRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.SchoolService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/school")
@RequiredArgsConstructor
@Tag(name = "School")
public class SchoolController {

    private final SchoolService schoolService;

    @PostMapping("/campus")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> createCampus(@RequestBody CreateCampusRequest request) {
        return schoolService.createCampus(request);
    }

    @GetMapping("/campus/list")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> viewCampusList(@RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "10") int pageSize) {
        return schoolService.viewCampusList(page, pageSize);
    }

    @PostMapping("/campaign/template")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> createAdmissionCampaignTemplate(@RequestBody CreateAdmissionCampaignTemplateRequest request) {
        return schoolService.createAdmissionCampaignTemplate(request);
    }

    @PostMapping("{id}/campaign/template/clone")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> cloneAdmissionCampaign(@PathVariable int id) {
        return schoolService.cloneAdmissionCampaign(id);
    }

    @PutMapping("/campaign/template")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> updateAdmissionCampaignTemplate(@RequestBody UpdateAdmissionCampaignTemplateRequest request) {
        return schoolService.updateAdmissionCampaignTemplate(request);
    }

    @PutMapping("{id}/campaign/template/status")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> publishAdmissionCampaignStatus(@PathVariable int id) {
        return schoolService.publishAdmissionCampaignStatus(id);
    }

    @PutMapping("{id}/campaign/template/cancel")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> cancelAdmissionCampaign(@PathVariable int id, String reason) {
        return schoolService.cancelAdmissionCampaign(id, reason);
    }

    @GetMapping("{year}/campaign/template")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> viewAdmissionCampaignTemplate(@PathVariable int year) {
        return schoolService.viewAdmissionCampaignTemplate(year);
    }

    @PostMapping("/curriculum")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> upsertCurriculum(@RequestBody CurriculumRequest request) {
        return schoolService.upsertCurriculum(request);
    }

    @PatchMapping("/{id}/activate/curriculum")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> activate(@PathVariable int id) {
        return schoolService.activateCurriculum(id);
    }

    @GetMapping("/curriculum/list")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> viewCurriculumList(@RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "10") int pageSize) {
        return schoolService.viewCurriculumList(page, pageSize);
    }

    @GetMapping("/program/list")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> viewProgramList(@RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "10") int pageSize) {
        return schoolService.viewProgramList(page, pageSize);
    }

    @PostMapping("/program")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> upsertProgram(@RequestBody ProgramRequest request) {
        return schoolService.upsertProgram(request);
    }

    @PostMapping("/campus/offering")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> createCampusProgramOffering(@RequestBody CreateCampusProgramOfferingRequest request) {
        return schoolService.createCampusProgramOffering(request);
    }

    @GetMapping("{campusId}/campus/offering/list")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> viewCampusProgramOfferingList(@PathVariable int campusId,
                                                                        @RequestParam(defaultValue = "0") int page,
                                                                        @RequestParam(defaultValue = "10") int pageSize) {
        return schoolService.viewCampusProgramOfferingList(campusId, page, pageSize);
    }

    @PutMapping("/campus/offering/list")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> updateCampusProgramOffering(@RequestBody UpdateCampusProgramOfferingRequest request) {
        return schoolService.updateCampusProgramOffering(request);
    }

    @PutMapping("/{offeringId}/campus/offering/status")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> changeCampusProgramOfferingStatus(@PathVariable int offeringId, @RequestParam Status targetStatus) {
        return schoolService.changeCampusProgramOfferingStatus(offeringId, targetStatus);
    }

    @PutMapping("/{offeringId}/campus/offering/close")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> closeCampusProgramOffering(@PathVariable int offeringId) {
        return schoolService.closeCampusProgramOffering(offeringId);
    }

    @PostMapping("/counsellor")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> createAccountCounsellor(@RequestBody CreateAccountCounsellorRequest request) {
        return schoolService.createAccountCounsellor(request);
    }

    @GetMapping("/counsellor/list")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> viewAccountCounsellorList(@RequestParam(defaultValue = "0") int page,
                                                                    @RequestParam(defaultValue = "10") int pageSize) {
        return schoolService.viewAccountCounsellorList(page, pageSize);
    }

    @GetMapping("/public/list")
    public ResponseEntity<ResponseObject> viewSchoolList() {
        return schoolService.viewSchoolList();
    }

    @GetMapping("/{schoolId}/public/detail")
    public ResponseEntity<ResponseObject> viewSchoolDetail(@PathVariable int schoolId) {
        return schoolService.viewSchoolDetail(schoolId);
    }

    // Open day event
    @PostMapping("/event")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> createOpenDayEvent(@RequestBody CreateOpenDayEventRequest request) {
        return schoolService.createOpenDayEvent(request);
    }

    @GetMapping("/event")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> viewOpenDayEventList(@RequestParam int page, @RequestParam int pageSize) {
        return schoolService.viewOpenDayEventList(page, pageSize);
    }

    @PutMapping("/campus/{campusId}/facility")
    public ResponseEntity<ResponseObject> updateCampusConfig(@PathVariable int campusId, @RequestBody UpdateCampusConfigRequest request) {
        return schoolService.updateCampusConfig(campusId, request);
    }
}
