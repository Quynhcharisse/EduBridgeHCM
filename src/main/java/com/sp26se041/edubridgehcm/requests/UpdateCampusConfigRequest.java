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

    List<FacilityItemRequest> itemList;

    Map<String, Object> imageJsonData;

    String hotline;

    String emailSupport;

    Integer minCounsellorPerSlot;

    Integer slotDurationInMinutes;   // Thời lượng mỗi ca tư vấn (ví dụ: 30, 45, 60)

    Integer maxBookingPerSlot;        // Số khách tối đa trong 1 ca (Tư vấn 1:1 hay 1:n)

    Integer allowBookingBeforeHours;  // Thời gian phải đặt trước (ví dụ: 24 tiếng)

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

