package com.sp26se041.edubridgehcm.models;

import com.sp26se041.edubridgehcm.enums.Status;
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
import org.jspecify.annotations.NullMarked;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "consultation_offline_request")
@FieldDefaults(level = AccessLevel.PRIVATE)
@NullMarked
public class ConsultationOfflineRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "phone")
    String phone;

    @Column(name = "question", length = 1100)
    String question;

    @Column(name = "note")
    String note;

    @Column(name = "appointment_time")
    LocalTime appointmentTime;

    @Column(name = "appointment_date")
    LocalDate appointmentDate;

    @Column(name = "created_date")
    LocalDate createdDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    Status status;

    @Column(name = "cancel_reason")
    String cancelReason;

    @ManyToOne
    @JoinColumn(name = "counsellor_id")
    Counsellor counsellor;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    Parent parent;

    @ManyToOne
    @JoinColumn(name = "counsellor_slot_id")
    CounsellorSlot counsellorSlot;
}
