package com.sp26se041.edubridgehcm.services;

import com.sp26se041.edubridgehcm.enums.ImportType;
import com.sp26se041.edubridgehcm.requests.CreateConfigDataRequest;
import com.sp26se041.edubridgehcm.requests.ImportConfirmRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface SystemService {

    ResponseEntity<ResponseObject> getConfigDataByKey(String k);

    ResponseEntity<ResponseObject> getConfigData();

    ResponseEntity<ResponseObject> updateConfigData(CreateConfigDataRequest request);

    ResponseEntity<ResponseObject> importPreview(MultipartFile file, ImportType type);

    ResponseEntity<ResponseObject> importConfirm(ImportConfirmRequest request, ImportType type);

    ResponseEntity<ResponseObject> validateSingleRow(ImportConfirmRequest request, ImportType type);
}
