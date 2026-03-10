package com.sp26se041.edubridgehcm.services.implementors;

import com.sp26se041.edubridgehcm.requests.CreateAccountCounsellorRequest;
import com.sp26se041.edubridgehcm.requests.CreateCampusRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.SchoolService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SchoolServiceImpl implements SchoolService {
    @Override
    public ResponseEntity<ResponseObject> createCampus(CreateCampusRequest request) {
        return null;
    }

    @Override
    public ResponseEntity<ResponseObject> viewCampusList() {
        return null;
    }

    @Override
    public ResponseEntity<ResponseObject> createAccountCounsellor(CreateAccountCounsellorRequest request) {
        return null;
    }

    @Override
    public ResponseEntity<ResponseObject> viewAccountCounsellorList() {
        return null;
    }
}
