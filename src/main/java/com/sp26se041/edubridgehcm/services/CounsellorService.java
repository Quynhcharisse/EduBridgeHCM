package com.sp26se041.edubridgehcm.services;

import com.sp26se041.edubridgehcm.responses.ResponseObject;
import org.springframework.http.ResponseEntity;

public interface CounsellorService {
    ResponseEntity<ResponseObject> getConversations(Long cursorId);

}
