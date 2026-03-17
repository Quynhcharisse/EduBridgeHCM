package com.sp26se041.edubridgehcm.controllers;

import com.sp26se041.edubridgehcm.requests.RestrictionRequest;
import com.sp26se041.edubridgehcm.requests.UpdateProfileRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.AccountService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

@RestController
@RequestMapping("/api/v1/account")
@RequiredArgsConstructor
@Tag(name = "Account")
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/logout")
    @PreAuthorize("hasAnyRole('ADMIN', 'SCHOOL', 'PARENT', 'COUNSELLOR')")
    public ResponseEntity<ResponseObject> logout(HttpServletRequest request, HttpServletResponse response) {
        return accountService.logout(request, response);
    }

    @PostMapping("/access")
    @PreAuthorize("hasAnyRole('ADMIN', 'SCHOOL', 'PARENT', 'COUNSELLOR')")
    public ResponseEntity<ResponseObject> getAccessToken(HttpServletRequest request) {
        return accountService.getAccessToken(request);
    }

    @PostMapping("/{accountId}/restrict")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseObject> toggleAccountRestriction(
            @PathVariable int accountId,
            @RequestBody RestrictionRequest request) {
        return accountService.toggleAccountRestriction(accountId, request);
    }

    @GetMapping("/user/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseObject> viewUserList(@RequestParam String role, @RequestParam int page, @RequestParam int pageSize) {
        return accountService.viewUserList(role, page, pageSize);
    }

    @GetMapping("/school/{schoolId}/campus/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseObject> viewSchoolCampusList(@PathVariable int schoolId, @RequestParam int page, @RequestParam int pageSize) {
        return accountService.viewSchoolCampusList(schoolId, page, pageSize);
    }

    @GetMapping("/campus/{campusId}/counsellor/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseObject> viewCampusCounsellorList(@PathVariable int campusId, @RequestParam int page, @RequestParam int pageSize) {
        return accountService.viewCampusCounsellorList(campusId, page, pageSize);
    }

    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole('ADMIN', 'SCHOOL', 'PARENT', 'COUNSELLOR')")
    public ResponseEntity<ResponseObject> viewProfile(HttpServletRequest request) {
        return accountService.viewProfile(request);
    }

    @PutMapping("/profile")
    @PreAuthorize("hasAnyRole('ADMIN', 'SCHOOL', 'PARENT', 'COUNSELLOR')")
    public ResponseEntity<ResponseObject> updateProfile(@RequestBody UpdateProfileRequest request, HttpServletRequest httpRequest) {
        return accountService.updateProfile(request, httpRequest);
    }

}
