package com.sp26se041.edubridgehcm.controllers;

import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.CounsellorService;
import com.sp26se041.edubridgehcm.services.WebSocketService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/counsellor")
@RequiredArgsConstructor
@Tag(name = "Counsellor")
public class CounsellorController {

    private final WebSocketService webSocketService;

    private final CounsellorService counsellorService;


    @GetMapping("/messages/history/{parentEmail}/{counsellorEmail}/{studentProfileId}")
    public ResponseEntity<ResponseObject> getChatHistory(@PathVariable String parentEmail, @PathVariable String counsellorEmail, @PathVariable int studentProfileId,@RequestParam(required = false) Long cursorId) {
        return counsellorService.getChatHistory(parentEmail, counsellorEmail, studentProfileId, cursorId);
    }

    @GetMapping("/conversations")
    @PreAuthorize("hasRole('COUNSELLOR')")
    public ResponseEntity<?> getConversations(@RequestParam String status, @RequestParam (required = false) Long cursorId){
        return counsellorService.getConversations(status, cursorId);
    }

    @PutMapping("/messages/read/{conversationId}/{username}")
    @PreAuthorize("hasRole('COUNSELLOR')")
    public ResponseEntity<ResponseObject> readMessages(@PathVariable Long conversationId, @PathVariable String username) {
        return webSocketService.markConversationAsRead(conversationId, username);
    }
}
