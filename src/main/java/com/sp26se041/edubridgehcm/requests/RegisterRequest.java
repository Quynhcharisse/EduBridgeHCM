package com.sp26se041.edubridgehcm.requests;

import com.sp26se041.edubridgehcm.enums.Status;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

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

        String personalEmail;

        String schoolName;

        String schoolAddress;

        String campusName;

        String campusAddress;

        String taxCode;

        String websiteUrl;

        String businessLicenseUrl;  // giấy phép kinh doanh

        String reviewNote;

        String rejectionReason;

        LocalDateTime createdAt;
    }

}
