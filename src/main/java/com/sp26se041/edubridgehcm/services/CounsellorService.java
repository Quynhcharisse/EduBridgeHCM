package com.sp26se041.edubridgehcm.services;

import com.sp26se041.edubridgehcm.responses.ResponseObject;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

public interface CounsellorService {
    ResponseEntity<ResponseObject> getCounsellorCalendar(LocalDate startDate, LocalDate endDate);
    ResponseEntity<ResponseObject> getConversations(String status, Long cursorId);
    ResponseEntity<ResponseObject> getChatHistory(String parentEmail, String counsellorEmail, int studentProfileId, Long cursorId);
}
