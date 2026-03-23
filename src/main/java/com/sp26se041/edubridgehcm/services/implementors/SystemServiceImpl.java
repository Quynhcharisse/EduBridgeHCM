package com.sp26se041.edubridgehcm.services.implementors;

import com.sp26se041.edubridgehcm.models.PlatformConfig;
import com.sp26se041.edubridgehcm.repositories.PlatformConfigRepo;
import com.sp26se041.edubridgehcm.requests.CreateConfigDataRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.SystemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SystemServiceImpl implements SystemService {

    private final PlatformConfigRepo platformConfigRepo;

    @Override
    public ResponseEntity<ResponseObject> getConfigData() {
        List<PlatformConfig> configList = platformConfigRepo.findAll();

        Map<String, Object> data = new HashMap<>();
        for (PlatformConfig config : configList) {
            String key = config.getKey();
        }

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
