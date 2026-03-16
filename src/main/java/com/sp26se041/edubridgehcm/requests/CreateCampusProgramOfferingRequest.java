package com.sp26se041.edubridgehcm.requests;

import com.sp26se041.edubridgehcm.enums.LearningMode;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateCampusProgramOfferingRequest {

    Integer admissionCampaignId;

    Integer campusId;

    Integer programId;

    int quota;

    LearningMode learningMode;

    BigDecimal tuitionFee;

    String applicationStatus;

    LocalDate openDate;

    LocalDate closeDate;
}

