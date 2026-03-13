package com.sp26se041.edubridgehcm.requests;

import com.sp26se041.edubridgehcm.enums.LearningMode;
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
public class CreateCampusProgramOfferingRequest {

    Integer admissionCampaignId;

    Integer campusId;

    Integer programId;

    int quota;

    LearningMode learningMode;

    BigDecimal tuitionFee;
}

