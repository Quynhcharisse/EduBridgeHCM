package com.sp26se041.edubridgehcm.repositories;

import com.sp26se041.edubridgehcm.models.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConversationRepo extends JpaRepository<Conversation, Long> {
    Optional<Conversation> findByParentEmailAndCounsellorEmail(String parentEmail, String counsellorEmail);
   Optional<Conversation> findById(Long id);
}
