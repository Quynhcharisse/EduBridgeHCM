package com.sp26se041.edubridgehcm.requests;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateAdmissionReservationFormRequest {


    List<SubmissionDocument> submissionDocuments;

    String methodCode;

    int campusProgramOfferingId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class SubmissionDocument {
        List<String> imageUrl;
        String key;
    }

}
