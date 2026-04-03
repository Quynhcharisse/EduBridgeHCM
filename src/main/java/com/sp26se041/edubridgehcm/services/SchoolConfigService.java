package com.sp26se041.edubridgehcm.services;

import com.sp26se041.edubridgehcm.requests.CreateFacilityTemplateRequest;
import com.sp26se041.edubridgehcm.requests.SchoolConfigRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import org.springframework.http.ResponseEntity;

public interface SchoolConfigService {

    ResponseEntity<ResponseObject> updateSchoolConfig(int schoolId, SchoolConfigRequest request);

    ResponseEntity<ResponseObject> getSchoolConfigList(int schoolId);

    ResponseEntity<ResponseObject> getSchoolConfigByKey(String k);

    ResponseEntity<ResponseObject> createOrUpdateFacilityTemplate(int schoolId, CreateFacilityTemplateRequest request);

    ResponseEntity<ResponseObject> getFacilityTemplate(int schoolId);
}

