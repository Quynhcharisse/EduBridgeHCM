package com.sp26se041.edubridgehcm.validations.campus;

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

import java.time.LocalDate;

public class CampusProgramOfferingValidation {

    public static String validateCreateCampusProgramOffering(CreateCampusProgramOfferingRequest request,
                                                             Campus actorCampus,
                                                             AdmissionCampaignRepo admissionCampaignRepo,
                                                             ProgramRepo programRepo,
                                                             CampusProgramOfferingRepo campusProgramOfferingRepo, CampusRepo campusRepo) {

        if (request == null || request.getAdmissionCampaignId() == null) {
            return "Vui lòng chọn chiến dịch tuyển sinh";
        }

        if (request.getProgramId() == null) {
            return "Vui lòng chọn chương trình đào tạo";
        }

        if (request.getLearningMode() == null) {
            return "Vui lòng chọn phương thức học tập";
        }

        if (!request.getLearningMode().equals(LearningMode.BOARDING)
                && !request.getLearningMode().equals(LearningMode.DAY_SCHOOL)
                && !request.getLearningMode().equals(LearningMode.SEMI_BOARDING)
                && !request.getLearningMode().equals(LearningMode.HALF_DAY)) {
            return "Phương thức học tập đã chọn không được hỗ trợ cho suất tuyển sinh này";
        }

        AdmissionCampaign campaign = admissionCampaignRepo.findById(request.getAdmissionCampaignId()).orElse(null);
        if (campaign == null || !campaign.getSchool().getId().equals(actorCampus.getSchool().getId())) {
            return "Chiến dịch tuyển sinh không nằm trong phạm vi quản lý của trường bạn";
        }

        // Không được tạo suất tuyển sinh khi chiến dịch chưa mở
        if (!campaign.getStatus().equals(Status.OPEN_ADMISSION_CAMPAIGN)) {
            return "Suất tuyển sinh chỉ có thể được tạo khi chiến dịch đang ở trạng thái MỞ. Trạng thái hiện tại: " + campaign.getStatus();
        }

        Program program = programRepo.findByIdAndCurriculum_School_Id(request.getProgramId(), actorCampus.getSchool().getId());

        if (program == null) {
            return "Không tìm thấy chương trình đào tạo";
        }

        if (program.getStatus().equals(Status.PRO_INACTIVE)) {
            return "Chương trình đào tạo hiện không hoạt động";
        }

        if (program.getCurriculum().getCurriculumStatus() != Status.CUR_ACTIVE) {
            return "Khung chương trình của chương trình này phải ở trạng thái hoạt động";
        }

        if (campaign.getYear() != program.getCurriculum().getApplicationYear()) {
            return "Năm của chiến dịch phải khớp với năm áp dụng của khung chương trình";
        }

        Campus targetCampus = resolveTargetCampus(actorCampus, request.getCampusId(), campusRepo);

        if (targetCampus == null) {
            return "Cơ sở được chọn không thuộc phạm vi quản lý của bạn";
        }

        // Kiểm tra % điều chỉnh giá
        if (request.getPriceAdjustmentPercentage() != null) {
            if (request.getPriceAdjustmentPercentage() < -100) {
                return "Phần trăm điều chỉnh không thể làm học phí trở thành số âm";
            }
        }

        // Campus có thể chọn ngày mở/đóng riêng, nhưng vẫn phải nằm trong khung chiến dịch.
        LocalDate openDate = request.getOpenDate() != null ? request.getOpenDate() : campaign.getStartDate();
        LocalDate closeDate = request.getCloseDate() != null ? request.getCloseDate() : campaign.getEndDate();

        if (closeDate.isBefore(LocalDate.now())) {
            return "Ngày đóng nhận hồ sơ không được ở trong quá khứ";
        }

        if (closeDate.isBefore(openDate)) {
            return "Ngày kết thúc phải sau hoặc bằng ngày bắt đầu";
        }

        if (openDate.isBefore(campaign.getStartDate()) || closeDate.isAfter(campaign.getEndDate())) {
            return "Thời hạn nhận hồ sơ của suất tuyển sinh phải nằm trong khoảng thời gian của chiến dịch ("
                    + campaign.getStartDate() + " đến " + campaign.getEndDate() + ")";
        }

        if (campusProgramOfferingRepo.existsByAdmissionCampaignIdAndCampusIdAndProgramIdAndLearningMode(
                campaign.getId(), targetCampus.getId(), program.getId(), request.getLearningMode())) {
            return "Cơ sở này đã tồn tại suất tuyển sinh cho chương trình và phương thức học tập này trong chiến dịch hiện tại";
        }

        return null;
    }

