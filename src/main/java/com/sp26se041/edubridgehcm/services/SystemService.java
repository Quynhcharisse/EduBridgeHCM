package com.sp26se041.edubridgehcm.services;

import com.sp26se041.edubridgehcm.requests.CreateConfigDataRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.utils.ResponseBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface SystemService {

    ResponseEntity<ResponseObject> getConfigDataByKey(String k);

    ResponseEntity<ResponseObject> getConfigData();

    ResponseEntity<ResponseObject> updateConfigData(CreateConfigDataRequest request);

}
