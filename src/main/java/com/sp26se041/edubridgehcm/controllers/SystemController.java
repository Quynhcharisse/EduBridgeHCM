package com.sp26se041.edubridgehcm.controllers;

import com.sp26se041.edubridgehcm.requests.CreateConfigDataRequest;
import com.sp26se041.edubridgehcm.requests.CreateFacilityTemplateRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.SystemService;
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
@RequestMapping("/api/v1/system")
@RequiredArgsConstructor
@Tag(name = "System")
public class SystemController {

    private final SystemService systemService;

    @GetMapping("/config")
    @PreAuthorize("hasAnyRole('ADMIN', 'SCHOOL', 'PARENT', 'COUNSELLOR')")
    public ResponseEntity<ResponseObject> getConfigData() {
        return systemService.getConfigData();
    }

    @GetMapping("/config/key")
    @PreAuthorize("hasAnyRole('ADMIN', 'SCHOOL', 'PARENT', 'COUNSELLOR')")
    public ResponseEntity<ResponseObject> getConfigDataByKey(@RequestParam String k) {
        return systemService.getConfigDataByKey(k);
    }

    @PutMapping("/config")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseObject> updateConfigData(@RequestBody CreateConfigDataRequest request) {
        return systemService.updateConfigData(request);
    }

    @GetMapping("/school/quota/year")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ResponseObject> getQuotaByYear(@RequestParam String year) {
        return systemService.getQuotaByYear(year);
    }

    @PostMapping("/school/facility/template")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> createFacilityTemplate(@RequestBody CreateFacilityTemplateRequest request) {
        return systemService.createFacilityTemplate(request);
    }

    @GetMapping("/school/facility/template/list")
    @PreAuthorize("hasAnyRole('SCHOOL')")
    public ResponseEntity<ResponseObject> getFacilityTemplate() {
        return systemService.getFacilityTemplate();
    }

}
