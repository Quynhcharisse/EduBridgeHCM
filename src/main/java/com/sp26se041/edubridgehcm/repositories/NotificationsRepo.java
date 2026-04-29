package com.sp26se041.edubridgehcm.repositories;

import com.sp26se041.edubridgehcm.models.Notifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface NotificationsRepo extends JpaRepository<Notifications, Integer> {

    Page<Notifications> findByCreatedAtBeforeAndRecipientsIsEmptyOrderByIdAsc(LocalDateTime cutoff, Pageable pageable);
}
