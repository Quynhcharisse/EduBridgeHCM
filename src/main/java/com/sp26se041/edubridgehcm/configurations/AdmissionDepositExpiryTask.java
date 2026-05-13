package com.sp26se041.edubridgehcm.configurations;

import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.models.AdmissionCampaign;
import com.sp26se041.edubridgehcm.models.AdmissionReservationForm;
import com.sp26se041.edubridgehcm.repositories.AdmissionCampaignRepo;
import com.sp26se041.edubridgehcm.repositories.AdmissionReservationFormRepo;
import com.sp26se041.edubridgehcm.validations.school.AdmissionCampaignValidation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdmissionDepositExpiryTask {

    private final AdmissionReservationFormRepo reservationFormRepo;
    private final AdmissionCampaignRepo admissionCampaignRepo;

    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void expireOverdueDepositForms() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        List<AdmissionReservationForm> candidates = reservationFormRepo.findByStatusIn(
                Set.of(Status.RESERVATION_PAYMENT_PENDING,
                        Status.RESERVATION_PAYMENT_REJECTED)
        );

        int expiredCount = 0;

        for (AdmissionReservationForm form : candidates) {
            LocalDate depositDeadline = resolveDepositDeadline(form);
            if (depositDeadline == null || !today.isAfter(depositDeadline)) {
                continue; // Chưa hết hạn
            }

            Status previousStatus = form.getStatus();

            // 1. Đổi status form
            form.setStatus(Status.RESERVATION_DEPOSIT_EXPIRED);
            reservationFormRepo.save(form);

            // 2. Hoàn quota campaign
            AdmissionCampaign campaign = form.getAdmissionCampaign();
            if (campaign != null && campaign.getRemainingQuota() != null) {
                campaign.setRemainingQuota(campaign.getRemainingQuota() + 1);
                admissionCampaignRepo.save(campaign);
            }

            //Offering quota KHÔNG restore ở đây vì:
            //OFFERING_SELECTED: offering quota chưa bị trừ
            //PAYMENT_PENDING  : offering quota chưa bị trừ
            //Offering quota chỉ bị trừ khi campus CONFIRM (→ DEPOSITED)
            //Nếu cần handle DEPOSITED expire thì xử lý riêng
            expiredCount++;
            log.info("[DepositExpiry] Form #{} expired (was {}) - campaign #{} quota restored",
                    form.getId(), previousStatus, campaign != null ? campaign.getId() : "N/A");
        }

        if (expiredCount > 0) {
            log.info("[DepositExpiry] Tổng số form hết hạn đặt cọc: {}", expiredCount);
        }
    }

    private LocalDate resolveDepositDeadline(AdmissionReservationForm form) {
        AdmissionCampaign campaign = form.getAdmissionCampaign();
        if (campaign == null || form.getMethodName() == null) return null;
        if (!(campaign.getAdmissionMethodTimelines() instanceof List<?> timelines)) return null;

        for (Object item : timelines) {
            if (!(item instanceof Map<?, ?> tl)) continue;
            String methodCode = Objects.toString(tl.get("methodCode"), null);
            if (!form.getMethodName().equalsIgnoreCase(methodCode)) continue;
            return AdmissionCampaignValidation.parseLocalDateSafe(tl.get("depositEndDate"));
        }
        return null;
    }
}
