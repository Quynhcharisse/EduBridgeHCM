package com.sp26se041.edubridgehcm.services;

import com.sp26se041.edubridgehcm.requests.AddSubjectRequest;
import com.sp26se041.edubridgehcm.requests.CreatePersonalityTypeRequest;
import com.sp26se041.edubridgehcm.requests.CreateServicePackageFeeRequest;
import com.sp26se041.edubridgehcm.requests.UpdateServicePackageFeeRequest;
import com.sp26se041.edubridgehcm.requests.UpdateStatusServicePackageFeeRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import org.springframework.http.ResponseEntity;

public interface AdminService {

    ResponseEntity<ResponseObject> verifyRegistration(int requestId);

    ResponseEntity<ResponseObject> viewSchoolRegistrationList();

    ResponseEntity<ResponseObject> createServicePackageFee(CreateServicePackageFeeRequest request);

    ResponseEntity<ResponseObject> updateServicePackageFee(UpdateServicePackageFeeRequest request);

    ResponseEntity<ResponseObject> viewServicePackageFeeList();

    ResponseEntity<ResponseObject> updateStatusServicePackageFee(UpdateStatusServicePackageFeeRequest request);

    ResponseEntity<ResponseObject> createPersonalityType(CreatePersonalityTypeRequest request);

    ResponseEntity<ResponseObject> getPersonalityTypeList();

    ResponseEntity<ResponseObject> createSubject(AddSubjectRequest request);
}
