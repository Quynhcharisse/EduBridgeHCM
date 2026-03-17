package com.sp26se041.edubridgehcm.controllers;

import com.sp26se041.edubridgehcm.models.ChatMessage;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.ParentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
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

    @MessageMapping("/private-message")
    public void privateMessage(ChatMessage message) {


        String error = parentService.createChatMessage(message);

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
    @GetMapping("/conversations")
    public ResponseEntity<?> getConversations(@RequestParam (required = false) Long cursorId){
        return ResponseEntity.ok(parentService.getConversations(cursorId));
    }

    @GetMapping("/messages/history/{parentEmail}/{counsellorEmail}")
    public ResponseEntity<ResponseObject> getChatHistory(@PathVariable String parentEmail, @PathVariable String counsellorEmail, @RequestParam (required = false) Long cursorId) {
        return parentService.getHistoryMessages(parentEmail, counsellorEmail, cursorId);
    }
    @PutMapping("/messages/read/{conversationId}/{username}")
    public ResponseEntity<ResponseObject> readMessages(@PathVariable Long conversationId, @PathVariable String username) {
        return parentService.markConversationAsRead(conversationId, username);
    }
}
