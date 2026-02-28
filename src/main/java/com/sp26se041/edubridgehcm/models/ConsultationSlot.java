package com.sp26se041.edubridgehcm.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "consultation_slot")
@FieldDefaults(level = AccessLevel.PRIVATE)
@NullMarked
public class ConsultationSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @ManyToOne
    @JoinColumn(name = "campus_id")
    Campus campus;

    @Column(name = "day_of_week")
    String dayOfWeek;

    @Column(name = "start_time")
    LocalTime startTime;

    @Column(name = "end_time")
    LocalTime endTime;

    @Column(name = "created_date")
    LocalDate createdDate;

    @Column(name = "updated_date")
    LocalDate updatedDate;

    @OneToMany(mappedBy = "consultationSlot")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<Assignment> assignmentList;
}
