package com.sp26se041.edubridgehcm.controllers;

import com.sp26se041.edubridgehcm.models.ChatMessage;
import com.sp26se041.edubridgehcm.requests.AddSubjectRequest;
import com.sp26se041.edubridgehcm.requests.CreatePersonalityTypeRequest;
import com.sp26se041.edubridgehcm.requests.UpsertServicePackageFeeRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.AdminService;
import com.sp26se041.edubridgehcm.services.WebSocketService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin")
public class AdminController {

    private final AdminService adminService;
    private final WebSocketService webSocketService;

    @PostMapping("/school/registrations/verify")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseObject> verifyRegistration(@RequestParam(name = "requestId") int requestId) {
        return adminService.verifyRegistration(requestId);
    }

    @GetMapping("/school/registrations/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseObject> viewSchoolRegistrationList() {
        return adminService.viewSchoolRegistrationList();
    }

    @PostMapping("/service/package/fee")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseObject> upsertServicePackageFee(@RequestBody UpsertServicePackageFeeRequest request) {
        return adminService.upsertServicePackageFee(request);
    }

    @PutMapping("/{packageId}/service/package/fee/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseObject> publishServicePackageFee(@PathVariable Integer packageId) {
        return adminService.publishServicePackageFee(packageId);
    }

    @PutMapping("/{packageId}/service/package/fee/deactive")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseObject> deActiveServicePackageFee(@PathVariable Integer packageId) {
        return adminService.deActiveServicePackageFee(packageId);
    }

    @GetMapping("/service/package/fee/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'SCHOOL')")
    public ResponseEntity<ResponseObject> viewServicePackageFeeList() {
        return adminService.viewServicePackageFeeList();
    }

    @PostMapping("/personality/type")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseObject> createPersonalityType(@RequestBody CreatePersonalityTypeRequest request) {
        return adminService.createPersonalityType(request);
    }

    @GetMapping("/personality/type")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseObject> viewPersonalityTypes() {
        return adminService.getPersonalityTypeList();
    }

    @PostMapping("/subject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseObject> createSubject(AddSubjectRequest request) {
        return adminService.createSubject(request);
    }

    @GetMapping("/subject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseObject> getAllSubjects() {
        return adminService.getAllSubjects();
    }

    @PostMapping(value = "/{categoryTemplate}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseObject> uploadFile(@PathVariable String categoryTemplate, @RequestParam("file") MultipartFile file) {
        return adminService.uploadTemplateDocxTemplate(file, categoryTemplate);
    }

    @GetMapping("/{categoryTemplate}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseObject> getFiles(@PathVariable String categoryTemplate) {
        return adminService.getTemplateDocs(categoryTemplate);
    }

    @DeleteMapping("/{templateId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseObject> deleteTemplate(@PathVariable long templateId) {
        return adminService.removeTemplateDocx(templateId);
    }

    @GetMapping("/conversation")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseObject> getConversations(@RequestParam (required = false) Long cursorId){
        return adminService.getConversations(cursorId);
    }


    @GetMapping("/message/history/{campusId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseObject> getHistoryChatWithAdmin(@PathVariable int campusId,@RequestParam(required = false) Long cursorId){
        return adminService.getChatHistory(campusId, cursorId);
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
