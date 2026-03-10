package com.sp26se041.edubridgehcm.services;

import com.sp26se041.edubridgehcm.requests.CreateAccountCounsellorRequest;
import com.sp26se041.edubridgehcm.requests.CreateCampusRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import org.springframework.http.ResponseEntity;

public interface SchoolService {

    ResponseEntity<ResponseObject> createCampus(CreateCampusRequest request);

    ResponseEntity<ResponseObject> viewCampusList();

    ResponseEntity<ResponseObject> createAccountCounsellor(CreateAccountCounsellorRequest request);

    ResponseEntity<ResponseObject> viewAccountCounsellorList();
}
