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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

        // Cho phép tạo chiến dịch cho năm hiện tại hoặc các năm tương lai
        if (request.getYear() < LocalDate.now().getYear()) {
            return "Không thể tạo chiến dịch cho một năm học trong quá khứ";
        }

        // 3. Kiểm tra Ngày tháng (Dates)
        if (request.getStartDate() == null) {
            return "Ngày bắt đầu không được để trống";
        }

        if (request.getEndDate() == null) {
            return "Ngày kết thúc không được để trống";
        }

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

        // Quy tắc: EndDate phải kết thúc trong năm học đó để chốt sổ
        if (request.getEndDate().getYear() != request.getYear()) {
            return "Ngày kết thúc phải nằm trong năm học " + request.getYear();
        }

        if (admissionCampaignRepo.existsByYearAndSchoolIdAndStatusIn(actorCampus.getSchool().getId(), request.getYear(), List.of(Status.OPEN_ADMISSION_CAMPAIGN))) {
            return "Mẫu chiến dịch cho năm học " + request.getYear() + " đã tồn tại";
        }

        String timelineError = validateMethodTimelines(request.getAdmissionMethodTimelines(), request.getStartDate(), request.getEndDate());
        if (timelineError != null) {
            return timelineError;
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

        String timelineError = validateMethodTimelines(request.getAdmissionMethodTimelines(), request.getStartDate(), request.getEndDate());
        if (timelineError != null) {
            return timelineError;
        }

        return null;
    }

    // chỉ validate thông tin cơ bản khi clone
    // ko check dates vì user sẽ chỉnh sau
    public static String validationCloneDraftCampaign(CreateAdmissionCampaignTemplateRequest request) {

        if (request == null) {
            return "Dữ liệu yêu cầu không được để trống";
        }

        if (normalize(request.getName()) == null) {
            return "Tên chiến dịch không được để trống";
        }

        if (normalize(request.getName()).length() > 100) {
            return "Tên chiến dịch quá dài. Độ dài tối đa là 100 ký tự";
        }

        if (request.getYear() <= 0) {
            return "Năm học không được hợp lệ";
        }
        return null;
    }

    //validate kĩ khi Publish
    // —> mọi điều kiện phải đủ trước khi mở cho học sinh đăng ký
    public static String validationPublishCampaign(AdmissionCampaign campaign, Integer systemQuota) {

        if (campaign.getEndDate().isBefore(LocalDate.now())) {
            return "Không thể công bố chiến dịch đã hết hạn. Vui lòng cập nhật ngày kết thúc";
        }

        if (campaign.getStartDate().isBefore(LocalDate.of(campaign.getYear() - 1, 10, 1))) {
            return "Ngày bắt đầu không hợp lệ cho chiến dịch năm " + campaign.getYear();
        }

        if (campaign.getEndDate().getYear() != campaign.getYear()) {
            return "Ngày kết thúc phải nằm trong năm học " + campaign.getYear();
        }

        if (!(campaign.getAdmissionMethodTimelines() instanceof List<?> timelines) || timelines.isEmpty()) {
            return "Chiến dịch phải có ít nhất 1 phương thức tuyển sinh trước khi công bố";
        }

        if (systemQuota != null) {
            int totalMethodQuota = sumMethodQuotas(
                    timelines.stream()
                            .filter(t -> t instanceof Map)
                            .map(t -> (Map<String, Object>) t)
                            .toList()
            );

            if (totalMethodQuota > systemQuota) {
                return "Tổng chỉ tiêu các phương thức (" + totalMethodQuota + ") vượt quá chỉ tiêu được cấp (" + systemQuota + ")";
            }

            if (totalMethodQuota < systemQuota) {
                return "Tổng chỉ tiêu các phương thức (" + totalMethodQuota + ") chưa bằng chỉ tiêu được cấp (" + systemQuota + "). Vui lòng phân bổ đủ chỉ tiêu trước khi công bố";
            }
        }
        return null;
    }

    private static String normalize(String value) {
        return CampusValidation.normalize(value);
    }

    private static Campus extractActorCampus() {
        Account account = AuthRequestUtil.extractAuthenticatedAccount();
        if (account == null || account.getRole() != Role.SCHOOL) {
            return null;
        }
        return account.getCampus();
    }

    private static String validateMethodTimelines(
            List<CreateAdmissionCampaignTemplateRequest.AdmissionMethodTimelineRequest> timelines,
            LocalDate campaignStart,
            LocalDate campaignEnd) {
        if (timelines == null || timelines.isEmpty()) {
            return "Chiến dịch phải có ít nhất 1 phương thức tuyển sinh";
        }

        Set<String> seenCodes = new HashSet<>();
        for (CreateAdmissionCampaignTemplateRequest.AdmissionMethodTimelineRequest entry : timelines) {
            if (entry == null) {
                return "Phương thức tuyển sinh không hợp lệ";
            }

            String code = entry.getMethodCode();
            if (code == null || code.trim().isEmpty()) {
                return "Mã phương thức tuyển sinh không được để trống";
            }

            if (!seenCodes.add(code.trim().toLowerCase())) {
                return "Phương thức tuyển sinh bị trùng: " + code.trim();
            }

            if (entry.getStartDate() == null) {
                return "Ngày bắt đầu của phương thức '" + code.trim() + "' không được để trống";
            }

            if (entry.getEndDate() == null) {
                return "Ngày kết thúc của phương thức '" + code.trim() + "' không được để trống";
            }

            if (!entry.getEndDate().isAfter(entry.getStartDate())) {
                return "Ngày kết thúc của phương thức '" + code.trim() + "' phải sau ngày bắt đầu";
            }

            if (campaignStart != null && entry.getStartDate().isBefore(campaignStart)) {
                return "Ngày bắt đầu của phương thức '" + code.trim() + "' không được trước ngày bắt đầu chiến dịch";
            }

            if (campaignEnd != null && entry.getEndDate().isAfter(campaignEnd)) {
                return "Ngày kết thúc của phương thức '" + code.trim() + "' không được sau ngày kết thúc chiến dịch";
            }

            if (entry.getQuota() == null || entry.getQuota() <= 0) {
                return "Chỉ tiêu của phương thức '" + code.trim() + "' phải lớn hơn 0";
            }
        }

        return null;
    }

    public static LocalDate parseLocalDateSafe(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        // Jackson serialize LocalDate thành array [year, month, day] khi lưu vào JSONB
        if (value instanceof List<?> list && list.size() == 3) {
            try {
                int year  = ((Number) list.get(0)).intValue();
                int month = ((Number) list.get(1)).intValue();
                int day   = ((Number) list.get(2)).intValue();
                return LocalDate.of(year, month, day);
            } catch (Exception ignored) {
            }
        }
        try {
            return LocalDate.parse(String.valueOf(value));
        } catch (Exception ignored) {
            return null;
        }
    }

    public static Boolean parseBooleanSafe(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean b) {
            return b;
        }
        String s = String.valueOf(value).trim();
        if (s.equalsIgnoreCase("true")) {
            return Boolean.TRUE;
        }
        if (s.equalsIgnoreCase("false")) {
            return Boolean.FALSE;
        }
        return null;
    }

    public static Integer parseIntegerSafe(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Integer i) {
            return i;
        }
        if (value instanceof Number n) {
            return n.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value).trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    public static int sumMethodQuotas(List<Map<String, Object>> timelines) {
        return timelines.stream()
                .mapToInt(t -> t.get("quota") instanceof Number n ? n.intValue() : 0)
                .sum();
    }
}
