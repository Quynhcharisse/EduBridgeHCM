package com.sp26se041.edubridgehcm.requests;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateAdmissionCampaignTemplateRequest {

    String name;

    String description;

    int year;

    LocalDate startDate;

    LocalDate endDate;

    List<AdmissionMethodTimelineRequest> admissionMethodTimelines;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class AdmissionMethodTimelineRequest {
        String methodCode;
        LocalDate startDate;
        LocalDate endDate;
        Boolean allowReservationSubmission; //đc nộp hồ sơ hay ko?
        Integer quota;
    }
}

