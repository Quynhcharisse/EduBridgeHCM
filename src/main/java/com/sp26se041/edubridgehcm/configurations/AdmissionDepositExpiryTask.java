package com.sp26se041.edubridgehcm.configurations;

import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.models.AdmissionCampaign;
import com.sp26se041.edubridgehcm.models.AdmissionReservationForm;
import com.sp26se041.edubridgehcm.models.CampusProgramOffering;
import com.sp26se041.edubridgehcm.repositories.AdmissionCampaignRepo;
import com.sp26se041.edubridgehcm.repositories.AdmissionReservationFormRepo;
import com.sp26se041.edubridgehcm.repositories.CampusProgramOfferingRepo;
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
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdmissionDepositExpiryTask {

    private final AdmissionReservationFormRepo reservationFormRepo;
    private final AdmissionCampaignRepo admissionCampaignRepo;
    private final CampusProgramOfferingRepo campusProgramOfferingRepo;

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

            form.setStatus(Status.RESERVATION_DEPOSIT_EXPIRED);
            reservationFormRepo.save(form);

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

        // Bug 2 fix: thêm RESERVATION_DEPOSITED — phụ huynh đã đóng tiền nhưng không chốt trường trước confirmationEndDate
        List<AdmissionReservationForm> candidates = reservationFormRepo.findByStatusIn(
                Set.of(Status.RESERVATION_DEPOSIT_EXPIRED,
                        Status.RESERVATION_APPROVAL,
                        Status.RESERVATION_DEPOSITED)
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

            if (previousStatus == Status.RESERVATION_DEPOSITED) {
                CampusProgramOffering offering = form.getCampusProgramOffering();
                if (offering != null) {
                    offering.setRemainingQuota(offering.getRemainingQuota() + 1);
                    campusProgramOfferingRepo.save(offering);
                }

                AdmissionCampaign campaign = form.getAdmissionCampaign();
                if (campaign != null && campaign.getRemainingQuota() != null) {
                    campaign.setRemainingQuota(campaign.getRemainingQuota() + 1);
                    admissionCampaignRepo.save(campaign);
                }
            }

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

        String methodCode = form.getCampusProgramOffering() != null
                ? form.getCampusProgramOffering().getAdmissionMethod()
                : null;

        LocalDate fallback = null;
        for (Object item : timelines) {
            if (!(item instanceof Map<?, ?> tl)) continue;
            if (fallback == null) {
                fallback = AdmissionCampaignValidation.parseLocalDateSafe(tl.get("depositEndDate"));
            }
            if (methodCode != null && methodCode.equals(tl.get("methodCode"))) {
                return AdmissionCampaignValidation.parseLocalDateSafe(tl.get("depositEndDate"));
            }
        }
        return fallback;
    }

    private LocalDate resolveConfirmationEndDate(AdmissionReservationForm form) {
        AdmissionCampaign campaign = form.getAdmissionCampaign();
        if (campaign == null) return null;
        if (!(campaign.getAdmissionMethodTimelines() instanceof List<?> timelines)) return null;

        String methodCode = form.getCampusProgramOffering() != null
                ? form.getCampusProgramOffering().getAdmissionMethod()
                : null;

        LocalDate fallback = null;
        for (Object item : timelines) {
            if (!(item instanceof Map<?, ?> tl)) continue;
            if (fallback == null) {
                fallback = AdmissionCampaignValidation.parseLocalDateSafe(tl.get("confirmationEndDate"));
            }
            if (methodCode != null && methodCode.equals(tl.get("methodCode"))) {
                return AdmissionCampaignValidation.parseLocalDateSafe(tl.get("confirmationEndDate"));
            }
        }
        return fallback;
    }
}
