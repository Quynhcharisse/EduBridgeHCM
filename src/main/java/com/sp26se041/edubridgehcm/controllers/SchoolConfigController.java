package com.sp26se041.edubridgehcm.controllers;

import com.sp26se041.edubridgehcm.requests.CreateFacilityTemplateRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.SchoolConfigService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/school/config")
@RequiredArgsConstructor
@Tag(name = "SchoolConfig")
public class SchoolConfigController {
    private final SchoolConfigService schoolConfigService;

    @PostMapping("/facility/template")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> createOrUpdateFacilityTemplate(@RequestParam int schoolId, @RequestBody CreateFacilityTemplateRequest request) {
        return schoolConfigService.createOrUpdateFacilityTemplate(schoolId, request);
    }

    @GetMapping("/facility/template")
    @PreAuthorize("hasAnyRole('SCHOOL', 'ADMIN')")
    public ResponseEntity<ResponseObject> getFacilityTemplate(@RequestParam int schoolId) {
        return schoolConfigService.getFacilityTemplate(schoolId);
    }
}

