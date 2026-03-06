package com.sp26se041.edubridgehcm.controllers;

import com.sp26se041.edubridgehcm.requests.CreateConfigDataRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.SystemService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
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
    @PreAuthorize("hasAnyRole('ADMIN', 'SCHOOL', 'COUNSELLOR', 'PARENT')")
    public ResponseEntity<ResponseObject> getConfigData(){
        return systemService.getConfigData();
    }

    @GetMapping("/config/key")
    @PreAuthorize("hasAnyRole('ADMIN', 'SCHOOL', 'COUNSELLOR', 'PARENT')")
    public ResponseEntity<ResponseObject> getConfigDataByKey(@RequestParam String k){
        return systemService.getConfigDataByKey(k);
    }

    @PutMapping("/config")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseObject> updateConfigData(@RequestBody CreateConfigDataRequest request){
        return systemService.updateConfigData(request);
    }

}
