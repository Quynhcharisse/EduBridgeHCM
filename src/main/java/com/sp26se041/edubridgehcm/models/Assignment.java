package com.sp26se041.edubridgehcm.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "consultationAssignment")
@FieldDefaults(level = AccessLevel.PRIVATE)
@NullMarked
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @ManyToOne
    @JoinColumn(name = "`slot_id`")
    ConsultationSlot consultationSlot;

    @ManyToOne
    @JoinColumn(name = "`counsellor_id`")
    Counsellor counsellor;

    @Column(name = "date_applied")
    LocalDate dateApplied;

    @Column(name = "date_unassigned")
    LocalDate dateUnassigned;
}
