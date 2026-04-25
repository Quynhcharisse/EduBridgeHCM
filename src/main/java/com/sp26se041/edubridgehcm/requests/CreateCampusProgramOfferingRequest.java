package com.sp26se041.edubridgehcm.requests;

import com.sp26se041.edubridgehcm.enums.LearningMode;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateCampusProgramOfferingRequest {

    Integer admissionCampaignId;

    Integer campusId;

    Integer programId;

    LearningMode learningMode;

    Float priceAdjustmentPercentage;
}