    public static String validateUpdateCampusProgramOffering(UpdateCampusProgramOfferingRequest request, Campus
            actorCampus, CampusProgramOffering offering, AdmissionCampaign targetCampaign, Campus targetCampus, Program
                                                                     targetProgram, int usedQuota, Status targetApplicationStatus, Integer targetQuota, LocalDate
                                                                     targetOpenDate, LocalDate targetCloseDate, LearningMode targetLearningMode, CampusProgramOfferingRepo campusProgramOfferingRepo) {

        if (request.getId() == null || request.getId() <= 0) {
            return "Yêu cầu ID của suất tuyển sinh";
        }

        if (offering == null) {
            return "Không tìm thấy suất tuyển sinh";
        }

        if (!offering.getAdmissionCampaign().getSchool().getId().equals(actorCampus.getSchool().getId())) {
            return "Suất tuyển sinh không nằm trong phạm vi quản lý của trường bạn";
        }

        if (!actorCampus.getIsPrimaryBranch() && !offering.getCampus().getId().equals(actorCampus.getId())) {
            return "Bạn chỉ có quyền cập nhật suất tuyển sinh của cơ sở mình";
        }

        if (targetProgram == null) {
            return "Chương trình đào tạo mục tiêu không hợp lệ";
        }

        if (Status.PRO_INACTIVE.equals(targetProgram.getStatus())) {
            return "Chương trình đào tạo này đã bị tạm dừng bởi nhà trường";
        }

        if (targetProgram.getCurriculum().getCurriculumStatus() != Status.CUR_ACTIVE) {
            return "Khung chương trình của chương trình này phải ở trạng thái hoạt động";
        }

        if (targetCampaign.getStatus() == Status.CLOSED || targetCampaign.getStatus() == Status.EXPIRED) {
            return "Không thể chuyển suất tuyển sinh sang chiến dịch đã đóng hoặc đã hết hạn";
        }

        if (targetCampaign.getYear() != targetProgram.getCurriculum().getApplicationYear()) {
            return "Năm của chiến dịch phải khớp với năm áp dụng của khung chương trình";
        }

        boolean identityChanged = !targetCampaign.getId().equals(offering.getAdmissionCampaign().getId())
                || !targetCampus.getId().equals(offering.getCampus().getId())
                || !targetProgram.getId().equals(offering.getProgram().getId())
                || targetLearningMode != offering.getLearningMode();

        if (usedQuota > 0 && identityChanged) {
            return "Không thể thay đổi chiến dịch/cơ sở/chương trình/phương thức sau khi đã có hồ sơ đăng ký";
        }

        // Khi đã có hồ sơ, không cho thay đổi học phí để tránh xung đột giữa hồ sơ cũ và mới.
        if (usedQuota > 0 && request.getPriceAdjustmentPercentage() != null) {
            return "Không thể thay đổi học phí sau khi đã có hồ sơ đăng ký";
        }

        if (targetQuota == null || targetQuota <= 0) {
            return "Chỉ tiêu tuyển sinh phải lớn hơn 0";
        }

        if (targetQuota < usedQuota) {
            return "Chỉ tiêu không thể nhỏ hơn số lượng hồ sơ đã đăng ký";
        }

        if (targetOpenDate == null || targetCloseDate == null) {
            return "Ngày mở và ngày đóng nhận hồ sơ không được để trống";
        }

        if (targetCloseDate.isBefore(targetOpenDate)) {
            return "Ngày kết thúc phải sau hoặc bằng ngày bắt đầu";
        }

        if (targetOpenDate.isBefore(targetCampaign.getStartDate()) || targetCloseDate.isAfter(targetCampaign.getEndDate())) {
            return "Ngày mở/đóng suất tuyển sinh phải nằm trong phạm vi thời gian của chiến dịch";
        }

        if (targetApplicationStatus == null) {
            return "Trạng thái hồ sơ phải là MỞ, TẠM DỪNG, ĐẦY, hoặc ĐÓNG";
        }

        Status currentStatus = offering.getApplicationStatus();

        if ((currentStatus == Status.FULL || currentStatus == Status.CLOSED)
                && targetApplicationStatus != currentStatus) {
            return "Không thể chuyển trạng thái từ ĐẦY hoặc ĐÓNG sang trạng thái khác";
        }

        if (targetApplicationStatus == Status.OPEN && targetQuota == usedQuota) {
            return "Không thể đặt trạng thái MỞ khi chỉ tiêu còn lại đã hết";
        }

        if (currentStatus == Status.PAUSED && (targetApplicationStatus == Status.FULL || targetApplicationStatus == Status.CLOSED)) {
            return "Không thể chuyển trực tiếp từ TẠM DỪNG sang ĐẦY hoặc ĐÓNG";
        }

        boolean duplicatedOffering = campusProgramOfferingRepo.existsByAdmissionCampaignIdAndCampusIdAndProgramIdAndLearningModeAndIdNot(
                targetCampaign.getId(),
                targetCampus.getId(),
                targetProgram.getId(),
                targetLearningMode,
                offering.getId()
        );

        if (duplicatedOffering) {
            return "Cơ sở này đã có suất tuyển sinh tương tự trong chiến dịch này";
        }

        if (request.getPriceAdjustmentPercentage() != null && request.getPriceAdjustmentPercentage() < -100) {
            return "Phần trăm điều chỉnh không thể làm học phí trở thành số âm";
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