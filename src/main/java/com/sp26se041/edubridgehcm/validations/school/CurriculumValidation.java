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
import java.util.HashSet;
import java.util.Set;

public class CurriculumValidation {

    public static String validationUpsertCurriculum(CurriculumRequest request, CurriculumRepo curriculumRepo, ProgramRepo programRepo) {
        // 1. Kiểm tra tồn tại bản ghi & Trạng thái
        Curriculum existing = null;
        if (request.getCurriculumId() != null && request.getCurriculumId() > 0) {
            existing = curriculumRepo.findById(request.getCurriculumId()).orElse(null);
            if (existing == null) return "Curriculum not found.";

            // KHÔNG cho phép sửa nếu đã bị ARCHIVED
            if (Status.CUR_ARCHIVED.equals(existing.getCurriculumStatus())) {
                return "Cannot update an archived curriculum. Please create a new version or use an active one.";
            }

            // 2. Kiểm tra tính bất biến khi đã có Program liên kết
            int linkedPrograms = programRepo.countByCurriculumId(existing.getId());
            if (linkedPrograms > 0) {
                if (existing.getEnrollmentYear() != request.getEnrollmentYear()) {
                    return String.format("Cannot change enrollment year because %d programs are using this curriculum.", linkedPrograms);
                }
                if (!existing.getCurriculumType().name().equals(request.getCurriculumType())) {
                    return "Cannot change curriculum type for a curriculum already linked to programs.";
                }
                // Nếu đổi SubType dẫn đến đổi GroupCode cũng phải chặn
                String newGroupCode = CurriculumNamingUtil.generateGroupCode(request);
                if (!existing.getGroupCode().equals(newGroupCode)) {
                    return "Cannot change sub-type name for a curriculum already linked to programs.";
                }
            }
        }

        // 3. Kiểm tra trùng lặp Business Identity trong Database
        // Mục đích: Không cho phép tạo 2 bản DRAFT hoặc 2 bản ACTIVE cùng (Year + Type + SubType)
        String targetGroupCode = CurriculumNamingUtil.generateGroupCode(request);
        boolean isDuplicateIdentity = curriculumRepo.existsByGroupCodeAndEnrollmentYearAndCurriculumStatusNotAndIdNot(
                targetGroupCode, request.getEnrollmentYear(), Status.CUR_ARCHIVED, request.getCurriculumId() != null ? request.getCurriculumId() : -1
        );

        if (isDuplicateIdentity) {
            return "A curriculum with the same type, year, and sub-type already exists (Draft or Active).";
        }

        // 4. Validate Enum & Basic Fields
        if (StringUtils.isBlank(request.getSubTypeName())) return "Sub-type name is required.";
        if (request.getSubTypeName().length() > 50) return "Sub-type name is too long (max 50 chars).";

        try {
            CurriculumType.valueOf(request.getCurriculumType());
            LearningMethod.valueOf(request.getMethodLearning());
        } catch (Exception e) {
            return "Invalid Curriculum Type or Learning Method.";
        }

        // 5. Validate Năm học (Nới lỏng một chút: -2 đến +5 là thực tế nhất cho trường tư)
        int currentYear = Year.now().getValue();
        if (request.getEnrollmentYear() < currentYear - 2 || request.getEnrollmentYear() > currentYear + 5) {
            return "Enrollment year must be between " + (currentYear - 2) + " and " + (currentYear + 5);
        }

        // 6. Validate Nội dung môn học (Subjects)
        if (request.getSubjectOptions() == null || request.getSubjectOptions().isEmpty()) {
            return "The curriculum must contain at least one subject.";
        }

        // Chốt chặn số lượng môn học (Safety Limit)
        if (request.getSubjectOptions().size() > 50) {
            return "A curriculum cannot have more than 50 subjects.";
        }

        Set<String> subjectNames = new HashSet<>();
        boolean hasMandatory = false;

        for (var opt : request.getSubjectOptions()) {
            String sName = opt.getName() != null ? opt.getName().trim() : "";
            if (StringUtils.isBlank(sName)) return "Subject name cannot be empty.";

            // Check độ dài tên môn (Ví dụ: tối đa 100 ký tự)
            if (sName.length() > 100) return "Subject name '" + sName + "' is too long (max 100).";

            if (!subjectNames.add(sName.toLowerCase())) {
                return "Duplicate subject name found: " + sName;
            }

            String sDesc = opt.getDescription() != null ? opt.getDescription().trim() : "";
            if (StringUtils.isBlank(sDesc)) {
                return "Description for subject '" + sName + "' is required.";
            }

            // Chốt chặn độ dài mô tả môn học
            if (sDesc.length() > 1000) {
                return "Description for subject '" + sName + "' is too long (max 1000).";
            }

            if (Boolean.TRUE.equals(opt.getIsMandatory())) {
                hasMandatory = true;
            }
        }

        if (!hasMandatory) {
            return "The curriculum must have at least one mandatory subject.";
        }

        return null;
    }
}
