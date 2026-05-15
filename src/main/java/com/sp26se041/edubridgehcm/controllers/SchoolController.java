package com.sp26se041.edubridgehcm.controllers;

import com.sp26se041.edubridgehcm.requests.CreateAdmissionCampaignTemplateRequest;
import com.sp26se041.edubridgehcm.requests.CreateCampusRequest;
import com.sp26se041.edubridgehcm.requests.CreateOpenDayEventRequest;
import com.sp26se041.edubridgehcm.requests.CreateSubscriptionRequest;
import com.sp26se041.edubridgehcm.requests.CurriculumRequest;
import com.sp26se041.edubridgehcm.requests.ProgramRequest;
import com.sp26se041.edubridgehcm.requests.SubscriptionPreviewRequest;
import com.sp26se041.edubridgehcm.requests.UpdateAdmissionCampaignTemplateRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.SchoolService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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
    public ResponseEntity<ResponseObject> cloneAdmissionCampaign(@PathVariable int id, @RequestParam int targetYear) {
        return schoolService.cloneAdmissionCampaign(id, targetYear);
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
    public ResponseEntity<ResponseObject> cancelAdmissionCampaign(@PathVariable int id, @RequestParam String reason) {
        return schoolService.cancelAdmissionCampaign(id, reason);
    }

    @GetMapping("/{year}/campaign/template")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> viewAdmissionCampaignTemplate(@PathVariable int year) {
        return schoolService.viewAdmissionCampaignTemplate(year);
    }

    @GetMapping("/{schoolId}/campaign/template/public")
    public ResponseEntity<ResponseObject> viewAdmissionCampaignTemplatePublic(@PathVariable int schoolId,
                                                                              @RequestParam(defaultValue = "0") int year) {
        return schoolService.viewAdmissionCampaignTemplatePublic(schoolId, year);
    }

    @PostMapping("/curriculum")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> upsertCurriculum(@RequestBody CurriculumRequest request) {
        return schoolService.upsertCurriculum(request);
    }

    @GetMapping("/parents")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> getParentsInFavouriteSchool() {
        return schoolService.getParentsInFavouriteSchool();
    }

    @GetMapping("/templates/national")
    public ResponseEntity<ResponseObject> getNationalTemplate() {
        return schoolService.getNationalCurriculumTemplate();
    }

    @PostMapping(value = "/extract/excel/international", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseObject> extractSubjectsFromExcel(@RequestParam("file") MultipartFile file) {
        return schoolService.extractSubjectsFromExcel(file);
    }

    @PostMapping(value = "/extract/excel/program-subjects", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> extractProgramSubjectsFromExcel(@RequestParam("file") MultipartFile file) {
        return schoolService.extractProgramSubjectsFromExcel(file);
    }

    @PatchMapping("/{id}/activate/curriculum")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> handleCurriculumAction(@PathVariable int id, @RequestParam(name = "action") String action) {
        return schoolService.handleCurriculumAction(id, action);
    }

    @GetMapping("/curriculum/list")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> viewCurriculumList(@RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "10") int pageSize) {
        return schoolService.viewCurriculumList(page, pageSize);
    }

    @PatchMapping("/{id}/activate/program")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> handleProgramAction(@PathVariable int id, @RequestParam(name = "action") String action) {
        return schoolService.handleProgramAction(id, action);
    }

    @PostMapping("/{id}/program/clone")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> cloneProgram(@PathVariable int id) {
        return schoolService.cloneProgram(id);
    }

    @GetMapping("/program/list")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> viewProgramList(@RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "10") int pageSize) {
        return schoolService.viewProgramList(page, pageSize);
    }

    @GetMapping("/public/language-options")
    public ResponseEntity<ResponseObject> getLanguageSubject() {
        return schoolService.getLanguageSubject();
    }

    @PostMapping("/program")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> upsertProgram(@RequestBody ProgramRequest request) {
        return schoolService.upsertProgram(request);
    }

    @GetMapping("/public/list")
    public ResponseEntity<ResponseObject> viewSchoolList() {
        return schoolService.viewSchoolList();
    }

    @GetMapping("/{schoolId}/public/detail")
    public ResponseEntity<ResponseObject> viewSchoolDetail(@PathVariable int schoolId) {
        return schoolService.viewSchoolDetail(schoolId);
    }

    @GetMapping("/campus/search/nearby")
    public ResponseEntity<ResponseObject> searchNearby(@RequestParam Double lat, @RequestParam Double lng, @RequestParam(defaultValue = "10") Double radius) {
        return schoolService.searchNearby(lat, lng, radius);
    }

    // Open day event
    @PostMapping("/event")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> createOpenDayEvent(@RequestBody CreateOpenDayEventRequest request) {
        return schoolService.createOpenDayEvent(request);
    }

    @GetMapping("/event")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> viewOpenDayEventList(@RequestParam(defaultValue = "0") int page,
                                                               @RequestParam(defaultValue = "10") int pageSize) {
        return schoolService.viewOpenDayEventList(page, pageSize);
    }

    @GetMapping("/campus/schedule/template/list")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> viewCampusScheduleTemplateListBySchool(@RequestParam(defaultValue = "0") int page,
                                                                                 @RequestParam(defaultValue = "10") int pageSize) {
        return schoolService.viewCampusScheduleTemplateListBySchool(page, pageSize);
    }

    @GetMapping("/campus/list/export")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<Resource> exportCampusList() throws IOException {
        return schoolService.exportCampusList();
    }

    @GetMapping("/admission/campaign/list/export")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<Resource> exportAdmissionCampaign(int year) throws IOException {
        return schoolService.exportAdmissionCampaign(year);
    }

    @PostMapping("/subscription")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> createSchoolSubscription(@RequestBody CreateSubscriptionRequest request, HttpServletRequest httpRequest) {
        return schoolService.createSchoolSubscription(request, httpRequest);
    }

    @PostMapping("/subscription/preview")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> previewSubscription(@RequestBody SubscriptionPreviewRequest request) {
        return schoolService.previewSubscription(request);
    }

    @GetMapping("/vnpay-callback")
    public ResponseEntity<ResponseObject> handleVNPayCallback(HttpServletRequest request) {
        return schoolService.handleVNPayCallback(request);
    }

    @GetMapping("/payment/receipt")
    public ResponseEntity<ResponseObject> viewPaymentReceipt(@RequestParam String txnRef) {
        return schoolService.viewPaymentReceipt(txnRef);
    }

    @GetMapping("/subscription/receipt/export")
    public ResponseEntity<byte[]> exportPaymentReceipt(@RequestParam String txnRef) {
        return schoolService.exportPaymentReceipt(txnRef);
    }

    @GetMapping("/current/subscription")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> viewCurrentSubscription() {
        return schoolService.viewCurrentSubscription();
    }

    @GetMapping("/admission/form/export")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<Resource> exportAdmissionForms() throws IOException {
        return schoolService.exportAdmissionForms();
    }
}
