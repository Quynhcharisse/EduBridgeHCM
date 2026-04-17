package com.sp26se041.edubridgehcm.services;

import com.sp26se041.edubridgehcm.requests.CreateHolidayRequest;
import com.sp26se041.edubridgehcm.requests.UpdateHolidayRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import org.springframework.http.ResponseEntity;

public interface SchoolHolidayService {
    
    ResponseEntity<ResponseObject> createHoliday(CreateHolidayRequest request);

    ResponseEntity<ResponseObject> updateHoliday(UpdateHolidayRequest request);

    ResponseEntity<ResponseObject> viewHolidayList();
}
