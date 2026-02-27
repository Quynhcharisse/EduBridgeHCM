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

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "application")
@FieldDefaults(level = AccessLevel.PRIVATE)
@NullMarked
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @ManyToOne
    @JoinColumn(name = "student_profile_id")
    StudentProfile studentProfile;

    @ManyToOne
    @JoinColumn(name = "admission_plan_id")
    AdmissionPlan admissionPlan;

    @ManyToOne
    @JoinColumn(name = "counsellor_id")
    Counsellor counsellor;

    @Column(name = "application_code")
    String applicationCode;

    @Column(name = "submission_date")
    LocalDate submissionDate;

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb", name = "documents_jsonb")
    String documentsJsonb;

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb", name = "test_results_jsonb")
    String testResultsJsonb;

    @Column(name = "total_score")
    Double totalScore;

    @Column(name = "note", columnDefinition = "TEXT")
    String note;

    @Column(name = "application_status")
    @Enumerated(EnumType.STRING)
    Status status;
}
