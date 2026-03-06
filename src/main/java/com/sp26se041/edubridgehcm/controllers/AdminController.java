package com.sp26se041.edubridgehcm.controllers;

import com.sp26se041.edubridgehcm.configurations.SkipRestrictedCheck;
import com.sp26se041.edubridgehcm.requests.CreatePostRequest;
import com.sp26se041.edubridgehcm.requests.CreateServicePackageFeeRequest;
import com.sp26se041.edubridgehcm.requests.DisablePostRequest;
import com.sp26se041.edubridgehcm.requests.ProcessRegistrationRequest;
import com.sp26se041.edubridgehcm.requests.UpdatePostRequest;
import com.sp26se041.edubridgehcm.requests.UpdateServicePackageFeeRequest;
import com.sp26se041.edubridgehcm.requests.UpdateStatusServicePackageFeeRequest;
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

    @PostMapping("/school/registrations")
    @PreAuthorize("hasRole('ADMIN')")
    @SkipRestrictedCheck
    public ResponseEntity<ResponseObject> processRegistration(@RequestParam(name = "a", defaultValue = "true") String isApproved, @RequestParam(name = "requestId") int requestId, @RequestBody ProcessRegistrationRequest reviewRequest) {
        return adminService.processRegistration(isApproved.equalsIgnoreCase("true"), requestId, reviewRequest);
    }

    @PostMapping("/service/package/fee")
    @PreAuthorize("hasRole('ADMIN')")
    @SkipRestrictedCheck
    public ResponseEntity<ResponseObject> createServicePackageFee(@RequestBody CreateServicePackageFeeRequest request) {
        return adminService.createServicePackageFee(request);
    }

    @PutMapping("/service/package/fee")
    @PreAuthorize("hasRole('ADMIN')")
    @SkipRestrictedCheck
    public ResponseEntity<ResponseObject> updateServicePackageFee(@RequestBody UpdateServicePackageFeeRequest request) {
        return adminService.updateServicePackageFee(request);
    }

    @PutMapping("status/service/package/fee")
    @PreAuthorize("hasRole('ADMIN')")
    @SkipRestrictedCheck
    public ResponseEntity<ResponseObject> updateStatusServicePackageFee(@RequestBody UpdateStatusServicePackageFeeRequest request) {
        return adminService.updateStatusServicePackageFee(request);
    }

    @GetMapping("/service/package/fee/list")
    @PreAuthorize("hasRole('ADMIN')")
    @SkipRestrictedCheck
    public ResponseEntity<ResponseObject> viewServicePackageFeeList() {
        return adminService.viewServicePackageFeeList();
    }

    @PostMapping("/post")
    @PreAuthorize("hasRole('ADMIN')")
    @SkipRestrictedCheck
    public ResponseEntity<ResponseObject> createPost(@RequestBody CreatePostRequest request) {
        return adminService.createPost(request);
    }

    @PutMapping("/post")
    @PreAuthorize("hasRole('ADMIN')")
    @SkipRestrictedCheck
    public ResponseEntity<ResponseObject> updatePost(@RequestBody UpdatePostRequest request) {
        return adminService.updatePost(request);
    }

    @PutMapping("post/disable")
    @PreAuthorize("hasRole('ADMIN')")
    @SkipRestrictedCheck
    public ResponseEntity<ResponseObject> disablePost(@RequestBody DisablePostRequest request) {
        return adminService.disablePost(request);
    }

    @GetMapping("/post/list")
    @PreAuthorize("hasRole('ADMIN')")
    @SkipRestrictedCheck
    public ResponseEntity<ResponseObject> viewPostList() {
        return adminService.viewPostList();
    }
}
