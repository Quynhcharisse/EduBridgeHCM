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

import java.time.LocalDate;
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

    String email;

    String description;

    @Column(name = "school_name")
    String schoolName;

    @Column(name = "campus_name")
    String campusName;

    @Column(name = "campus_address")
    String campusAddress;

    @Column(name = "campus_phone")
    String campusPhone;

    @Column(name = "tax_code", length = 50)
    String taxCode;

    @Column(name = "website_url")
    String websiteUrl;

    @Column(name = "logo_url")
    String logoUrl;

    @Column(name = "founding_date")
    LocalDate foundingDate;

    @Column(name = "representative_name")
    String representativeName;

    String hotline;

    @Column(name = "business_license_url")
    String businessLicenseUrl;

    @Enumerated(EnumType.STRING)
    Status status;

    @Column(name = "created_at")
    LocalDateTime createdAt;
}
