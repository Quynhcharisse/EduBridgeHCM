package com.sp26se041.edubridgehcm.controllers;

import com.sp26se041.edubridgehcm.requests.ChatMessageForChatBot;
import com.sp26se041.edubridgehcm.requests.UpdateConsultationOfflineRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.CounsellorService;
import com.sp26se041.edubridgehcm.services.ParentService;
import com.sp26se041.edubridgehcm.services.WebSocketService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;

@RestController
@RequestMapping("/api/v1/counsellor")
@RequiredArgsConstructor
@Tag(name = "Counsellor")
public class CounsellorController {

    private final WebSocketService webSocketService;

    private final CounsellorService counsellorService;

    private final ParentService parentService;


    @GetMapping("/messages/history/{parentEmail}/{counsellorEmail}/{studentProfileId}")
    public ResponseEntity<ResponseObject> getChatHistory(@PathVariable String parentEmail, @PathVariable String counsellorEmail, @PathVariable int studentProfileId,@RequestParam(required = false) Long cursorId) {
        return counsellorService.getChatHistory(parentEmail, counsellorEmail, studentProfileId, cursorId);
    }

    @GetMapping("/conversations")
    @PreAuthorize("hasRole('COUNSELLOR')")
    public ResponseEntity<?> getConversations(@RequestParam String status, @RequestParam (required = false) Long cursorId){
        return counsellorService.getConversations(status, cursorId);
    }

    @GetMapping("/calendar")
    @PreAuthorize("hasRole('COUNSELLOR')")
    public ResponseEntity<ResponseObject> getCounsellorCalendar(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate){
        return counsellorService.getCounsellorCalendar(startDate, endDate);
    }

    @PutMapping("/messages/read/{conversationId}/{username}")
    @PreAuthorize("hasRole('COUNSELLOR')")
    public ResponseEntity<ResponseObject> readMessages(@PathVariable Long conversationId, @PathVariable String username) {
        return webSocketService.markConversationAsRead(conversationId, username);
    }

    @GetMapping("/student/{id}")
    @PreAuthorize("hasAnyRole('COUNSELLOR')")
    public ResponseEntity<ResponseObject> getStudentInfoById(@PathVariable int id) {
        return parentService.getStudentInfo(id);
    }

    @GetMapping("/consultation/offline")
    @PreAuthorize("hasAnyRole('COUNSELLOR')")
    public ResponseEntity<ResponseObject> getConsultationOfflineRequests(@RequestParam String status ,@RequestParam int page, @RequestParam int pageSize) {
        return counsellorService.getConsultationOfflineRequests(status, page, pageSize);
    }

    @GetMapping()
    @PreAuthorize("hasAnyRole('COUNSELLOR')")
    public ResponseEntity<ResponseObject> getCounsellorsInSlot(@RequestParam LocalDate appointmentDate, @RequestParam LocalTime appointmentTime) {
        return counsellorService.getCounsellorsInSlot(appointmentTime, appointmentDate);
    }

    @GetMapping("/slots")
    @PreAuthorize("hasAnyRole('COUNSELLOR')")
    public ResponseEntity<ResponseObject> getSlotsOfCampus(@RequestParam LocalDate startDate, @RequestParam LocalDate endDate){
        return counsellorService.getSlotsOfCampus(startDate, endDate);
    }

    @PutMapping("/consultation/offline")
    @PreAuthorize("hasAnyRole('COUNSELLOR')")
    public ResponseEntity<ResponseObject> updateConsultationOfflineRequest(@RequestBody UpdateConsultationOfflineRequest updateConsultationOfflineRequest){
        return counsellorService.updateConsultationOfflineRequest(updateConsultationOfflineRequest);
    }

    @PostMapping("/chat-with-AI-chatbot")
    @PreAuthorize("hasAnyRole('COUNSELLOR')")
    public ResponseEntity<ResponseObject> chatWithAIChatBotSchool(@RequestBody ChatMessageForChatBot chatMessageForChatBot) {
        return counsellorService.chatWithChatbotForSchool(chatMessageForChatBot);
    }

}
