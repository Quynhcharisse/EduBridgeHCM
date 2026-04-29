package com.sp26se041.edubridgehcm.controllers;

import com.sp26se041.edubridgehcm.models.ChatMessage;
import com.sp26se041.edubridgehcm.requests.AddFavouriteSchoolRequest;
import com.sp26se041.edubridgehcm.requests.AddStudentInfoRequest;
import com.sp26se041.edubridgehcm.requests.AutoFillTranscriptRequest;
import com.sp26se041.edubridgehcm.requests.CreateConsultationOfflineRequest;
import com.sp26se041.edubridgehcm.requests.CreateConversationRequest;
import com.sp26se041.edubridgehcm.requests.UpdateStudentInfoRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.ParentService;
import com.sp26se041.edubridgehcm.services.WebSocketService;
import com.sp26se041.edubridgehcm.utils.ResponseBuilder;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/parent")
@RequiredArgsConstructor
@Tag(name = "Parent")
public class ParentController {

    private final SimpMessagingTemplate simpMessagingTemplate;

    private final ParentService parentService;

    private final WebSocketService webSocketService;

    @MessageMapping("/private-message")
    public void privateMessage(ChatMessage message) {
        String error = webSocketService.createChatMessage(message);
        if (error != null && !error.isBlank()) {
            ChatMessage systemMessage = ChatMessage.builder()
                    .senderName("System")
                    .receiverName(message.getSenderName())
                    .message(error)
                    .timestamp(LocalDateTime.now())
                    .build();
            simpMessagingTemplate.convertAndSendToUser(
                    message.getSenderName(),
                    "/queue/private",
                    systemMessage
            );
            return;
        }

        String receiver = message.getReceiverName() != null ? message.getReceiverName().trim() : "";
        if (!receiver.isEmpty()) {
            simpMessagingTemplate.convertAndSendToUser(
                    receiver,
                    "/queue/private",
                    message
            );
            return;
        }
        // receiver rỗng: fan-out tới tất cả TVV của campus (principal = email đăng nhập STOMP)
        Integer campusId = message.getCampusId();
        if (campusId != null && campusId > 0) {
            List<String> counsellorEmails = parentService.findCounsellorEmailsByCampusId(campusId);
            for (String email : counsellorEmails) {
                if (email == null || email.isBlank()) continue;
                simpMessagingTemplate.convertAndSendToUser(
                        email.trim(),
                        "/queue/private",
                        message
                );
            }
        }
    }
      
    @GetMapping("/messages/history/{parentEmail}/{campusId}/{studentProfileId}")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<ResponseObject> getChatHistory(@PathVariable String parentEmail, @PathVariable int campusId, @PathVariable int studentProfileId, @RequestParam (required = false) Long cursorId) {
        return parentService.getChatHistory(parentEmail, campusId, studentProfileId, cursorId);
    }

    @PutMapping("/messages/read/{conversationId}/{username}")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<ResponseObject> readMessages(@PathVariable Long conversationId, @PathVariable String username) {
        return webSocketService.markConversationAsRead(conversationId, username);
    }

    @GetMapping("/conversations")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<?> getConversations(@RequestParam(required = false) Long cursorId) {
        return parentService.getConversations(cursorId);
    }

    @PostMapping("/conversation")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<ResponseObject> createConversation(@RequestBody CreateConversationRequest createConversationRequest) {
        return parentService.createConversation(createConversationRequest);
    }

    @PostMapping("/favourite/school")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<ResponseObject> addFavouriteSchool(@RequestBody AddFavouriteSchoolRequest request) {
         return parentService.addFavouriteSchool(request);
    }

    @DeleteMapping("/favourite/school/{id}")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<ResponseObject> removeFavouriteSchool(@PathVariable long id) {
        return parentService.removeFavouriteSchool(id);
    }

    @GetMapping("/favourite/school")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<ResponseObject> getFavouriteSchool(@RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "10") int pageSize) {
        return parentService.getFavouriteSchools(page, pageSize);
    }



    //Personality Type, Subject
    @GetMapping("/personality/type")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<ResponseObject> getPersonalityTypes() {
        return parentService.getPersonalityTypes();
    }

    @GetMapping("/subject")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<ResponseObject> getSubjects() {
        return parentService.getAllSubjects();
    }
    @GetMapping("/major")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<ResponseObject> getMajors() {
        return parentService.getAllMajors();
    }

    //Student Info
    @PostMapping("/student")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<ResponseObject> addStudentInfo(@RequestBody AddStudentInfoRequest request) {
        return parentService.addStudentInfo(request);
    }

    @PutMapping("/student")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<ResponseObject> updateStudentInfo(@RequestBody UpdateStudentInfoRequest request) {
        return parentService.updateStudentInfo(request);
    }

    @PostMapping("/transcript/auto/fill")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<ResponseObject> autoFillTranscript(@RequestBody AutoFillTranscriptRequest request) {
        return parentService.autoFillTranscript(request);
    }

    @GetMapping("/student")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<ResponseObject> getStudentInfos() {
        return parentService.getStudents();
    }

    @GetMapping("/student/{id}")
    @PreAuthorize("hasAnyRole('PARENT')")
    public ResponseEntity<ResponseObject> getStudentInfoById(@PathVariable int id) {
        return parentService.getStudentInfo(id);
    }

    @GetMapping("/slots/{campusId}")
    @PreAuthorize("hasAnyRole('PARENT')")
    public ResponseEntity<ResponseObject> getSlots(@PathVariable int campusId ,@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
           return parentService.getSlots(campusId, startDate, endDate);
    }

    @PostMapping("/book/consultation/offline")
    @PreAuthorize("hasAnyRole('PARENT')")
    public ResponseEntity<ResponseObject> bookConsultationOffline(@RequestBody CreateConsultationOfflineRequest request) {
         return parentService.createConsultationOfflineRequest(request);
    }

    @GetMapping("/consultation/offline")
    @PreAuthorize("hasAnyRole('PARENT')")
    public ResponseEntity<ResponseObject> getConsultationOfflineRequests(@RequestParam String status ,@RequestParam int page, @RequestParam int pageSize) {
        return parentService.getConsultationOfflineRequests(status, page, pageSize);
    }
}

