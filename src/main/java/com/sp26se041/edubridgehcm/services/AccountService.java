package com.sp26se041.edubridgehcm.services;

import com.sp26se041.edubridgehcm.requests.RestrictionRequest;
import com.sp26se041.edubridgehcm.requests.UpdateProfileRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;

public interface AccountService {
    ResponseEntity<ResponseObject> logout(HttpServletRequest request, HttpServletResponse response);

    ResponseEntity<ResponseObject> getAccessToken(HttpServletRequest request);

    ResponseEntity<ResponseObject> toggleAccountRestriction(int accountId, RestrictionRequest request, HttpServletResponse response);

    ResponseEntity<ResponseObject> updateProfile(UpdateProfileRequest request, HttpServletResponse response);

    ResponseEntity<ResponseObject> viewProfile(HttpServletRequest request, HttpServletResponse response);
}
