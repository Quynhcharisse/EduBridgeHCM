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
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.NullMarked;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "campus_program_offering")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Accessors(chain = true)
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

    @Column(name = "base_tuition_snapshot")
    BigDecimal baseTuitionSnapshot; // Giá gốc của Program lúc tạo Offering

    @Column(name = "program_name_snapshot")
    String programNameSnapshot; // Tên Program lúc tạo Offering

    @Column(name = "admission_method")
    String admissionMethod; // methodCode lấy từ admissionMethodTimelines của campaign

    @Column(name = "quota")
    int quota; // Chỉ tiêu snapshot từ method timeline lúc tạo offering

    @Column(name = "open_date")
    LocalDate openDate;

    @Column(name = "close_date")
    LocalDate closeDate;

    @Column(name = "remaining_quota")
    int remainingQuota; //(Chỉ tiêu còn lại)

    @Enumerated(EnumType.STRING)
    @Column(name = "learning_mode")
    LearningMode learningMode;

    @Column(name = "price_adjustment_percentage")
    Float priceAdjustmentPercentage;

    @Column(name = "tuition_fee")
    BigDecimal finalTuitionFee; // giá cuối cùng của mỗi campus sau khi chỉnh

    @Enumerated(EnumType.STRING)
    @Column(name = "application_status")
    Status applicationStatus;

    @Enumerated(EnumType.STRING)
    Status status;

    @Column(name = "allow_reservation_submission")
    Boolean allowReservationSubmission;
}
