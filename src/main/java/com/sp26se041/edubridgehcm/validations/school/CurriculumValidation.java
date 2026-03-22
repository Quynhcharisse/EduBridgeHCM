package com.sp26se041.edubridgehcm.validations.school;

import com.sp26se041.edubridgehcm.enums.CurriculumType;
import com.sp26se041.edubridgehcm.enums.LearningMethod;
import com.sp26se041.edubridgehcm.models.Curriculum;
import com.sp26se041.edubridgehcm.repositories.CurriculumRepo;
import com.sp26se041.edubridgehcm.repositories.ProgramRepo;
import com.sp26se041.edubridgehcm.requests.CurriculumRequest;
import com.sp26se041.edubridgehcm.utils.CurriculumNamingUtil;
import io.hypersistence.utils.common.StringUtils;

import java.time.Year;

public class CurriculumValidation {

    public static String validationUpsertCurriculum(CurriculumRequest request, CurriculumRepo curriculumRepo, ProgramRepo programRepo) {

        // 1. Kiểm tra tồn tại bản ghi và tính bất biến (Immutability)
        Curriculum existing = null;
        if (request.getCurriculumId() != null && request.getCurriculumId() > 0) {
            existing = curriculumRepo.findById(request.getCurriculumId()).orElse(null);
            if (existing == null) return "Curriculum not found";

            // Kiểm tra Program liên kết để chặn đổi thông tin định danh
            int linkedPrograms = programRepo.countByCurriculumId(existing.getId());
            if (linkedPrograms > 0) {
                if (existing.getEnrollmentYear() != request.getEnrollmentYear()) {
                    return String.format("Cannot change enrollment year because %d programs are using this curriculum.", linkedPrograms);
                }
                if (!existing.getCurriculumType().name().equals(request.getCurriculumType())) {
                    return "Cannot change curriculum type for a curriculum already linked to programs.";
                }
            }
        }

        // 2. Kiểm tra trùng lặp định danh (Business Identity Check)
        // Tránh việc tạo 2 bản ghi khác ID nhưng cùng (Type + Year + SubType) dẫn đến trùng GroupCode
        String newGroupCode = CurriculumNamingUtil.generateGroupCode(request);

        // Nếu tạo mới, hoặc sửa bản cũ mà thay đổi thông tin định danh (Identity)
        boolean isIdentityChanged = (existing == null) ||
                (!existing.getGroupCode().equals(newGroupCode)) ||
                (existing.getEnrollmentYear() != request.getEnrollmentYear());

        // 3. Validate các trường bắt buộc & Enum
        if (StringUtils.isBlank(request.getSubTypeName())) {
            return "Sub-type name is required (e.g., Cambridge, Global).";
        }

        try {
            CurriculumType.valueOf(request.getCurriculumType());
            LearningMethod.valueOf(request.getMethodLearning());
        } catch (Exception e) {
            return "Invalid Curriculum Type or Learning Method selected.";
        }

        // 4. Validate Năm học (5 năm cũ - 2 năm tương lai)
        int currentYear = Year.now().getValue();
        if (request.getEnrollmentYear() < currentYear - 5 || request.getEnrollmentYear() > currentYear + 2) {
            return String.format("Invalid enrollment year. Allowed range: %d to %d.", currentYear - 5, currentYear + 2);
        }

        // 5. Validate Nội dung môn học (Subjects)
        if (request.getSubjectOptions() == null || request.getSubjectOptions().isEmpty()) {
            return "The curriculum must contain at least one subject.";
        }

        // Kiểm tra từng môn học trong danh sách
        for (var opt : request.getSubjectOptions()) {
            if (StringUtils.isBlank(opt.getName())) {
                return "Subject name cannot be empty.";
            }
            if (StringUtils.isBlank(opt.getDescription())) {
                return "Description for subject '" + opt.getName() + "' is required.";
            }
        }

        // Chốt chặn nghiệp vụ: Phải có ít nhất 1 môn bắt buộc
        boolean hasMandatory = request.getSubjectOptions().stream()
                .anyMatch(o -> Boolean.TRUE.equals(o.getIsMandatory()));

        if (!hasMandatory) {
            return "The curriculum must have at least one mandatory (required) subject.";
        }

        return null;
    }
}
