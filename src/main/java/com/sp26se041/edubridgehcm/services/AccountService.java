package com.sp26se041.edubridgehcm.services;

import com.sp26se041.edubridgehcm.requests.RestrictionRequest;
import com.sp26se041.edubridgehcm.requests.UpdateProfileRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

public interface AccountService {

    ResponseEntity<ResponseObject> logout(HttpServletRequest request, HttpServletResponse response);

    ResponseEntity<ResponseObject> getAccessToken(HttpServletRequest request);

    ResponseEntity<ResponseObject> toggleAccountRestriction(int accountId, RestrictionRequest request);

    ResponseEntity<ResponseObject> viewUserList(String role, int page, int pageSize);

    ResponseEntity<ResponseObject> viewSchoolCampusList(int schoolId, int page, int pageSize);

    ResponseEntity<ResponseObject> viewCampusCounsellorList(int campusId, int page, int pageSize);

    ResponseEntity<ResponseObject> updateProfile(UpdateProfileRequest request, HttpServletRequest httpRequest);

    ResponseEntity<ResponseObject> viewProfile(HttpServletRequest request);

    ResponseEntity<Resource> exportUserList(String role) throws IOException;

    ResponseEntity<Resource> exportSchoolList() throws IOException;

    ResponseEntity<Resource> exportCounsellorListOfCampus() throws IOException;
}
