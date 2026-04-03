package com.sp26se041.edubridgehcm.controllers;

import com.sp26se041.edubridgehcm.requests.SchoolConfigRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.SchoolConfigService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
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

    @GetMapping("/{schoolId}")
    @PreAuthorize("hasAnyRole('SCHOOL')")
    public ResponseEntity<ResponseObject> getSchoolConfigList(@PathVariable int schoolId) {
        return schoolConfigService.getSchoolConfigList(schoolId);
    }

    @GetMapping("/key")
    @PreAuthorize("hasAnyRole('SCHOOL')")
    public ResponseEntity<ResponseObject> getSchoolConfigByKey(@RequestParam String k) {
        return schoolConfigService.getSchoolConfigByKey(k);
    }

    @PutMapping("/{schoolId}")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> updateSchoolConfig(@PathVariable int schoolId, @RequestBody SchoolConfigRequest request) {
        return schoolConfigService.updateSchoolConfig(schoolId, request);
    }
}

