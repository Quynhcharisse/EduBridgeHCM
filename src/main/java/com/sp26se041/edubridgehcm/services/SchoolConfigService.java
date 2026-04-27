package com.sp26se041.edubridgehcm.services;

import com.sp26se041.edubridgehcm.enums.ImportType;
import com.sp26se041.edubridgehcm.requests.ImportConfirmRequest;
import com.sp26se041.edubridgehcm.requests.SchoolConfigRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface SchoolConfigService {

    ResponseEntity<ResponseObject> updateSchoolConfig(int schoolId, SchoolConfigRequest request);

    ResponseEntity<ResponseObject> importMandatoryDocs(MultipartFile file, ImportType type);

    ResponseEntity<ResponseObject> importConfirm(ImportConfirmRequest request, ImportType type);

    ResponseEntity<ResponseObject> validateSingleRow(ImportConfirmRequest request, ImportType type);

    ResponseEntity<ResponseObject> getSchoolConfigList(int schoolId);

    ResponseEntity<ResponseObject> getSchoolConfigByKey(String k);

    ResponseEntity<ResponseObject> getCampusConfigList();
}

