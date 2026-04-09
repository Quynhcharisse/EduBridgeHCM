package com.sp26se041.edubridgehcm.services;

import com.sp26se041.edubridgehcm.requests.AddSubjectRequest;
import com.sp26se041.edubridgehcm.requests.CreatePersonalityTypeRequest;
import com.sp26se041.edubridgehcm.requests.UpsertServicePackageFeeRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface AdminService {

    ResponseEntity<ResponseObject> verifyRegistration(int requestId);

    ResponseEntity<ResponseObject> viewSchoolRegistrationList();

    ResponseEntity<ResponseObject> upsertServicePackageFee(UpsertServicePackageFeeRequest request);

    ResponseEntity<ResponseObject> publishServicePackageFee(Integer packageId);

    ResponseEntity<ResponseObject> viewServicePackageFeeList();

    ResponseEntity<ResponseObject> deActiveServicePackageFee(Integer packageId);

    ResponseEntity<ResponseObject> createPersonalityType(CreatePersonalityTypeRequest request);

    ResponseEntity<ResponseObject> getPersonalityTypeList();

    ResponseEntity<ResponseObject> createSubject(AddSubjectRequest request);

    ResponseEntity<ResponseObject> getAllSubjects();

    ResponseEntity<ResponseObject> uploadTemplateDocxTemplate(MultipartFile file, String categoryTemplate);

    ResponseEntity<ResponseObject> removeTemplateDocx(long templateDocxId);
}