package com.sp26se041.edubridgehcm.requests;

import com.sp26se041.edubridgehcm.enums.LearningMode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateCampusProgramOfferingRequest {
    Integer id;

    Integer admissionCampaignId;

    Integer campusId;

    Integer programId;

    LearningMode learningMode;

    Float priceAdjustmentPercentage;

    LocalDate openDate;

    LocalDate closeDate;
}
