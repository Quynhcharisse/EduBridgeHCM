package com.sp26se041.edubridgehcm.requests;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProgramRequest {

    Integer programId;

    Integer curriculumId;

    String name;

    String graduationStandard;

    String languageOfInstruction;

    String programCategory;

    BigDecimal baseTuitionFee;

    String targetStudentDescription;

    String feeUnit;
}
