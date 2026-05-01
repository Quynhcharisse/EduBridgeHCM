package com.sp26se041.edubridgehcm.utils;

import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.models.Subscription;
import com.sp26se041.edubridgehcm.repositories.SchoolSubscriptionRepo;
import com.sp26se041.edubridgehcm.repositories.SubscriptionRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SubscriptionPriceWorker {

    private final SubscriptionRepo subscriptionRepo;
    private final SchoolSubscriptionRepo schoolSubscriptionRepo;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional

    public void processPriceActivation() {
        //Tìm các gói đang ở trạng thái chờ hủy (PENDING_DEACTIVE)
        List<Subscription> pendingSubs = subscriptionRepo.findAllByPackageStatus(Status.PACKAGE_INACTIVE_PENDING);

        for (Subscription sub : pendingSubs) {
            //Kiểm tra xem còn trường nào có hạn dùng (endDate >= hôm nay) không
            boolean stillInUse = schoolSubscriptionRepo.existsBySubscriptionAndEndDateAfter(sub, LocalDate.now());

            if (!stillInUse) {
                //Nếu không còn ai dùng nữa -> Chuyển sang trạng thái chết hẳn
                sub.setPackageStatus(Status.PACKAGE_DEACTIVATED);
                subscriptionRepo.save(sub);
                System.out.println("Gói " + sub.getName() + " đã hết khách dùng và được tạm dừng hoàn toàn.");
            }
        }
    }
}
