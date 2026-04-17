package com.sp26se041.edubridgehcm.validations.school;

import com.sp26se041.edubridgehcm.enums.FeeUnit;
import com.sp26se041.edubridgehcm.enums.LanguageInstruction;
import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.models.Campus;
import com.sp26se041.edubridgehcm.models.Curriculum;
import com.sp26se041.edubridgehcm.models.Program;
import com.sp26se041.edubridgehcm.repositories.CurriculumRepo;
import com.sp26se041.edubridgehcm.repositories.ProgramRepo;
import com.sp26se041.edubridgehcm.requests.ProgramRequest;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ProgramValidation {

    public static String validationUpsertProgram(ProgramRequest request,
                                                 Campus actorCampus,
                                                 CurriculumRepo curriculumRepo,
                                                 ProgramRepo programRepo) {

        if (request == null) {
            return "Dữ liệu yêu cầu không được để trống";
        }

        if (request.getCurriculumId() == null) return "Yêu cầu mã khung chương trình (Curriculum ID)";

        Curriculum curriculum = curriculumRepo.findById(request.getCurriculumId()).orElse(null);

        if (curriculum == null || !curriculum.getSchool().getId().equals(actorCampus.getSchool().getId())) {
            return "Khung chương trình không hợp lệ hoặc không thuộc về trường của bạn";
        }

        if (curriculum.getCurriculumStatus() != Status.CUR_ACTIVE) {
            return "Không thể sử dụng khung chương trình này. Chỉ những khung chương trình đang HOẠT ĐỘNG mới có thể liên kết với chương trình đào tạo.";
        }

        if (normalize(request.getName()) == null) return "Tên chương trình không được để trống";

        if (normalize(request.getName()).length() > 100) return "Tên chương trình quá dài (tối đa 100 ký tự)";

        if (normalize(request.getGraduationStandard()) == null) return "Chuẩn đầu ra không được để trống";

        if (normalize(request.getGraduationStandard()).length() > 2000)
            return "Chuẩn đầu ra quá dài (tối đa 2000 ký tự)";

        if (request.getLanguageOfInstructionList() == null || request.getLanguageOfInstructionList().isEmpty()) {
            return "Yêu cầu ít nhất một ngôn ngữ giảng dạy.";
        }
        for (String lang : request.getLanguageOfInstructionList()) {
            if (!isValidLanguageOfInstruction(lang)) {
                return "Ngôn ngữ không hợp lệ: " + lang + ". Phải là một trong: " + Arrays.toString(LanguageInstruction.values());
            }
        }

        if (request.getBaseTuitionFee() == null) return "Học phí không được để trống";

        if (request.getBaseTuitionFee().compareTo(BigDecimal.ZERO) < 0) return "Học phí không được là số âm";

        if (normalize(request.getFeeUnit()) == null) {
            return "Đơn vị tính phí không được để trống";
        }

        if (!isValidFeeUnit(request.getFeeUnit())) {
            return "Đơn vị tính phí không hợp lệ. Phải là một trong:" + Arrays.toString(FeeUnit.values());
        }

        if (normalize(request.getTargetStudentDescription()) == null)
            return "Mô tả đối tượng học sinh không được để trống";

        if (normalize(request.getTargetStudentDescription()).length() > 2000)
            return "Mô tả đối tượng học sinh quá dài (tối đa 2000 ký tự)";

        if (request.getExtraSubjectList() != null && !request.getExtraSubjectList().isEmpty()) {
            List<Map<String, Object>> coreSubjects = (List<Map<String, Object>>) curriculum.getSubjectsJsonb();
            Set<String> coreNames = coreSubjects == null ? new HashSet<>() : coreSubjects.stream()
                    .map(s -> s.get("name").toString().toLowerCase().trim())
                    .collect(Collectors.toSet());

            Set<String> extraNamesInRequest = new HashSet<>();

            for (var extra : request.getExtraSubjectList()) {

                String extraName = normalize(extra.getName());

                if (extraName == null) return "Tên môn học bổ sung không được để trống";

                // Chặn trùng với môn trong Curriculum
                if (coreNames.contains(extraName.toLowerCase())) {
                    return "Môn học '" + extraName + "' đã tồn tại trong Khung chương trình cốt lõi.";
                }

                // Chặn trùng tên ngay trong danh sách gửi lên
                if (!extraNamesInRequest.add(extraName.toLowerCase())) {
                    return "Phát hiện tên môn học bổ sung bị trùng lặp trong yêu cầu: " + extraName;
                }

                if (normalize(extra.getDescription()) == null) {
                    return "Mô tả cho môn học bổ sung '" + extraName + "' không được để trống.";
                }
            }
        }

        boolean isUpdate = request.getProgramId() != null && request.getProgramId() > 0;

        if (isUpdate) {
            Program existingProgram = programRepo.findByIdAndCurriculum_School_Id(request.getProgramId(), actorCampus.getSchool().getId());

            if (existingProgram == null) {
                return "Không tìm thấy chương trình đào tạo trong phạm vi quản lý của trường bạn";
            }

            boolean duplicatedName = isUpdate
                    ? programRepo.existsByCurriculum_IdAndNameIgnoreCaseAndIdNot(request.getCurriculumId(), normalize(request.getName()), request.getProgramId())
                    : programRepo.existsByCurriculum_IdAndNameIgnoreCase(request.getCurriculumId(), normalize(request.getName()));

            if (duplicatedName) return "Tên chương trình đào tạo đã tồn tại trong khung chương trình này";

            if (Status.PRO_ACTIVE.equals(existingProgram.getStatus())) {

                // 1. Chặn đổi Curriculum khi đã ACTIVE
                if (!existingProgram.getCurriculum().getId().equals(request.getCurriculumId())) {
                    return "Không thể thay đổi khung chương trình của một chương trình đào tạo đang HOẠT ĐỘNG.";
                }

                // 2. Chặn đổi học phí hoặc đơn vị tính khi đã ACTIVE
                if (existingProgram.getBaseTuitionFee().compareTo(request.getBaseTuitionFee()) != 0
                        || !existingProgram.getFeeUnit().name().equalsIgnoreCase(request.getFeeUnit())) {
                    return "Không thể thay đổi học phí hoặc đơn vị tính của chương trình đào tạo đang HOẠT ĐỘNG. Vui lòng đóng chương trình này và tạo chương trình mới.";
                }
            }

            boolean isCurriculumChanging = !existingProgram.getCurriculum().getId().equals(request.getCurriculumId());

            int offeringCount = programRepo.countOfferingsById(existingProgram.getId());

            if (offeringCount > 0 && isCurriculumChanging) {
                return "Không thể thay đổi khung chương trình vì chương trình đào tạo này đã có các suất tuyển sinh hoặc hồ sơ nhập học đang hoạt động.";
            }

            boolean duplicatedWhenUpdate = programRepo.existsByCurriculum_IdAndGraduationStandardIgnoreCaseAndIdNot(
                    request.getCurriculumId(),
                    normalize(request.getGraduationStandard()),
                    existingProgram.getId()
            );

            if (duplicatedWhenUpdate) {
                return "Chuẩn đầu ra đã tồn tại trong khung chương trình này";
            }
        } else {
            boolean duplicatedWhenCreate = programRepo.existsByCurriculum_IdAndGraduationStandardIgnoreCase(
                    request.getCurriculumId(),
                    normalize(request.getGraduationStandard())
            );

            if (duplicatedWhenCreate) {
                return "Chuẩn đầu ra đã tồn tại trong khung chương trình này";
            }
        }

        return null;
    }

    private static boolean isValidLanguageOfInstruction(String languageOfInstruction) {
        if (languageOfInstruction == null) return false;
        for (LanguageInstruction language : LanguageInstruction.values()) {
            if (language.name().equalsIgnoreCase(languageOfInstruction)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isValidFeeUnit(String feeUnit) {
        if (feeUnit == null) return false;
        for (FeeUnit unit : FeeUnit.values()) {
            if (unit.name().equalsIgnoreCase(feeUnit)) {
                return true;
            }
        }
        return false;
    }

    public static String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
