package com.sp26se041.edubridgehcm.requests;

import com.sp26se041.edubridgehcm.enums.StudyOrientation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAdmissionPlanRequest {

    int campusId;

    String title;

    Integer quota;

    LocalDate startDate;

    LocalDate endDate;

    Double minGpaRequired;

    String conductRequired;

    StudyOrientation studyOrientation;

    Object requirementsDetails;
}

