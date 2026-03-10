package com.sp26se041.edubridgehcm.controllers;


import com.sp26se041.edubridgehcm.requests.CreateAccountCounsellorRequest;
import com.sp26se041.edubridgehcm.requests.CreateCampusRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.SchoolService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping("/campus/list")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> viewCampusList() {
        return schoolService.viewCampusList();
    }

    @PostMapping("/counsellor")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> createAccountCounsellor(@RequestBody CreateAccountCounsellorRequest request) {
        return schoolService.createAccountCounsellor(request);
    }

    @PostMapping("/counsellor/list")
    @PreAuthorize("hasRole('SCHOOL')")
    public ResponseEntity<ResponseObject> viewAccountCounsellorList() {
        return schoolService.viewAccountCounsellorList();
    }

}
