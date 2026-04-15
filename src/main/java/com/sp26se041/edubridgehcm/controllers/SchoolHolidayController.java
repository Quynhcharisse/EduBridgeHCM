package com.sp26se041.edubridgehcm.controllers;

import com.sp26se041.edubridgehcm.requests.CreateHolidayRequest;
import com.sp26se041.edubridgehcm.requests.UpdateHolidayRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.SchoolHolidayService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/holiday")
@RequiredArgsConstructor
@Tag(name = "School Holiday")
public class SchoolHolidayController {

    private final SchoolHolidayService schoolHolidayService;

    @PostMapping("/")
    @PreAuthorize("hasAnyRole('SCHOOL')")
    public ResponseEntity<ResponseObject> createHoliday(@RequestBody CreateHolidayRequest request) {
        return schoolHolidayService.createHoliday(request);
    }

    @PutMapping("/")
    @PreAuthorize("hasAnyRole('SCHOOL')")
    public ResponseEntity<ResponseObject> updateHoliday(@RequestBody UpdateHolidayRequest request) {
        return schoolHolidayService.updateHoliday(request);
    }

    @GetMapping("/")
    @PreAuthorize("hasAnyRole('SCHOOL')")
    public ResponseEntity<ResponseObject> viewHolidayList() {
        return schoolHolidayService.viewHolidayList();
    }
}
