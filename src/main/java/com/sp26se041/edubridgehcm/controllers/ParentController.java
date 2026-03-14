package com.sp26se041.edubridgehcm.controllers;

import com.sp26se041.edubridgehcm.models.ChatMessage;
import com.sp26se041.edubridgehcm.repositories.ChatMessageRepo;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/parent")
@RequiredArgsConstructor
@Tag(name = "Parent")
public class ParentController {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ChatMessageRepo chatMessageRepo;

    @MessageMapping("/private-message")
    public void privateMessage(ChatMessage message) {
        String receiver = message.getReceiverName();
        simpMessagingTemplate.convertAndSendToUser(receiver, "/private", message);

        chatMessageRepo.save(ChatMessage
                .builder()
                        .receiverName(message.getReceiverName())
                        .senderName(message.getSenderName())
                .message(message.getMessage())
                .build());
    }

}
