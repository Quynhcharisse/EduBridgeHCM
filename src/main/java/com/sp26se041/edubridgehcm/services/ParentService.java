package com.sp26se041.edubridgehcm.services;

import com.sp26se041.edubridgehcm.models.ChatMessage;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

public interface ParentService {
    ResponseEntity<ResponseObject> createMessage(ChatMessage chatMessage);
}
