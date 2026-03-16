package com.sp26se041.edubridgehcm.models;

import com.sp26se041.edubridgehcm.enums.LearningMode;
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
import org.jspecify.annotations.NullMarked;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "campus_program_offering")
@FieldDefaults(level = AccessLevel.PRIVATE)
@NullMarked
public class CampusProgramOffering {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @ManyToOne
    @JoinColumn(name = "campus_id")
    Campus campus;

    @ManyToOne
    @JoinColumn(name = "admission_campaign_id")
    AdmissionCampaign admissionCampaign;

    @OneToMany(mappedBy = "campusProgramOffering")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<AdmissionReservationForm> admissionReservationFormList;

    @ManyToOne
    @JoinColumn(name = "program_id")
    Program program;

    int quota;

    @Column(name = "remaining_quota")
    int remainingQuota;

    @Enumerated(EnumType.STRING)
    LearningMode learningMode;

    @Column(name = "price_adjustment_percentage")
    float priceAdjustmentPercentage;

    @Column(name = "tuition_fee")
    BigDecimal tuitionFee;

    @Enumerated(EnumType.STRING)
    @Column(name = "application_status")
    Status applicationStatus;

    @Column(name = "open_date")
    LocalDate openDate;

    @Column(name = "close_date")
    LocalDate closeDate;

    @Enumerated(EnumType.STRING)
    Status status;
}
