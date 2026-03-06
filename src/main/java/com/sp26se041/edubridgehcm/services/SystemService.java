package com.sp26se041.edubridgehcm.services;

import com.sp26se041.edubridgehcm.requests.CreateConfigDataRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import org.springframework.http.ResponseEntity;

public interface SystemService {

    ResponseEntity<ResponseObject> getConfigData();

    ResponseEntity<ResponseObject> getConfigDataByKey(String k);

    ResponseEntity<ResponseObject> updateConfigData(CreateConfigDataRequest request);
}
