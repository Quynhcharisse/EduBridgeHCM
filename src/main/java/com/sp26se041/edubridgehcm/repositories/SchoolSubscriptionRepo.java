package com.sp26se041.edubridgehcm.repositories;

import com.sp26se041.edubridgehcm.models.SchoolSubscription;
import com.sp26se041.edubridgehcm.models.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SchoolSubscriptionRepo extends JpaRepository<SchoolSubscription, Integer> {

    boolean existsBySubscriptionAndEndDateAfter(Subscription subscription, LocalDate now);

    Optional<SchoolSubscription> findBySchoolIdAndEndDateGreaterThanEqualAndIsSelectedTrue(
            Integer schoolId,
            LocalDate endDate
    );

    List<SchoolSubscription> findBySchoolIdAndIsSelected(Integer schoolId, boolean selected);

    // 1. Dành cho Admin: Lấy toàn bộ gói đang có hiệu lực trên toàn hệ thống
    List<SchoolSubscription> findAllByIsSelectedTrueAndEndDateGreaterThanEqualOrderByEndDateAsc(LocalDate today);

    // 4. Tìm các gói đã hết hạn (Dành cho báo cáo thống kê)
    List<SchoolSubscription> findAllByEndDateBefore(LocalDate day);

    Optional<SchoolSubscription> findTopBySchoolIdAndIdNotOrderByEndDateDesc(Integer schoolId, Integer schoolSubscriptionId);
}
