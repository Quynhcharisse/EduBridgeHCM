package com.sp26se041.edubridgehcm.repositories;

import com.sp26se041.edubridgehcm.models.NotificationRecipients;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface NotificationRecipientsRepo extends JpaRepository<NotificationRecipients, Integer> {

    Page<NotificationRecipients> findByRecipientUserIdOrderByCreatedAtDesc(Integer recipientUserId, Pageable pageable);

    long countByRecipientUserIdAndIsReadFalse(Integer recipientUserId);

    Page<NotificationRecipients> findByCreatedAtBeforeOrderByIdAsc(LocalDateTime cutoff, Pageable pageable);
}
