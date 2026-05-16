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
                continue;
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

            expiredCount++;
        }

        if (expiredCount > 0) {
            log.info("[DepositExpiry] Tổng số form hết hạn đặt cọc: {}", expiredCount);
        }
    }

    @Scheduled(cron = "0 5 1 * * *")
    @Transactional
    public void ghostExpiredForms() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        List<AdmissionReservationForm> candidates = reservationFormRepo.findByStatusIn(
                Set.of(Status.RESERVATION_DEPOSIT_EXPIRED,
                        Status.RESERVATION_APPROVAL)
        );

        int ghostCount = 0;

        for (AdmissionReservationForm form : candidates) {
            LocalDate confirmationEnd = resolveConfirmationEndDate(form);
            if (confirmationEnd == null || !today.isAfter(confirmationEnd)) {
                continue;
            }

            Status previousStatus = form.getStatus();
            form.setStatus(Status.RESERVATION_GHOST);
            reservationFormRepo.save(form);
            ghostCount++;
            log.info("[ConfirmationExpiry] Form #{} → GHOST (was {}) - campaign #{}",
                    form.getId(), previousStatus,
                    form.getAdmissionCampaign() != null ? form.getAdmissionCampaign().getId() : "N/A");
        }

        if (ghostCount > 0) {
            log.info("[ConfirmationExpiry] Tổng số form chuyển GHOST: {}", ghostCount);
        }
    }

    private LocalDate resolveDepositDeadline(AdmissionReservationForm form) {
        AdmissionCampaign campaign = form.getAdmissionCampaign();
        if (campaign == null) return null;
        if (!(campaign.getAdmissionMethodTimelines() instanceof List<?> timelines)) return null;

        for (Object item : timelines) {
            if (!(item instanceof Map<?, ?> tl)) continue;
            return AdmissionCampaignValidation.parseLocalDateSafe(tl.get("depositEndDate"));
        }
        return null;
    }

    private LocalDate resolveConfirmationEndDate(AdmissionReservationForm form) {
        AdmissionCampaign campaign = form.getAdmissionCampaign();
        if (campaign == null) return null;
        if (!(campaign.getAdmissionMethodTimelines() instanceof List<?> timelines)) return null;

        for (Object item : timelines) {
            if (!(item instanceof Map<?, ?> tl)) continue;
            return AdmissionCampaignValidation.parseLocalDateSafe(tl.get("confirmationEndDate"));
        }
        return null;
    }
}
