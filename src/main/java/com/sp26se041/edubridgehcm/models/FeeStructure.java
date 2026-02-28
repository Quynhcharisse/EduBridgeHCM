package com.sp26se041.edubridgehcm.models;

import com.sp26se041.edubridgehcm.enums.BoardingType;
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

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "fee_structure")
@FieldDefaults(level = AccessLevel.PRIVATE)
@NullMarked
public class FeeStructure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(name = "academic_year")
    int academicYear;

    @Enumerated(EnumType.STRING)
    @Column(name = "boarding_type")
    BoardingType boardingType;

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb", name = "fees_detail")
    Object feesDetail;

    @ManyToOne
    @JoinColumn(name = "admission_plan_id")
    AdmissionPlan admissionPlan;

    @ManyToOne
    @JoinColumn(name = "campus_id")
    Campus campus;

    @ManyToOne
    @JoinColumn(name = "curriculum_id")
    Curriculum curriculum;
}
