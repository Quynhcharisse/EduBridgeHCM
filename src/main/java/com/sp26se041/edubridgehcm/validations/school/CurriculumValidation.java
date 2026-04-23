package com.sp26se041.edubridgehcm.validations.school;

import com.sp26se041.edubridgehcm.enums.CurriculumType;
import com.sp26se041.edubridgehcm.enums.LearningMethod;
import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.models.Curriculum;
import com.sp26se041.edubridgehcm.repositories.CurriculumRepo;
import com.sp26se041.edubridgehcm.repositories.ProgramRepo;
import com.sp26se041.edubridgehcm.requests.CurriculumRequest;
import com.sp26se041.edubridgehcm.utils.CurriculumNamingUtil;
import io.hypersistence.utils.common.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CurriculumValidation {

    public static String validationUpsertCurriculum(CurriculumRequest request, CurriculumRepo curriculumRepo, ProgramRepo programRepo) {
        // 1. Kiểm tra tồn tại bản ghi & Trạng thái
        Curriculum existing = null;
        if (request.getCurriculumId() != null && request.getCurriculumId() > 0) {

            existing = curriculumRepo.findById(request.getCurriculumId()).orElse(null);

            if (existing == null) return "Không tìm thấy khung chương trình.";

            // KHÔNG cho phép sửa nếu đã bị ARCHIVED (Lưu trữ)
            if (Status.CUR_ARCHIVED.equals(existing.getCurriculumStatus())) {
                return "Không thể cập nhật khung chương trình đã được lưu trữ. Vui lòng tạo phiên bản mới hoặc sử dụng bản đang hoạt động.";
            }

            // 2. Kiểm tra tính bất biến khi đã có Chương trình (Program) liên kết
            int linkedPrograms = programRepo.countByCurriculumId(existing.getId());

            if (linkedPrograms > 0) {
                if (!existing.getCurriculumType().name().equals(request.getCurriculumType())) {
                    return "Không thể thay đổi loại chương trình khi đã có chương trình đào tạo liên kết.";
                }
                // Nếu đổi SubType dẫn đến đổi GroupCode cũng phải chặn
                String newGroupCode = CurriculumNamingUtil.generateGroupCode(request);
                if (!existing.getGroupCode().equals(newGroupCode)) {
                    return "Không thể thay đổi tên phân loại phụ (Sub-type) khi đã có chương trình đào tạo liên kết.";
                }
            }
        }

        String targetGroupCode = CurriculumNamingUtil.generateGroupCode(request);

        // 3. Kiểm tra trùng lặp bản nháp (Vì không có Year, chỉ được phép có 1 bản nháp cho mỗi GroupCode)
        boolean isDuplicateDraft = curriculumRepo.existsByGroupCodeAndCurriculumStatusAndIdNot(
                targetGroupCode, Status.CUR_DRAFT, request.getCurriculumId() != null ? request.getCurriculumId() : -1);

        if (isDuplicateDraft) {
            return "Đã tồn tại một bản nháp cho phân loại chương trình này. Vui lòng cập nhật bản nháp đó.";
        }

        // 4. Validate Enum & Các trường cơ bản
        if (StringUtils.isBlank(request.getSubTypeName())) return "Tên phân loại phụ (Sub-type) không được để trống.";

        if (request.getSubTypeName().length() > 50) return "Tên phân loại phụ quá dài (tối đa 50 ký tự).";

        try {
            if (request.getMethodLearningList() == null || request.getMethodLearningList().isEmpty()) {
                return "Yêu cầu ít nhất một phương thức học tập.";
            }

            for (String method : request.getMethodLearningList()) {
                LearningMethod.valueOf(method.toUpperCase());
            }
        } catch (Exception e) {
            return "Loại chương trình hoặc Phương thức học tập không hợp lệ.";
        }

        if (request.getSubjectOptions() == null || request.getSubjectOptions().isEmpty()) {
            return "Khung chương trình phải chứa ít nhất một môn học.";
        }

        if (request.getSubjectOptions().size() > 50) {
            return "Số lượng môn học tối đa là 50.";
        }

        CurriculumType type = parseCurriculumType(request.getCurriculumType());

        if (type == null) return "Loại chương trình giảng dạy không hợp lệ.";

        boolean isNational = CurriculumType.NATIONAL.equals(type);

        if (isNational && request.getSubjectOptions().size() < 8) {
            return "Khung chương trình Quốc gia phải có ít nhất 8 môn (7 bắt buộc và 1 ngoại ngữ).";
        }

        Set<String> subjectNames = new HashSet<>();
        int mandatoryCount = 0;

        for (var opt : request.getSubjectOptions()) {
            String sName = opt.getName() != null ? opt.getName().trim() : "";
            if (StringUtils.isBlank(sName)) return "Tên môn học không được để trống.";
            if (sName.length() > 100) return "Tên môn học '" + sName + "' quá dài.";

            if (!subjectNames.add(sName.toLowerCase())) {
                return "Phát hiện tên môn học bị trùng lặp: " + sName;
            }

            if (Boolean.TRUE.equals(opt.getIsMandatory())) {
                mandatoryCount++;
            }
        }

        // 5. Kiểm tra logic môn bắt buộc theo Type
        if (isNational) {
            if (mandatoryCount < 7) {
                return "Hệ Quốc gia phải có ít nhất 7 môn bắt buộc.";
            }
        } else {
            if (mandatoryCount == 0) {
                return "Khung chương trình phải có ít nhất một môn học bắt buộc.";
            }
        }

        return null;
    }

    public static CurriculumType parseCurriculumType(String value) {
        String normalizedValue = normalize(value);
        if (normalizedValue == null) {
            return null;
        }

        return Arrays.stream(CurriculumType.values())
                .filter(r -> r.getValue().equalsIgnoreCase(normalizedValue) || r.name().equalsIgnoreCase(normalizedValue))
                .findFirst()
                .orElse(null);
    }

    public static String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
