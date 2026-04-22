package com.sp26se041.edubridgehcm.requests;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpsertServicePackageFeeRequest {

    Integer packageId;

    String name;

    String packageType;

    String description;

    Double price;

    Double finalPrice;

    Integer durationDays;

    FeatureData featureData;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class FeatureData {
        Integer maxCounsellors;
        Integer postLimit;
        Boolean hasAiAssistant;
        String parentPostPermission; // NONE, VIEW_ONLY, CREATE_POST
        Boolean isFeatured; // Hiện nhãn nổi bật
        Integer topRanking;
        String supportLevel;
    }

}
