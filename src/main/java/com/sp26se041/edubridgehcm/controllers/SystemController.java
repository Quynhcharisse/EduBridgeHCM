package com.sp26se041.edubridgehcm.controllers;

import com.sp26se041.edubridgehcm.enums.ImportType;
import com.sp26se041.edubridgehcm.requests.CreateConfigDataRequest;
import com.sp26se041.edubridgehcm.requests.ImportConfirmRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.SystemService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
    public ResponseEntity<ResponseObject> getConfigDataByKey(@RequestParam String k) {
        return systemService.getConfigDataByKey(k);
    }

    @PutMapping("/config")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseObject> updateConfigData(@RequestBody CreateConfigDataRequest request) {
        return systemService.updateConfigData(request);
    }

    @PostMapping(value = "/config/preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseObject> importPreview(
            @RequestParam("file") MultipartFile file,
            @Parameter(
                    description = "Loại dữ liệu import cấu hình",
                    schema = @Schema(
                            allowableValues = {"ALLOWED_METHODS", "ADMISSION_PROCESSES", "METHOD_DOCUMENTS"}
                    )
            )
            @RequestParam("type") ImportType type) {
        return systemService.importPreview(file, type);
    }

    @PostMapping("/config/confirm")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseObject> importConfirm(
            @RequestBody ImportConfirmRequest request,
            @Parameter(
                    description = "Loại dữ liệu import cấu hình",
                    schema = @Schema(
                            allowableValues = {"ALLOWED_METHODS", "ADMISSION_PROCESSES", "METHOD_DOCUMENTS"}
                    )
            )
            @RequestParam("type") ImportType type) {
        return systemService.importConfirm(request, type);
    }

    @PostMapping("/config/validate-row")
    public ResponseEntity<ResponseObject> validateRow(
            @RequestBody ImportConfirmRequest request,
            @Parameter(
                    description = "Loại dữ liệu import cấu hình",
                    schema = @Schema(
                            allowableValues = {"ALLOWED_METHODS", "ADMISSION_PROCESSES", "METHOD_DOCUMENTS"}
                    )
            )
            @RequestParam("type") ImportType type) {
        return systemService.validateSingleRow(request, type);
    }
}
