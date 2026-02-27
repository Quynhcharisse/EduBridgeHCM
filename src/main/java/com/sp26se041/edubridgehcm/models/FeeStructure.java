package com.sp26se041.edubridgehcm.models;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
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
import org.hibernate.annotations.Type;
import org.jspecify.annotations.NullMarked;

import java.math.BigDecimal;

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

    @Column(name = "tuition_fee", precision = 15, scale = 2)
    BigDecimal tuitionFee;

    @Column(name = "boarding_fee", precision = 15, scale = 2)
    BigDecimal boardingFee;

    @Column(name = "day_fee", precision = 15, scale = 2)
    BigDecimal dayFee;

    @Column(name = "academic_year")
    String academicYear;

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb", name = "other_services_jsonb")
    String otherServicesJsonb;

    @ManyToOne
    @JoinColumn(name = "admission_plan_id")
    AdmissionPlan admissionPlan;
}
