package com.sp26se041.edubridgehcm.controllers;

import com.sp26se041.edubridgehcm.configurations.SkipRestrictedCheck;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/account")
@RequiredArgsConstructor
@Tag(name = "Account")
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/logout")
    @PreAuthorize("hasAnyRole('ADMIN', 'SCHOOL', 'PARENT', 'COUNSELLOR')")
    @SkipRestrictedCheck
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
    @SkipRestrictedCheck
    public ResponseEntity<ResponseObject> toggleAccountRestriction(
            @PathVariable int accountId,
            @RequestBody RestrictionRequest request,
            HttpServletResponse response) {
        return accountService.toggleAccountRestriction(accountId, request, response);
    }

    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole('ADMIN', 'SCHOOL', 'PARENT', 'COUNSELLOR')")
    @SkipRestrictedCheck
    public ResponseEntity<ResponseObject> viewProfile(HttpServletRequest request, HttpServletResponse response) {
        return accountService.viewProfile(request, response);
    }

    @PostMapping("/profile")
    @PreAuthorize("hasAnyRole('ADMIN', 'SCHOOL', 'PARENT', 'COUNSELLOR')")
    public ResponseEntity<ResponseObject> updateProfile(@RequestBody UpdateProfileRequest request, HttpServletResponse response) {
        return accountService.updateProfile(request, response);
    }

}
