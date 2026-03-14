package com.sp26se041.edubridgehcm.services;

import com.sp26se041.edubridgehcm.requests.LoginRequest;
import com.sp26se041.edubridgehcm.requests.RefreshTokenRequest;
import com.sp26se041.edubridgehcm.requests.RegisterRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;

public interface AuthService {
    ResponseEntity<ResponseObject> login(LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse response);

    ResponseEntity<ResponseObject> register(RegisterRequest request, HttpServletResponse response);

    ResponseEntity<ResponseObject> refresh(RefreshTokenRequest request, HttpServletRequest httpRequest, HttpServletResponse response);
}
