package com.sp26se041.edubridgehcm.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/counsellor")
@RequiredArgsConstructor
@Tag(name = "Counsellor")
public class CounsellorController {
}
