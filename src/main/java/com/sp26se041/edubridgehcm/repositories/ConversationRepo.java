package com.sp26se041.edubridgehcm.repositories;

import com.sp26se041.edubridgehcm.models.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ConversationRepo extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findByParentEmailAndCounsellorEmail(String parentEmail, String counsellorEmail);
    
    List<Conversation> findTop20ByParentEmailAndStudentProfileIsNotNullOrderByUpdatedDateDesc(String parentEmail);
    
    List<Conversation> findTop20ByCounsellorEmailAndStudentProfileIsNotNullOrderByUpdatedDateDesc(String counsellorEmail);

    List<Conversation> findTop20ByParentEmailAndIdLessThanAndStudentProfileIsNotNullOrderByUpdatedDateDesc(String parentEmail, Long cursorId);
    
    List<Conversation> findTop20ByCounsellorEmailAndIdLessThanAndStudentProfileIsNotNullOrderByUpdatedDateDesc(String counsellorEmail, Long cursorId);

    Optional<Conversation> findById(Long id);
}
