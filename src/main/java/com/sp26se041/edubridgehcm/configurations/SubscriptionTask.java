package com.sp26se041.edubridgehcm.configurations;

import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.models.Subscription;
import com.sp26se041.edubridgehcm.repositories.SchoolSubscriptionRepo;
import com.sp26se041.edubridgehcm.repositories.SubscriptionRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionTask {

    private final SubscriptionRepo subscriptionRepo;

    private final SchoolSubscriptionRepo schoolSubscriptionRepo;


    // Chạy vào lúc 00:00 mỗi ngày để quét các gói PENDING_DEACTIVE
    // Cron expression: "0 0 0 * * *" (Giây - Phút - Giờ - Ngày - Tháng - Thứ)
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void cleanupPendingDeactivePackages() {
        log.info("Starting cleanup of PENDING_DEACTIVE packages at {}", LocalDateTime.now());

        //Tìm tất cả các gói đang ở trạng thái chờ Deactive
        List<Subscription> pendingPackages = subscriptionRepo.findAllByPackageStatus(Status.PACKAGE_INACTIVE_PENDING);

        for (Subscription sub : pendingPackages) {
            //Kiểm tra xem còn School nào đang dùng gói này và chưa hết hạn không
            boolean hasActiveSchools = schoolSubscriptionRepo.existsBySubscriptionAndEndDateAfter(
                    sub,
                    LocalDate.now()
            );

            //Nếu không còn ai dùng (hoặc tất cả đã quá ngày endDate)
            if (!hasActiveSchools) {
                sub.setPackageStatus(Status.PACKAGE_DEACTIVATED);
                subscriptionRepo.save(sub);
                log.info("Package ID: {} has been fully DEACTIVATED because all subscriptions expired.", sub.getId());
            }
        }
    }
}
