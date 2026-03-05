package com.sp26se041.edubridgehcm.models;

import com.sp26se041.edubridgehcm.enums.Status;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Type;
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

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb", name = "document_urls")
    Object documentUrls; // Link ảnh giấy phép kinh doanh/giấy phép giáo dục (có thể lưu dạng JSON array hoặc comma-separated)

    @Enumerated(EnumType.STRING)
    Status status; // PENDING, APPROVED, REJECTED

    @Column(name = "rejection_reason")
    String rejectionReason;

    @Column(name = "created_at")
    LocalDateTime createdAt;
}
