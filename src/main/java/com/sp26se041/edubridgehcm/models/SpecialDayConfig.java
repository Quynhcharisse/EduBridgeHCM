package com.sp26se041.edubridgehcm.models;

import com.sp26se041.edubridgehcm.enums.ConfigType;
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
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "special_day_config")
@FieldDefaults(level = AccessLevel.PRIVATE)
@NullMarked
public class SpecialDayConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @ManyToOne
    @JoinColumn(name = "campus_id")
    Campus campus;

    @Column(name = "config_type")
    @Enumerated(EnumType.STRING)
    ConfigType configType;

    @Column(name = "new_start_time")
    LocalTime newStartTime;

    @Column(name = "new_end_time")
    LocalTime newEndTime;

    @Column(name = "from_date")
    LocalDate fromDate;

    @Column(name = "to_date")
    LocalDate toDate;
}
