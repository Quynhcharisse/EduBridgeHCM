package com.sp26se041.edubridgehcm.models;

import com.sp26se041.edubridgehcm.enums.CurriculumType;
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

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "curriculum")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Curriculum {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @ManyToOne
    @JoinColumn(name = "admission_plan_id")
    AdmissionPlan admissionPlan;

    @Enumerated(EnumType.STRING)
    @Column(name = "curriculum_type")
    CurriculumType curriculumType;

    @Type(JsonBinaryType.class)
    @Column(name = "mandatory_subjects_jsonb", columnDefinition = "jsonb")
    String mandatorySubjectsJsonb;

    @Type(JsonBinaryType.class)
    @Column(name = "subjects_jsonb", columnDefinition = "jsonb")
    String subjectsJsonb;

    @Column(name = "method_learning")
    String methodLearning;
}
