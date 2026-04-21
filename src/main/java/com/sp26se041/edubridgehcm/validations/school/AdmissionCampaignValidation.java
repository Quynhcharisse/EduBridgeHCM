package com.sp26se041.edubridgehcm.validations.school;

import com.sp26se041.edubridgehcm.enums.Role;
import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.models.Account;
import com.sp26se041.edubridgehcm.models.AdmissionCampaign;
import com.sp26se041.edubridgehcm.models.Campus;
import com.sp26se041.edubridgehcm.repositories.AdmissionCampaignRepo;
import com.sp26se041.edubridgehcm.requests.CreateAdmissionCampaignTemplateRequest;
import com.sp26se041.edubridgehcm.requests.UpdateAdmissionCampaignTemplateRequest;
import com.sp26se041.edubridgehcm.utils.AuthRequestUtil;

import java.time.LocalDate;
import java.util.List;

public class AdmissionCampaignValidation {

    public static String validationCreateAdmissionCampaignTemplate(CreateAdmissionCampaignTemplateRequest request,
                                                                   Campus actorCampus,
                                                                   AdmissionCampaignRepo admissionCampaignRepo) {
        if (request == null) {
            return "Dữ liệu yêu cầu không được để trống";
        }

        if (normalize(request.getName()) == null) {
            return "Tên chiến dịch không được để trống";
        }

        if (normalize(request.getName()).length() > 100) {
            return "Tên chiến dịch quá dài. Độ dài tối đa là 100 ký tự";
        }

        if (normalize(request.getDescription()) == null) {
            return "Mô tả chiến dịch không được để trống";
        }

        if (request.getYear() <= 0) {
            return "Năm học không được để trống";
        }

//        // Cho phép tạo chiến dịch cho năm hiện tại hoặc các năm tương lai
//        if (request.getYear() < LocalDate.now().getYear()) {
//            return "Không thể tạo chiến dịch cho một năm học trong quá khứ";
//        }
//
//        // 3. Kiểm tra Ngày tháng (Dates)
//        if (request.getStartDate() == null) {
//            return "Ngày bắt đầu không được để trống";
//        }
//
//        if (request.getEndDate() == null) {
//            return "Ngày kết thúc không được để trống";
//        }

        // Check quá khứ cho StartDate (cho phép lùi 1 ngày)
        if (request.getStartDate().isBefore(LocalDate.now().minusDays(1))) {
            return "Ngày bắt đầu không được ở trong quá khứ";
        }

        // Check quá khứ cho EndDate
        if (request.getEndDate().isBefore(LocalDate.now())) {
            return "Ngày kết thúc phải ở trong tương lai";
        }

        //chiến dịch phải diễn ra ít nhất là hơn 1 ngày
        if (!request.getEndDate().isAfter(request.getStartDate())) {
            return "Ngày kết thúc phải sau ngày bắt đầu";
        }

        // Quy tắc: StartDate có thể nằm ở quý 4 của năm trước (n-1)
        // để bắt đầu nhận hồ sơ sớm cho năm học (n).
        // Ví dụ: Chiến dịch 2027 có thể bắt đầu từ 01/10/2026.
        if (request.getStartDate().isBefore(LocalDate.of(request.getYear() - 1, 10, 1))) {
            return "Ngày bắt đầu quá sớm. Đợt tuyển sinh sớm cho năm " + request.getYear() + " nên bắt đầu từ tháng 10 năm " + (request.getYear() - 1);
        }

        if (request.getEndDate().isBefore(LocalDate.of(request.getYear() - 1, 12, 31))) {
            return "Ngày kết thúc không hợp lệ. Chiến dịch cho năm " + request.getYear() + " phải kéo dài ít nhất đến hết năm " + (request.getYear() - 1);
        }

        // Quy tắc: EndDate phải kết thúc trong năm học đó để chốt sổ
        if (request.getEndDate().getYear() != request.getYear()) {
            return "Ngày kết thúc phải nằm trong năm học " + request.getYear();
        }

        if (admissionCampaignRepo.existsByYearAndSchoolIdAndStatusIn(actorCampus.getSchool().getId(), request.getYear(), List.of(Status.OPEN_ADMISSION_CAMPAIGN))) {
            return "Mẫu chiến dịch cho năm học " + request.getYear() + " đã tồn tại";
        }

        return null;
    }

    public static String validationUpdateAdmissionCampaignTemplate(UpdateAdmissionCampaignTemplateRequest request, AdmissionCampaignRepo admissionCampaignRepo) {

        Campus actorCampus = extractActorCampus();

        if (actorCampus == null) {
            return "Không tìm thấy tài khoản cơ sở trường học";
        }

        if (request == null) {
            return "Dữ liệu yêu cầu không được để trống";
        }

        if (normalize(request.getName()) == null) {
            return "Tên chiến dịch không được để trống";
        }

        if (normalize(request.getName()).length() > 100) {
            return "Tên chiến dịch quá dài. Độ dài tối đa là 100 ký tự";
        }

        if (normalize(request.getDescription()) == null) {
            return "Mô tả chiến dịch không được để trống";
        }

        if (request.getYear() <= 0) {
            return "Năm học không được để trống";
        }

        if (request.getYear() < LocalDate.now().getYear()) {
            return "Không thể cập nhật chiến dịch cho một năm học trong quá khứ";
        }

        if (request.getStartDate() == null) {
            return "Ngày bắt đầu không được để trống";
        }

        if (request.getEndDate() == null) {
            return "Ngày kết thúc không được để trống";
        }

        if (request.getStartDate().isBefore(LocalDate.now().minusDays(1))) {
            return "Ngày bắt đầu không được ở trong quá khứ";
        }

        if (request.getEndDate().isBefore(LocalDate.now())) {
            return "Ngày kết thúc phải ở trong tương lai";
        }

        if (!request.getEndDate().isAfter(request.getStartDate())) {
            return "Ngày kết thúc phải sau ngày bắt đầu";
        }

        if (request.getStartDate().isBefore(LocalDate.of(request.getYear() - 1, 10, 1))) {
            return "Ngày bắt đầu quá sớm. Đợt tuyển sinh sớm cho năm " + request.getYear() + " nên bắt đầu từ tháng 10 năm " + (request.getYear() - 1);
        }

        if (request.getEndDate().isBefore(LocalDate.of(request.getYear() - 1, 12, 31))) {
            return "Ngày kết thúc không hợp lệ. Chiến dịch cho năm " + request.getYear() + " phải kéo dài ít nhất đến hết năm " + (request.getYear() - 1);
        }

        if (request.getEndDate().getYear() != request.getYear()) {
            return "Ngày kết thúc phải nằm trong năm học " + request.getYear();
        }

        List<AdmissionCampaign> existingCampaigns = admissionCampaignRepo
                .findAllBySchoolIdAndYearAndIdNot(actorCampus.getSchool().getId(), request.getYear(), request.getAdmissionCampaignTemplateId());

        for (AdmissionCampaign other : existingCampaigns) {
            if (other.getStatus() == Status.OPEN_ADMISSION_CAMPAIGN) {
                return "Năm học " + request.getYear() + " đã có một chiến dịch đang MỞ (ID: " + other.getId() + "). Không thể có nhiều chiến dịch hoạt động cùng lúc.";
            }
        }

        return null;
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static Campus extractActorCampus() {
        Account account = AuthRequestUtil.extractAuthenticatedAccount();
        if (account == null || account.getRole() != Role.SCHOOL) {
            return null;
        }
        return account.getCampus();
    }
}
