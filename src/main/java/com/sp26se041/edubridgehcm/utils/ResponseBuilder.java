package com.sp26se041.edubridgehcm.utils;

import com.sp26se041.edubridgehcm.responses.ResponseObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseBuilder {
    public static ResponseEntity<ResponseObject> build(HttpStatus status, String message, Object body) {
        return ResponseEntity.status(status)
                .body(
                        ResponseObject.builder()
                                .message(message)
                                .body(body)
                                .build()
                );
    }
}
