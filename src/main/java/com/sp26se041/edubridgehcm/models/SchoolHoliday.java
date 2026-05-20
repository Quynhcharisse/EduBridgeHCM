package com.sp26se041.edubridgehcm.models;

import com.sp26se041.edubridgehcm.enums.HolidayImpactLevel;
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
import org.jspecify.annotations.NullMarked;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "school_holiday")
@FieldDefaults(level = AccessLevel.PRIVATE)
@NullMarked
public class SchoolHoliday {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    String title;

    @Column(name = "start_date")
    LocalDate startDate;

    @Column(name = "end_date")
    LocalDate endDate;

    @ManyToOne
    @JoinColumn(name = "school_id")
    School school; // thuộc trường nào

    @ManyToOne
    @JoinColumn(name = "campus_id")
    Campus campus;

    @Column(name = "apply_to_consultant")
    @Enumerated(EnumType.STRING)
    HolidayImpactLevel holidayImpactLevel;
}
