package com.sp26se041.edubridgehcm.requests;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProgramRequest {

    Integer programId;

    Integer curriculumId;

    String name;

    List<SubjectExtraRequest> extraSubjectList;

    List<Long> languageOfInstructionList;

    String graduationStandard;

    BigDecimal baseTuitionFee;

    String targetStudentDescription;

    String feeUnit;

    @Data
    public static class SubjectExtraRequest {
        String name;
        String description;
        Boolean isMandatory;
    }
}
