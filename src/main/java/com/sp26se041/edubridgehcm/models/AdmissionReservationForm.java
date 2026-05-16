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

    @Column(name = "reject_reason")
    String rejectReason;

    @Column(name = "transfer_code", unique = true)
    String transferCode;

    @Column(name = "confirm_code", unique = true)
    String confirmCode;

    @Column(name = "payment_proof_url")
    String paymentProofUrl;

    @Column(name = "payment_confirmed_by")
    String paymentConfirmedBy;

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