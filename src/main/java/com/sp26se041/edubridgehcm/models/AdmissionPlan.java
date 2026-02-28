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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Type;
import org.jspecify.annotations.NullMarked;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "admission_plan")
@FieldDefaults(level = AccessLevel.PRIVATE)
@NullMarked
public class AdmissionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @ManyToOne
    @JoinColumn(name = "campus_id")
    Campus campus;

    String title;

    Integer quota;

    @Column(name = "start_date")
    LocalDate startDate;

    @Column(name = "end_date")
    LocalDate endDate;

    @Column(name = "min_gpa_required")
    Double minGpaRequired;

    @Column(name = "conduct_required")
    String conductRequired;

    @Column(name = "admission_plan_status")
    @Enumerated(EnumType.STRING)
    Status admissionPlanStatus;

    @Column(name = "verify_at")
    LocalDate verifyAt;

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb", name = "requirements_details_jsonb")
    Object requirementsDetailsJsonb;

    @OneToMany(mappedBy = "admissionPlan")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<FeeStructure> feeStructureList;

    @OneToMany(mappedBy = "admissionPlan")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<Application> applicationList;

    @OneToMany(mappedBy = "admissionPlan")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<Curriculum> curriculumList;
}
