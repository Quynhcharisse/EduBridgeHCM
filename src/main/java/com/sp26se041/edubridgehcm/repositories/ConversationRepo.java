package com.sp26se041.edubridgehcm.repositories;

import com.sp26se041.edubridgehcm.models.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ConversationRepo extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findByParentEmailAndCounsellorEmail(String parentEmail, String counsellorEmail);
    List<Conversation> findTop20ByParentEmailOrderByUpdatedDateDesc(String parentEmail);
    List<Conversation> findTop20ByCounsellorEmailOrderByUpdatedDateDesc(String counsellorEmail);

    List<Conversation> findTop20ByParentEmailAndIdLessThanOrderByUpdatedDateDesc(String parentEmail, Long cursorId);
    List<Conversation> findTop20ByCounsellorEmailAndIdLessThanOrderByUpdatedDateDesc(String counsellorEmail, Long cursorId);



    Optional<Conversation> findById(Long id);
}
