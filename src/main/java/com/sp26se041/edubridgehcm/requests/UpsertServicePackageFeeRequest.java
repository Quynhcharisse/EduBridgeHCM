package com.sp26se041.edubridgehcm.requests;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpsertServicePackageFeeRequest {

    Integer packageId;

    String name;

    String description;

    Double price;

    Integer durationDays;

    FeatureData featureData;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class FeatureData {
        Integer maxCounsellors;
        Integer maxAdmissions;
        Boolean allowChat;
        String parentPostPermission; // NONE, VIEW_ONLY, CREATE_POST
        Boolean isFeatured;
        Integer topRanking;
        String supportLevel;
    }

}
