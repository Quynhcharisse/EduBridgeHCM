package com.sp26se041.edubridgehcm.models;

import com.sp26se041.edubridgehcm.enums.Status;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "admission_reservation_form")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdmissionReservationForm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb", name = "profile_metadata")
    Object profileMetadata;

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb", name = "transcript_images")
    Object transcriptImages;

    @Column(name = "created_time")
    LocalDateTime createdTime;

    @Column(name = "updated_time")
    LocalDateTime updatedTime;

    @Column(name = "cancel_reason")
    String cancelReason;

    @Column(name = "verify_by")
    String verifiedBy;

    @Column(name = "type")
    String type;

    @Column(name = "reject_reason", length = 2000)
    String rejectReason;

    @Column(name = "confirm_code", unique = true)
    String confirmCode;

    @Column(name = "payment_proof_url")
    String paymentProofUrl;

    @Column(name = "payment_confirmed_by")
    String paymentConfirmedBy;

    @Column(name = "payment_retry_count", columnDefinition = "integer default 0")
    Integer paymentRetryCount;

    @Column(name = "is_applied", columnDefinition = "boolean default false")
    Boolean isApplied;

    @ManyToOne
    @JoinColumn(name = "campus_program_offering_id")
    CampusProgramOffering campusProgramOffering;

    @ManyToOne
    @JoinColumn(name = "student_profile_id")
    StudentProfile studentProfile;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    Status status;

    @ManyToOne
    @JoinColumn(name = "admission_campaign_id")
    AdmissionCampaign admissionCampaign;

}