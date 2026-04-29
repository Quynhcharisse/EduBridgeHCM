package com.sp26se041.edubridgehcm.configurations;

import com.sp26se041.edubridgehcm.repositories.NotificationRecipientsRepo;
import com.sp26se041.edubridgehcm.repositories.NotificationsRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationCleanupTask {

    private static final int RETENTION_DAYS = 30;
    private static final int CLEANUP_BATCH_SIZE = 500;
    private static final int MAX_BATCHES_PER_RUN = 20;

    private final NotificationRecipientsRepo notificationRecipientsRepo;
    private final NotificationsRepo notificationsRepo;

    @Scheduled(cron = "0 30 0 * * *")
    @Transactional

    //Nếu trong lúc xóa 500 dòng mà gặp lỗi,
    // hệ thống sẽ rollback (hoàn tác) lại để dữ liệu không bị sai lệch.
    public void cleanupOldNotifications() {

        // giới hạn mỗi lần chạy chỉ xóa tối đa 500 dòng (CLEANUP_BATCH_SIZE)
        // chạy tối đa 20 đợt (MAX_BATCHES_PER_RUN).

        LocalDateTime cutoff = LocalDateTime.now().minusDays(RETENTION_DAYS);
        int recipientDeleted = 0;

        PageRequest batchPage = PageRequest.of(0, CLEANUP_BATCH_SIZE);

        for (int i = 0; i < MAX_BATCHES_PER_RUN; i++) {

            var recipients = notificationRecipientsRepo.findByCreatedAtBeforeOrderByIdAsc(cutoff, batchPage);

            int deleted = recipients.getNumberOfElements();

            if (deleted == 0) {
                break;
            }

            //Xóa người nhận (notificationRecipientsRepo): Xóa các bản ghi chi tiết về việc "Ai đã nhận thông báo nào" trước.
            // Điều này đảm bảo tính toàn vẹn dữ liệu, tránh lỗi khóa ngoại (Foreign Key) nếu bảng Notification có quan hệ với bảng Recipient.
            notificationRecipientsRepo.deleteAllInBatch(recipients.getContent());

            recipientDeleted += deleted;
            if (deleted < CLEANUP_BATCH_SIZE) {
                break;
            }
        }

        int notificationDeleted = 0;
        for (int i = 0; i < MAX_BATCHES_PER_RUN; i++) {

            var notifications = notificationsRepo.findByCreatedAtBeforeAndRecipientsIsEmptyOrderByIdAsc(cutoff, batchPage);

            int deleted = notifications.getNumberOfElements();

            if (deleted == 0) {
                break;
            }

            //Xóa thông báo mồ côi (notificationsRepo): Chỉ xóa các thông báo mà danh sách người nhận đã trống (RecipientsIsEmpty).
            // Điều này giúp dọn sạch những thông báo "cha" mà không còn đứa "con" nào sử dụng nữa.
            notificationsRepo.deleteAllInBatch(notifications.getContent());
            notificationDeleted += deleted;
            if (deleted < CLEANUP_BATCH_SIZE) {
                break;
            }
        }

        if (recipientDeleted > 0 || notificationDeleted > 0) {
            log.info("Notification cleanup done. recipientsDeleted={}, notificationsDeleted={}, cutoff={}",
                    recipientDeleted, notificationDeleted, cutoff);
        }
    }
}
