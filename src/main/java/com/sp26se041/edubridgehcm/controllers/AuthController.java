package com.sp26se041.edubridgehcm.controllers;

import com.sp26se041.edubridgehcm.requests.LoginRequest;
import com.sp26se041.edubridgehcm.requests.RegisterRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ResponseObject> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        return authService.login(request, response);
    }

    @PostMapping("/register")
    public ResponseEntity<ResponseObject> register(@RequestBody RegisterRequest request, HttpServletResponse response) {
        return authService.register(request, response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ResponseObject> refresh(HttpServletRequest request, HttpServletResponse response) {
        return authService.refresh(request, response);
    }
}
