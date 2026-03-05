package com.sp26se041.edubridgehcm.models;

import com.sp26se041.edubridgehcm.enums.Status;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.NullMarked;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "school_registration_request")
@FieldDefaults(level = AccessLevel.PRIVATE)
@NullMarked
public class SchoolRegistrationRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(name = "email_personal")
    String emailPersonal;

    @Column(name = "school_name")
    String schoolName;

    @Column(name = "school_address")
    String schoolAddress;

    @Column(name = "campus_name")
    String campusName;

    @Column(name = "campus_address")
    String campusAddress;

    @Column(name = "tax_code", length = 50)
    String taxCode; // Mã số thuế của trường

    @Column(name = "website_url")
    String websiteUrl; // Website của trường

    @Column(name = "business_license_url")
    String businessLicenseUrl; // Link ảnh giấy phép kinh doanh

    @Enumerated(EnumType.STRING)
    Status status; // PENDING, APPROVED, REJECTED

    @Column(name = "rejection_reason")
    String rejectionReason;

    @Column(name = "created_at")
    LocalDateTime createdAt;
}
