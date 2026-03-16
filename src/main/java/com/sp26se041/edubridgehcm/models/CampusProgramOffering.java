package com.sp26se041.edubridgehcm.models;

import com.sp26se041.edubridgehcm.enums.LearningMode;
import com.sp26se041.edubridgehcm.enums.Status;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.NullMarked;
import java.math.BigDecimal;
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

    @Enumerated(EnumType.STRING)
    LearningMode learningMode;

    @Column(name = "price_adjustment_percentage")
    float priceAdjustmentPercentage;

    @Column(name = "tuition_fee")
    BigDecimal tuitionFee;

    @Enumerated(EnumType.STRING)
    Status status;
}
