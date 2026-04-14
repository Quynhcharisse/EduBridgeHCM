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
            return "Request is required";
        }

        if (request.getCurriculumId() == null) return "Curriculum ID is required";

        Curriculum curriculum = curriculumRepo.findById(request.getCurriculumId()).orElse(null);

        if (curriculum == null || !curriculum.getSchool().getId().equals(actorCampus.getSchool().getId())) {
            return "Curriculum is invalid or does not belong to your school";
        }

        if (curriculum.getCurriculumStatus() != Status.CUR_ACTIVE) {
            return "Cannot use this curriculum. Only ACTIVE curriculums can be linked to a program.";
        }

        if (normalize(request.getName()) == null) return "Name is required";

        if (normalize(request.getName()).length() > 100) return "Name exceeds 100 characters";

        if (normalize(request.getGraduationStandard()) == null) return "Graduation standard is required";

        if (normalize(request.getGraduationStandard()).length() > 2000)
            return "Graduation standard exceeds 2000 characters";

        if (request.getLanguageOfInstructionList() == null || request.getLanguageOfInstructionList().isEmpty()) {
            return "At least one language of instruction is required.";
        }
        for (String lang : request.getLanguageOfInstructionList()) {
            if (!isValidLanguageOfInstruction(lang)) {
                return "Invalid language: " + lang + ". Must be one of: " + Arrays.toString(LanguageInstruction.values());
            }
        }

        if (request.getBaseTuitionFee() == null) return "Tuition fee is required";

        if (request.getBaseTuitionFee().compareTo(BigDecimal.ZERO) < 0) return "Tuition fee cannot be negative";

        if (normalize(request.getFeeUnit()) == null) {
            return "Fee unit is required";
        }

        if (!isValidFeeUnit(request.getFeeUnit())) {
            return "Invalid fee unit. Must be one of:" + Arrays.toString(FeeUnit.values());
        }

        if (normalize(request.getTargetStudentDescription()) == null) return "Target student description is required";

        if (normalize(request.getTargetStudentDescription()).length() > 2000)
            return "Target student description exceeds 2000 characters";

        if (request.getExtraSubjectList() != null && !request.getExtraSubjectList().isEmpty()) {
            List<Map<String, Object>> coreSubjects = (List<Map<String, Object>>) curriculum.getSubjectsJsonb();
            Set<String> coreNames = coreSubjects == null ? new HashSet<>() : coreSubjects.stream()
                    .map(s -> s.get("name").toString().toLowerCase().trim())
                    .collect(Collectors.toSet());

            Set<String> extraNamesInRequest = new HashSet<>();

            for (var extra : request.getExtraSubjectList()) {

                String extraName = normalize(extra.getName());

                if (extraName == null) return "Extra subject name cannot be empty";

                //chặn trùng với môn trong Curriculum (Kế thừa rồi thì không cần thêm)
                if (coreNames.contains(extraName.toLowerCase())) {
                    return "Subject '" + extraName + "' already exists in the core Curriculum.";
                }

                // chặn trùng tên ngay trong chính danh sách gửi lên
                if (!extraNamesInRequest.add(extraName.toLowerCase())) {
                    return "Duplicate extra subject name found in request: " + extraName;
                }

                if (normalize(extra.getDescription()) == null) {
                    return "Description for extra subject '" + extraName + "' is required.";
                }
            }
        }

        boolean isUpdate = request.getProgramId() != null && request.getProgramId() > 0;

        if (isUpdate) {
            Program existingProgram = programRepo.findByIdAndCurriculum_School_Id(request.getProgramId(), actorCampus.getSchool().getId());

            if (existingProgram == null) {
                return "Program not found in your school scope";
            }

            boolean duplicatedName = isUpdate
                    ? programRepo.existsByCurriculum_IdAndNameIgnoreCaseAndIdNot(request.getCurriculumId(), normalize(request.getName()), request.getProgramId())
                    : programRepo.existsByCurriculum_IdAndNameIgnoreCase(request.getCurriculumId(), normalize(request.getName()));

            if (duplicatedName) return "Program name already exists in this curriculum";

            if (Status.PRO_ACTIVE.equals(existingProgram.getStatus())) {

                // 1. Chặn đổi Curriculum (Đã có logic check offeringCount bên dưới, nhưng check status cho chắc)
                if (!existingProgram.getCurriculum().getId().equals(request.getCurriculumId())) {
                    return "Cannot change curriculum of an ACTIVE program.";
                }

                // 2. Chặn đổi học phí hoặc đơn vị tính khi đã ACTIVE
                if (existingProgram.getBaseTuitionFee().compareTo(request.getBaseTuitionFee()) != 0
                        || !existingProgram.getFeeUnit().name().equalsIgnoreCase(request.getFeeUnit())) {
                    return "Cannot change tuition fee or fee unit of an ACTIVE program. Please close this and create a new program.";
                }
            }

            boolean isCurriculumChanging = !existingProgram.getCurriculum().getId().equals(request.getCurriculumId());

            int offeringCount = programRepo.countOfferingsById(existingProgram.getId());

            if (offeringCount > 0 && isCurriculumChanging) {
                return "Cannot change curriculum because this program has active offerings/enrollments.";
            }

            boolean duplicatedWhenUpdate = programRepo.existsByCurriculum_IdAndGraduationStandardIgnoreCaseAndIdNot(
                    request.getCurriculumId(),
                    normalize(request.getGraduationStandard()),
                    existingProgram.getId()
            );

            if (duplicatedWhenUpdate) {
                return "Graduation standard already exists in this curriculum";
            }
        } else {
            boolean duplicatedWhenCreate = programRepo.existsByCurriculum_IdAndGraduationStandardIgnoreCase(
                    request.getCurriculumId(),
                    normalize(request.getGraduationStandard())
            );

            if (duplicatedWhenCreate) {
                return "Graduation standard already exists in this curriculum";
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
