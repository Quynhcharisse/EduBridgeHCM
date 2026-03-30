package com.sp26se041.edubridgehcm.validations.school;

import com.sp26se041.edubridgehcm.models.AdmissionCampaign;
import com.sp26se041.edubridgehcm.models.Campus;
import com.sp26se041.edubridgehcm.models.CampusProgramOffering;
import com.sp26se041.edubridgehcm.repositories.AdmissionCampaignRepo;
import com.sp26se041.edubridgehcm.repositories.CampusProgramOfferingRepo;
import com.sp26se041.edubridgehcm.requests.CreateAdmissionCampaignTemplateRequest;
import com.sp26se041.edubridgehcm.requests.UpdateAdmissionCampaignTemplateRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class AdmissionCampaignValidation {

    public static String validationCreateAdmissionCampaignTemplate(CreateAdmissionCampaignTemplateRequest request,
                                                                   Campus actorCampus,
                                                                   AdmissionCampaignRepo admissionCampaignRepo) {
        if (request == null) {
            return "Request is required";
        }

        if (normalize(request.getName()) == null) {
            return "Name is required";
        }

        if (normalize(request.getName()).length() > 100) {
            return "Name is too long. Maximum length is 100 characters";
        }

        if (normalize(request.getDescription()) == null) {
            return "Description is required";
        }

        if (request.getYear() <= 0) {
            return "Year is required";
        }

        // 2. Kiểm tra Năm (Year)
        if (request.getYear() < LocalDate.now().getYear()) {
            return "Cannot create a campaign for a past year";
        }

        if (admissionCampaignRepo.existsBySchoolIdAndYear(actorCampus.getSchool().getId(), request.getYear())) {
            return "A campaign template for the year already exists";
        }

        // 3. Kiểm tra Ngày tháng (Dates)
        if (request.getStartDate() == null || request.getEndDate() == null) {
            return "Start date and end date are required";
        }

        // Đồng bộ Năm và Ngày (Bạn đã làm rất tốt bước này)
        if (request.getStartDate().getYear() != request.getYear() || request.getEndDate().getYear() != request.getYear()) {
            return "Start date and end date must be within the year " + request.getYear();
        }

        // Check quá khứ cho StartDate (cho phép lùi 1 ngày)
        if (request.getStartDate().isBefore(LocalDate.now().minusDays(1))) {
            return "Start date cannot be in the past";
        }

        // Check quá khứ cho EndDate
        if (request.getEndDate().isBefore(LocalDate.now())) {
            return "End date must be in the future";
        }

        // Check mối quan hệ End - Start (Nên dùng !isAfter để bắt buộc khác ngày)
        if (!request.getEndDate().isAfter(request.getStartDate())) {
            return "End date must be after start date";
        }

        return null;
    }

    public static String validationUpdateAdmissionCampaignTemplate(UpdateAdmissionCampaignTemplateRequest request,
                                                                   AdmissionCampaign admissionCampaign,
                                                                   CampusProgramOfferingRepo campusProgramOfferingRepo) {
        if (request == null) {
            return "Request is required";
        }

        if (normalize(request.getName()) == null) {
            return "Name is required";
        }

        if (normalize(request.getName()).length() > 100) {
            return "Name is too long. Maximum length is 100 characters";
        }

        String description = normalize(request.getDescription());
        if (description == null) {
            return "Description is required";
        }

        // 2. Kiểm tra Null cho ngày tháng (BẮT BUỘC để tránh crash 500)
        if (request.getStartDate() == null || request.getEndDate() == null) {
            return "Start date and end date are required";
        }

        // 1. Lấy ngày hiện tại trong DB để so sánh
        LocalDate oldStart = admissionCampaign.getStartDate();
        // 3. Logic thời gian
        // StartDate cho phép lùi 1 ngày

        // 2. Logic StartDate: Chỉ chặn nếu người dùng THAY ĐỔI ngày bắt đầu sang một ngày quá khứ mới
        // Nếu họ giữ nguyên ngày cũ (dù là quá khứ), thì cho phép qua.
        if (!request.getStartDate().equals(oldStart)) {
            if (request.getStartDate().isBefore(LocalDate.now().minusDays(1))) {
                return "Start date cannot be in the past";
            }
        }

        // EndDate phải từ hôm nay trở đi
        if (request.getEndDate().isBefore(LocalDate.now())) {
            return "End date must be a future date";
        }

        // End phải sau Start (Dùng !isAfter để đảm bảo không trùng ngày)
        if (!request.getEndDate().isAfter(request.getStartDate())) {
            return "End date must be after start date";
        }

        List<CampusProgramOffering> offeringList = campusProgramOfferingRepo.findByAdmissionCampaignId(admissionCampaign.getId());

        Optional<CampusProgramOffering> checkOffering = offeringList.stream()
                .filter(o -> o.getOpenDate().isBefore(request.getStartDate())
                        || o.getCloseDate().isAfter(request.getEndDate()))
                .findFirst();

        return checkOffering.map(campusProgramOffering -> "Cannot update: The program offering '" + campusProgramOffering.getProgram().getCurriculum().getName() +
                "' has dates outside the new campaign range.").orElse("");

    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
