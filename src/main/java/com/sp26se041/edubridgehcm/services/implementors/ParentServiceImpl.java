package com.sp26se041.edubridgehcm.services.implementors;

import com.sp26se041.edubridgehcm.models.ChatMessage;
import com.sp26se041.edubridgehcm.repositories.ChatMessageRepo;
import com.sp26se041.edubridgehcm.repositories.ParentRepo;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.ParentService;
import com.sp26se041.edubridgehcm.utils.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ParentServiceImpl implements ParentService {

    private final ChatMessageRepo chatMessageRepo;

    @Override
    public ResponseEntity<ResponseObject> createMessage(ChatMessage chatMessage) {
        chatMessageRepo.save(chatMessage);
        return ResponseBuilder.build(HttpStatus.OK, "", null);
    }
}
