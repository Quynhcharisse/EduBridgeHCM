package com.sp26se041.edubridgehcm.validations.school;

import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.models.Campus;
import com.sp26se041.edubridgehcm.models.Curriculum;
import com.sp26se041.edubridgehcm.models.Program;
import com.sp26se041.edubridgehcm.repositories.CurriculumRepo;
import com.sp26se041.edubridgehcm.repositories.ProgramRepo;
import com.sp26se041.edubridgehcm.requests.ProgramRequest;

import java.math.BigDecimal;
import java.util.List;

public class ProgramValidation {

    public static String validationUpsertProgram(ProgramRequest request,
                                                 Campus actorCampus,
                                                 CurriculumRepo curriculumRepo,
                                                 ProgramRepo programRepo) {

        if (request == null) {
            return "Request is required";
        }

        if (request.getCurriculumId() == null) {
            return "Curriculum ID is not found";
        }

        Curriculum curriculum = curriculumRepo.findById(request.getCurriculumId()).orElse(null);
        if (curriculum == null || !curriculum.getSchool().getId().equals(actorCampus.getSchool().getId())) {
            return "Curriculum is invalid";
        }

        if (curriculum.getCurriculumStatus() != Status.CUR_ACTIVE) {
            return "Only active curriculum can be used for a program";
        }

        if (normalize(request.getGraduationStandard()) == null) {
            return "Graduation standard is required";
        }

        if (normalize(request.getGraduationStandard()).length() > 2000) {
            return "Graduation standard exceeds 2000 characters";
        }

        if (normalize(request.getName()) == null) {
            return "Name is required";
        }

        if (normalize(request.getName()).length() > 100) {
            return "Name exceeds 100 characters";
        }

        if (normalize(request.getTargetStudentDescription()) == null) {
            return "Target student description is required";
        }

        if (normalize(request.getTargetStudentDescription()).length() > 2000) {
            return "Target student description exceeds 2000 characters";
        }

        boolean isUpdate = request.getProgramId() != null && request.getProgramId() > 0;

        if (isUpdate) {

            Program existingProgram = programRepo.findByIdAndCurriculum_School_Id(request.getProgramId(), actorCampus.getSchool().getId());

            if (existingProgram == null) {
                return "Program not found in your school scope";
            }

            boolean isCurriculumChanging = !existingProgram.getCurriculum().getId().equals(request.getCurriculumId());
            int offeringCount = programRepo.countOfferingsById(existingProgram.getId());
            int effectiveOfferingCount = programRepo.countEffectiveOfferingsById(existingProgram.getId(),
                    List.of(Status.OPEN, Status.PAUSED),
                    List.of(Status.OPEN, Status.PAUSED, Status.FULL));

            if (offeringCount > 0 && isCurriculumChanging) {
                return "Cannot change curriculum because this program has active offerings/enrollments.";
            }

            if (effectiveOfferingCount > 0 && existingProgram.isActive() && Boolean.FALSE.equals(request.getIsActive())) {
                return "Cannot deactivate a program that still has effective offerings.";
            }

            boolean duplicatedWhenUpdate = programRepo.existsByCurriculum_School_IdAndCurriculum_IdAndGraduationStandardIgnoreCaseAndIdNot(
                    actorCampus.getSchool().getId(),
                    request.getCurriculumId(),
                    normalize(request.getGraduationStandard()),
                    existingProgram.getId()
            );

            if (duplicatedWhenUpdate) {
                return "Graduation standard already exists in this curriculum";
            }
        } else {
            boolean duplicatedWhenCreate = programRepo.existsByCurriculum_School_IdAndCurriculum_IdAndGraduationStandardIgnoreCase(
                    actorCampus.getSchool().getId(),
                    request.getCurriculumId(),
                    normalize(request.getGraduationStandard())
            );

            if (duplicatedWhenCreate) {
                return "Graduation standard already exists in this curriculum";
            }
        }

        if (request.getBaseTuitionFee() == null) return "Tuition fee is required";

        if (request.getBaseTuitionFee().compareTo(BigDecimal.ZERO) < 0) return "Tuition fee cannot be negative";

        if (request.getIsActive() == null) return "Active flag is required";

        return null;
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

}
