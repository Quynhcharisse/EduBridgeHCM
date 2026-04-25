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
import org.jspecify.annotations.NullMarked;
import org.hibernate.annotations.Type;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "admission_campaign")
@FieldDefaults(level = AccessLevel.PRIVATE)
@NullMarked
public class AdmissionCampaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @ManyToOne
    @JoinColumn(name = "school_id")
    School school;

    @OneToMany(mappedBy = "admissionCampaign")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<CampusProgramOffering> campusProgramOfferingList;

    @Column(name = "academic_year")
    int year;

    String name;

    @Column(columnDefinition = "TEXT")
    String description;

    String reason;

    @Column(name = "start_date")
    LocalDate startDate;

    @Column(name = "end_date")
    LocalDate endDate;

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    Object admissionMethodTimelines;

    @Enumerated(EnumType.STRING)
    Status status;

    @OneToMany(mappedBy = "admissionCampaign")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<CounsellorSlot> counsellorSlotList ;

}
