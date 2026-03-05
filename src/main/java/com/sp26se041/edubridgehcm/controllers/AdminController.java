package com.sp26se041.edubridgehcm.controllers;

import com.sp26se041.edubridgehcm.configurations.SkipRestrictedCheck;
import com.sp26se041.edubridgehcm.requests.ProcessRegistrationRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.AdminService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
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

    @PostMapping("/school/registrations")
    @PreAuthorize("hasRole('ADMIN')")
    @SkipRestrictedCheck
    public ResponseEntity<ResponseObject> processRegistration(
            @RequestParam(name = "a", defaultValue = "true") String isApproved,
            @RequestParam(name = "requestId") int requestId,
            @RequestBody ProcessRegistrationRequest reviewRequest) {
        return adminService.processRegistration(isApproved.equalsIgnoreCase("true"), requestId, reviewRequest);
    }
}
