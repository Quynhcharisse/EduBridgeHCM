package com.sp26se041.edubridgehcm.validations.school;

import com.sp26se041.edubridgehcm.enums.LearningMode;
import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.models.AdmissionCampaign;
import com.sp26se041.edubridgehcm.models.Campus;
import com.sp26se041.edubridgehcm.models.CampusProgramOffering;
import com.sp26se041.edubridgehcm.models.Program;
import com.sp26se041.edubridgehcm.repositories.AdmissionCampaignRepo;
import com.sp26se041.edubridgehcm.repositories.CampusProgramOfferingRepo;
import com.sp26se041.edubridgehcm.repositories.CampusRepo;
import com.sp26se041.edubridgehcm.repositories.ProgramRepo;
import com.sp26se041.edubridgehcm.requests.CreateCampusProgramOfferingRequest;
import com.sp26se041.edubridgehcm.requests.UpdateCampusProgramOfferingRequest;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CampusProgramOfferingValidation {

    public static String validateCreateCampusProgramOffering(CreateCampusProgramOfferingRequest request,
                                                             Campus actorCampus,
                                                             AdmissionCampaignRepo admissionCampaignRepo,
                                                             ProgramRepo programRepo,
                                                             CampusProgramOfferingRepo campusProgramOfferingRepo, CampusRepo campusRepo) {

        if (request == null || request.getAdmissionCampaignId() == null) {
            return "Campaign are required";
        }

        if (request.getProgramId() == null) {
            return "Program are required";
        }

        if (request.getLearningMode() == null) {
            return "Learning mode are required";
        }

        if (!request.getLearningMode().equals(LearningMode.BOARDING)
                && !request.getLearningMode().equals(LearningMode.DAY_SCHOOL)
                && !request.getLearningMode().equals(LearningMode.SEMI_BOARDING)
                && !request.getLearningMode().equals(LearningMode.HALF_DAY)) {
            return "Selected learning mode is not supported for this offering";
        }


        if (request.getQuota() <= 0) {
            return "Quota are required";
        }

        AdmissionCampaign campaign = admissionCampaignRepo.findById(request.getAdmissionCampaignId()).orElse(null);
        if (campaign == null || !campaign.getSchool().getId().equals(actorCampus.getSchool().getId())) {
            return "Campaign is out of your school scope";
        }

        // ko đc create campusProgramOffering khi chưa open
        if (!campaign.getStatus().equals(Status.OPEN_ADMISSION_CAMPAIGN)) {
            return "Offering can only be created when the campaign is officially OPEN. Current status: " + campaign.getStatus();
        }

        Program program = programRepo.findByIdAndCurriculum_School_Id(request.getProgramId(), actorCampus.getSchool().getId());

        if (program == null) {
            return "Program not found";
        }

        if (!program.getStatus().equals(Status.PRO_INACTIVE)) {
            return "Program is inactive";
        }

        if (program.getCurriculum().getCurriculumStatus() != Status.CUR_ACTIVE) {
            return "Program curriculum must be active";
        }

        if (campaign.getYear() != program.getCurriculum().getEnrollmentYear()) {
            return "Campaign year must match curriculum enrollment year";
        }

        if (program.getBaseTuitionFee() == null) {
            return "The selected program does not have a base tuition fee defined by the primary campus";
        }


        Campus targetCampus = resolveTargetCampus(actorCampus, request.getCampusId(), campusRepo);

        if (targetCampus == null) {
            return "Campus is out of your scope";
        }

        // 6. Check % Adjustment hợp lệ (Ví dụ: không giảm quá 100%)
        if (request.getPriceAdjustmentPercentage() != null) {
            if (request.getPriceAdjustmentPercentage() < -100) {
                return "Price adjustment cannot result in a negative tuition fee";
            }
        }

        LocalDate openDate = request.getOpenDate() != null ? request.getOpenDate() : campaign.getStartDate();
        LocalDate closeDate = request.getCloseDate() != null ? request.getCloseDate() : campaign.getEndDate();

        if (closeDate.isBefore(LocalDate.now())) {
            return "The offering's closing date cannot be in the past";
        }

        if (closeDate.isBefore(openDate)) {
            return "Close date must be after or equal to open date";
        }

        if (openDate.isBefore(campaign.getStartDate()) || closeDate.isAfter(campaign.getEndDate())) {
            return "Offering registration period must be within the campaign timeframe ("
                    + campaign.getStartDate() + " to " + campaign.getEndDate() + ")";
        }

        if (campusProgramOfferingRepo.existsByAdmissionCampaignIdAndCampusIdAndProgramIdAndLearningMode(
                campaign.getId(), targetCampus.getId(), program.getId(), request.getLearningMode())) {
            return "This campus already has the same program offering for this mode in this campaign";
        }

        return null;
    }

    public static String validateUpdateCampusProgramOffering(UpdateCampusProgramOfferingRequest request, Campus
            actorCampus, CampusProgramOffering offering, AdmissionCampaign targetCampaign, Campus targetCampus, Program
                                                                     targetProgram, int usedQuota, Status targetApplicationStatus, Integer targetQuota, LocalDate
                                                                     targetOpenDate, LocalDate targetCloseDate, LearningMode targetLearningMode, CampusProgramOfferingRepo campusProgramOfferingRepo) {

        if (request.getId() == null || request.getId() <= 0) {
            return "Offering id is required";
        }

        if (offering == null) {
            return "Offering not found";
        }

        if (!offering.getAdmissionCampaign().getSchool().getId().equals(actorCampus.getSchool().getId())) {
            return "Offering is out of your school scope";
        }

        if (!actorCampus.getIsPrimaryBranch() && !offering.getCampus().getId().equals(actorCampus.getId())) {
            return "You can only update your campus offering";
        }

        if (targetProgram == null) {
            return "Target program is invalid";
        }

        // Nếu status LÀ INACTIVE
        if (Status.PRO_INACTIVE.equals(targetProgram.getStatus())) {
            return "This program has been inactivated by the school.";
        }

        if (targetProgram.getCurriculum().getCurriculumStatus() != Status.CUR_ACTIVE) {
            return "Program curriculum must be active";
        }

        if (targetCampaign.getStatus() == Status.CLOSED || targetCampaign.getStatus() == Status.EXPIRED) {
            return "Cannot move offering to closed/expired campaign";
        }

        if (targetCampaign.getYear() != targetProgram.getCurriculum().getEnrollmentYear()) {
            return "Campaign year must match curriculum enrollment year";
        }

        boolean identityChanged = !targetCampaign.getId().equals(offering.getAdmissionCampaign().getId())
                || !targetCampus.getId().equals(offering.getCampus().getId())
                || !targetProgram.getId().equals(offering.getProgram().getId())
                || targetLearningMode != offering.getLearningMode();

        if (usedQuota > 0 && identityChanged) {
            return "Cannot change campaign/campus/program/mode after applications have been received";
        }

        if (targetQuota == null || targetQuota <= 0) {
            return "Quota must be greater than 0";
        }

        if (targetQuota < usedQuota) {
            return "Quota cannot be smaller than registered quantity";
        }

        if (targetOpenDate == null || targetCloseDate == null) {
            return "Open date and close date are required";
        }

        if (targetCloseDate.isBefore(targetOpenDate)) {
            return "Close date must be after or equal to open date";
        }

        if (targetOpenDate.isBefore(targetCampaign.getStartDate()) || targetCloseDate.isAfter(targetCampaign.getEndDate())) {
            return "Offering open/close date must be within campaign date range";
        }

        if (targetApplicationStatus == null) {
            return "Application status must be OPEN, PAUSED, FULL, or CLOSED";
        }
        // Không cho phép chuyển từ FULL hoặc CLOSED về trạng thái khác
        Status currentStatus = offering.getApplicationStatus();

        if ((currentStatus == Status.FULL || currentStatus == Status.CLOSED)
                && targetApplicationStatus != currentStatus) {
            return "Cannot change status from FULL or CLOSED to another status";
        }

        // Không cho phép chuyển về OPEN nếu quota đã đủ
        if (targetApplicationStatus == Status.OPEN && targetQuota == usedQuota) {
            return "Cannot set OPEN status when remaining quota is zero";
        }

        // Chỉ cho phép chuyển trạng thái theo luồng hợp lệ
        if (currentStatus == Status.PAUSED && (targetApplicationStatus == Status.FULL || targetApplicationStatus == Status.CLOSED)) {
            return "Cannot set status to FULL or CLOSED from PAUSED directly";
        }

        boolean duplicatedOffering = campusProgramOfferingRepo.existsByAdmissionCampaignIdAndCampusIdAndProgramIdAndLearningModeAndIdNot(
                targetCampaign.getId(),
                targetCampus.getId(),
                targetProgram.getId(),
                targetLearningMode,
                offering.getId()
        );

        if (duplicatedOffering) {
            return "This campus already has the same program offering in this campaign";
        }

        BigDecimal targetTuition = request.getTuitionFee() != null ? request.getTuitionFee() : offering.getTuitionFee();

        if (targetTuition.signum() < 0) {
            return "Tuition fee must be >= 0";
        }

        return null;
    }

    public static Campus resolveTargetCampus(Campus actorCampus, Integer requestedCampusId, CampusRepo campusRepo) {
        if (!actorCampus.getIsPrimaryBranch()) {
            if (requestedCampusId != null && !requestedCampusId.equals(actorCampus.getId())) {
                return null;
            }
            return actorCampus;
        }

        Integer targetCampusId = requestedCampusId == null ? actorCampus.getId() : requestedCampusId;
        return campusRepo.findByIdAndSchoolId(targetCampusId, actorCampus.getSchool().getId()).orElse(null);
    }
}
