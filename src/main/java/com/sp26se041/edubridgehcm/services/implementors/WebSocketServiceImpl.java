package com.sp26se041.edubridgehcm.services.implementors;

import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.models.Account;
import com.sp26se041.edubridgehcm.models.ChatMessage;
import com.sp26se041.edubridgehcm.models.Conversation;
import com.sp26se041.edubridgehcm.models.PersonalityType;
import com.sp26se041.edubridgehcm.models.StudentProfile;
import com.sp26se041.edubridgehcm.repositories.AccountRepo;
import com.sp26se041.edubridgehcm.repositories.ChatMessageRepo;
import com.sp26se041.edubridgehcm.repositories.ConversationRepo;
import com.sp26se041.edubridgehcm.repositories.PersonalityTypeRepo;
import com.sp26se041.edubridgehcm.repositories.StudentInfoRepo;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.WebSocketService;
import com.sp26se041.edubridgehcm.utils.AccountRestrictionUtil;
import com.sp26se041.edubridgehcm.utils.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WebSocketServiceImpl implements WebSocketService {

    private final ConversationRepo conversationRepo;
    private final ChatMessageRepo chatMessageRepo;

    @Override
    public String createChatMessage(ChatMessage chatMessage) {
        Optional<Conversation> conversation = conversationRepo.findById(chatMessage.getConversationId());
        if (conversation.isEmpty()) {
            return "Please refresh page and try again later";
        }
        chatMessage.setStatus(Status.MESSAGE_SENT);
        conversation.get().setUpdatedDate(LocalDateTime.now());
        chatMessageRepo.save(chatMessage);
        return "";
    }

    @Override
    public ResponseEntity<ResponseObject> markConversationAsRead(Long conversationId, String receiverEmail) {
        List<ChatMessage> unreadMessages = chatMessageRepo.findByConversationIdAndReceiverNameAndStatus(conversationId, receiverEmail, Status.MESSAGE_SENT);
        unreadMessages.forEach(msg -> msg.setStatus(Status.MESSAGE_READ));
        chatMessageRepo.saveAll(unreadMessages);
        return ResponseBuilder.build(HttpStatus.OK, "Marked as read", unreadMessages);
    }

}
