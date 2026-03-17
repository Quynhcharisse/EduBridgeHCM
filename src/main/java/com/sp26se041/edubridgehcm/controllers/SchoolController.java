package com.sp26se041.edubridgehcm.controllers;

import com.sp26se041.edubridgehcm.requests.CreateAccountCounsellorRequest;
import com.sp26se041.edubridgehcm.requests.CreateAdmissionCampaignTemplateRequest;
import com.sp26se041.edubridgehcm.requests.CreateCampusProgramOfferingRequest;
import com.sp26se041.edubridgehcm.requests.CreateCampusRequest;
import com.sp26se041.edubridgehcm.requests.UpdateAdmissionCampaignTemplateRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.SchoolService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/school")
@RequiredArgsConstructor
@Tag(name = "School")
public class SchoolController {

    private final SchoolService schoolService;

    @PostMapping("/campus")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> createCampus(@RequestBody CreateCampusRequest request, HttpServletRequest httpServletRequest) {
        return schoolService.createCampus(request, httpServletRequest);
    }

    @GetMapping("/campus/list")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> viewCampusList(HttpServletRequest httpServletRequest) {
        return schoolService.viewCampusList(httpServletRequest);
    }

    @PostMapping("/campaign/template")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> createAdmissionCampaignTemplate(@RequestBody CreateAdmissionCampaignTemplateRequest request, HttpServletRequest httpServletRequest) {
        return schoolService.createAdmissionCampaignTemplate(request, httpServletRequest);
    }

    @PutMapping("/campaign/template")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> updateAdmissionCampaignTemplate(@RequestBody UpdateAdmissionCampaignTemplateRequest request, HttpServletRequest httpServletRequest) {
        return schoolService.updateAdmissionCampaignTemplate(request, httpServletRequest);
    }

    @GetMapping("{year}/campaign/template")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> viewAdmissionCampaignTemplate(@PathVariable int year, HttpServletRequest request) {
        return schoolService.viewAdmissionCampaignTemplate(year, request);
    }

    @PostMapping("/campaign/offering")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> createCampusProgramOffering(@RequestBody CreateCampusProgramOfferingRequest request, HttpServletRequest httpServletRequest) {
        return schoolService.createCampusProgramOffering(request, httpServletRequest);
    }

    @GetMapping("{campusId}/campaign/offering/list")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> viewCampusProgramOfferingList(@PathVariable int campusId, HttpServletRequest httpServletRequest) {
        return schoolService.viewCampusProgramOfferingList(campusId, httpServletRequest);
    }

    @PostMapping("/counsellor")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> createAccountCounsellor(@RequestBody CreateAccountCounsellorRequest request) {
        return schoolService.createAccountCounsellor(request);
    }

    @GetMapping("/counsellor/list")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> viewAccountCounsellorList() {
        return schoolService.viewAccountCounsellorList();
    }
}
