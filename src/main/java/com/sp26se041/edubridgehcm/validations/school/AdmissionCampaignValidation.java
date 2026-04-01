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

        // Cho phép tạo chiến dịch cho năm hiện tại hoặc các năm tương lai
        if (request.getYear() < LocalDate.now().getYear()) {
            return "Cannot create a campaign for a past academic year";
        }

        // 3. Kiểm tra Ngày tháng (Dates)
        if (request.getStartDate() == null) {
            return "Start date are required";
        }

        if (request.getEndDate() == null) {
            return "End date are required";
        }

        // Check quá khứ cho StartDate (cho phép lùi 1 ngày)
        if (request.getStartDate().isBefore(LocalDate.now().minusDays(1))) {
            return "Start date cannot be in the past";
        }

        // Check quá khứ cho EndDate
        if (request.getEndDate().isBefore(LocalDate.now())) {
            return "End date must be in the future";
        }

        //chiến dịch phải diễn ra ít nhất là hơn 1 ngày
        if (!request.getEndDate().isAfter(request.getStartDate())) {
            return "End date must be after start date";
        }

        // Quy tắc: StartDate có thể nằm ở quý 4 của năm trước (n-1)
        // để bắt đầu nhận hồ sơ sớm cho năm học (n).
        // Ví dụ: Chiến dịch 2027 có thể bắt đầu từ 01/10/2026.
        if (request.getStartDate().isBefore(LocalDate.of(request.getYear() - 1, 10, 1))) {
            return "Start date is too early. Early bird for " + request.getYear() + " should start from October " + (request.getYear() - 1);
        }

        if (request.getEndDate().isBefore(LocalDate.of(request.getYear() - 1, 12, 31))) {
            return "End date is invalid. A campaign for " + request.getYear() + " must at least last until the end of " + (request.getYear() - 1);
        }

        // Quy tắc: EndDate phải kết thúc trong năm học đó để chốt sổ
        if (request.getEndDate().getYear() != request.getYear()) {
            return "End date must be within the academic year " + request.getYear();
        }

        if (admissionCampaignRepo.existsBySchoolIdAndYear(actorCampus.getSchool().getId(), request.getYear())) {
            return "A campaign template for the " + request.getYear() + "year already exists";
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
