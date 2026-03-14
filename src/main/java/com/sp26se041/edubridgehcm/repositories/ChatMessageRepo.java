package com.sp26se041.edubridgehcm.repositories;

import com.sp26se041.edubridgehcm.models.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepo extends JpaRepository<ChatMessage, Long> {
}
