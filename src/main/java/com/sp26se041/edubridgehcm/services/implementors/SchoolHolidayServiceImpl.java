package com.sp26se041.edubridgehcm.services.implementors;

import com.sp26se041.edubridgehcm.requests.CreateHolidayRequest;
import com.sp26se041.edubridgehcm.requests.UpdateHolidayRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.SchoolHolidayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SchoolHolidayServiceImpl implements SchoolHolidayService {

    @Override
    public ResponseEntity<ResponseObject> createHoliday(CreateHolidayRequest request) {
        return null;
    }

    @Override
    public ResponseEntity<ResponseObject> updateHoliday(UpdateHolidayRequest request) {
        return null;
    }

    @Override
    public ResponseEntity<ResponseObject> viewHolidayList() {
        return null;
    }
}
