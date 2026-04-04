package com.sp26se041.edubridgehcm.controllers;

import com.sp26se041.edubridgehcm.models.ChatMessage;
import com.sp26se041.edubridgehcm.requests.AddFavouriteSchoolRequest;
import com.sp26se041.edubridgehcm.requests.AddStudentInfoRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.ParentService;
import com.sp26se041.edubridgehcm.services.WebSocketService;
import com.sp26se041.edubridgehcm.utils.ResponseBuilder;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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

import java.time.LocalDateTime;

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
                    "/private",
                    systemMessage
            );
            return;
        }
        simpMessagingTemplate.convertAndSendToUser(
                message.getReceiverName(),
                "/private",
                message
        );
        simpMessagingTemplate.convertAndSendToUser(
                message.getSenderName(),
                "/private",
                message
        );
    }
      
    @GetMapping("/messages/history/{parentEmail}/{campusId}/{studentProfileId}")
    public ResponseEntity<ResponseObject> getChatHistory(@PathVariable String parentEmail, @PathVariable int campusId, @PathVariable int studentProfileId, @RequestParam (required = false) Long cursorId) {
        return parentService.getChatHistory(parentEmail, campusId, studentProfileId, cursorId);
    }

    @PutMapping("/messages/read/{conversationId}/{username}")
    public ResponseEntity<ResponseObject> readMessages(@PathVariable Long conversationId, @PathVariable String username) {
        return webSocketService.markConversationAsRead(conversationId, username);
    }

    @GetMapping("/conversations")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<?> getConversations(@RequestParam(required = false) Long cursorId) {
        return parentService.getConversations(cursorId);
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

    @GetMapping("/student")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<ResponseObject> getStudentInfos() {
        return parentService.getStudents();
    }


}
