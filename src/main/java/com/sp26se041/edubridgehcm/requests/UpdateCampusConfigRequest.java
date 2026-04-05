package com.sp26se041.edubridgehcm.requests;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateCampusConfigRequest {

    String overview;

    List<FacilityItemRequest> itemList;

    Map<String, Object> imageJsonData;

    String hotline;

    String emailSupport;

    Integer minCounsellorPerSlot;

    CampusWorkingOverride workingOverride;

    List<AdmissionStepOverride> admissionStepsOverride;

    String policyDetail;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class CampusWorkingOverride {
        String note;
        Boolean isOpenSunday;
        List<WorkShiftRequest> workShifts;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class AdmissionStepOverride {
        int stepOrder;
        String description;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class WorkShiftRequest {
        String name;
        String startTime;
        String endTime;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class FacilityItemRequest {
        String facilityCode;
        String name;
        int value;
        String unit;
        String category;
    }
}

