package com.sp26se041.edubridgehcm.requests;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RegisterRequest {

    String email;

    String role;

    SchoolRequest schoolRequest;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SchoolRequest {
        String schoolName;

        String schoolAddress;

        String campusName;

        String campusAddress;

        String taxCode;

        String websiteUrl;

        String logoUrl;

        LocalDate foundingDate;

        String representativeName;

        String hotline;

        String businessLicenseUrl;

        String status;

        String rejectionReason;

        LocalDateTime createdAt;
    }

}
