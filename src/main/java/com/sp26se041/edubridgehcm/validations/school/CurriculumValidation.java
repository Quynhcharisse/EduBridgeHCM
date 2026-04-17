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

import java.time.Year;
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
                if (existing.getApplicationYear() != request.getApplicationYear()) {
                    return String.format("Không thể thay đổi năm áp dụng vì có %d chương trình đang sử dụng khung chương trình này.", linkedPrograms);
                }
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

        // 3. Kiểm tra trùng lặp Business Identity trong Database
        String targetGroupCode = CurriculumNamingUtil.generateGroupCode(request);

        boolean isDuplicateIdentity = curriculumRepo.existsByGroupCodeAndApplicationYearAndCurriculumStatusNotAndIdNot(
                targetGroupCode, request.getApplicationYear(), Status.CUR_ARCHIVED, request.getCurriculumId() != null ? request.getCurriculumId() : -1
        );

        if (isDuplicateIdentity) {
            return "Khung chương trình có cùng loại, năm và phân loại phụ này đã tồn tại (ở dạng Nháp hoặc Đang hoạt động).";
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

        // 5. Validate Năm áp dụng
        int currentYear = Year.now().getValue();
        if (request.getApplicationYear() < currentYear - 2 || request.getApplicationYear() > currentYear + 5) {
            return "Năm áp dụng phải nằm trong khoảng từ " + (currentYear - 2) + " đến " + (currentYear + 5);
        }

        // 6. Validate Nội dung môn học (Subjects)
        if (request.getSubjectOptions() == null || request.getSubjectOptions().isEmpty()) {
            return "Khung chương trình phải chứa ít nhất một môn học.";
        }

        // Giới hạn số lượng môn học
        if (request.getSubjectOptions().size() > 50) {
            return "Một khung chương trình không thể có quá 50 môn học.";
        }

        Set<String> subjectNames = new HashSet<>();
        boolean hasMandatory = false;

        for (var opt : request.getSubjectOptions()) {
            String sName = opt.getName() != null ? opt.getName().trim() : "";
            if (StringUtils.isBlank(sName)) return "Tên môn học không được để trống.";

            if (sName.length() > 100) return "Tên môn học '" + sName + "' quá dài (tối đa 100 ký tự).";

            if (!subjectNames.add(sName.toLowerCase())) {
                return "Phát hiện tên môn học bị trùng lặp: " + sName;
            }

            String sDesc = opt.getDescription() != null ? opt.getDescription().trim() : "";
            if (StringUtils.isBlank(sDesc)) {
                return "Mô tả cho môn học '" + sName + "' không được để trống.";
            }

            if (sDesc.length() > 1000) {
                return "Mô tả cho môn học '" + sName + "' quá dài (tối đa 1000 ký tự).";
            }

            if (Boolean.TRUE.equals(opt.getIsMandatory())) {
                hasMandatory = true;
            }
        }

        if (!hasMandatory) {
            return "Khung chương trình phải có ít nhất một môn học bắt buộc.";
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
