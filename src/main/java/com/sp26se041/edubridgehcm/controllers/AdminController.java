package com.sp26se041.edubridgehcm.controllers;

import com.sp26se041.edubridgehcm.requests.AddSubjectRequest;
import com.sp26se041.edubridgehcm.requests.CreatePersonalityTypeRequest;
import com.sp26se041.edubridgehcm.requests.UpdateStatusServicePackageFeeRequest;
import com.sp26se041.edubridgehcm.requests.UpsertServicePackageFeeRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.AdminService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin")
public class AdminController {

    private final AdminService adminService;

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

    @PutMapping("status/service/package/fee")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseObject> updateStatusServicePackageFee(@RequestBody UpdateStatusServicePackageFeeRequest request) {
        return adminService.updateStatusServicePackageFee(request);
    }

    @GetMapping("/service/package/fee/list")
    @PreAuthorize("hasRole('ADMIN')")
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

}
