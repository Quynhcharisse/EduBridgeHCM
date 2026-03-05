package com.sp26se041.edubridgehcm.requests;

import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.models.SchoolRegistrationRequest;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RegisterRequest {
    String email;

    String role;

    String name;

    String personalEmail;

    String schoolName;

    String schoolAddress;

    String campusName;

    String campusAddress;

    String taxCode;

    String websiteUrl;

    Object documentUrls;  // sinh ra JSON : sau đó nhập biến a, biến b

    String reviewNote;

    Status status;

    String rejectionReason;

    LocalDateTime createdAt;

    LocalDateTime approvedAt;
}
