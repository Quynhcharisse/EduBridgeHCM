package com.sp26se041.edubridgehcm.models;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
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

import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "campus")
@FieldDefaults(level = AccessLevel.PRIVATE)
@NullMarked
public class Campus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @ManyToOne
    @JoinColumn(name = "school_id")
    School school;

    @OneToOne
    @JoinColumn(name = "account_id")
    Account account;

    @Column(name = "description", columnDefinition = "TEXT")
    String description;

    @Column(name = "campus_name")
    String campusName;

    String address;

    String district;

    String phone;

    @Column(name = "map_url")
    String mapUrl;

    @Column(name = "open_time")
    LocalTime openTime;

    @Column(name = "close_time")
    LocalTime closeTime;

    @Column(name = "is_active")
    Boolean isActive;

    @Column(name = "is_boarding_school")
    Boolean isBoardingSchool;

    @Column(name = "is_day_school")
    Boolean isDaySchool;

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb", name = "image_json")
    String imageJson;

    @OneToMany(mappedBy = "campus")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<ConsultationSlot> consultationSlotList;

    @OneToMany(mappedBy = "campus")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<Counsellor> counsellorList;

    @OneToMany(mappedBy = "campus")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<AdmissionPlan> admissionPlanList;

    @OneToMany(mappedBy = "campus")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<SpecialDayConfig> specialDayConfigList;
}
