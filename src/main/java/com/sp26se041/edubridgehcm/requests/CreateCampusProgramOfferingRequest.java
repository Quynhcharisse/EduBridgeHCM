package com.sp26se041.edubridgehcm.requests;

import com.sp26se041.edubridgehcm.enums.LearningMode;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateCampusProgramOfferingRequest {

    Integer admissionCampaignId;

    String methodCode; // phải khớp với một methodCode trong admissionMethodTimelines của campaign

    Integer campusId;

    Integer programId;

    LearningMode learningMode;

    Float priceAdjustmentPercentage;

    LocalDate openDate;

    LocalDate closeDate;
}

