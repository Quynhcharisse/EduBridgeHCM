package com.sp26se041.edubridgehcm.services.implementors;

import com.sp26se041.edubridgehcm.requests.CreateConfigDataRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.SystemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SystemServiceImpl implements SystemService {
    @Override
    public ResponseEntity<ResponseObject> getConfigData() {
        return null;
    }

    @Override
    public ResponseEntity<ResponseObject> getConfigDataByKey(String k) {
        return null;
    }

    @Override
    public ResponseEntity<ResponseObject> updateConfigData(CreateConfigDataRequest request) {
        return null;
    }
}
