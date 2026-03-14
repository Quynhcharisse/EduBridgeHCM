package com.sp26se041.edubridgehcm.models;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.NullMarked;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "assignment")
@FieldDefaults(level = AccessLevel.PRIVATE)
@NullMarked
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @ManyToOne
    @JoinColumn(name = "`slot_id`")
    CampusScheduleTemplate campusScheduleTemplate;

    @ManyToOne
    @JoinColumn(name = "`counsellor_id`")
    Counsellor counsellor;

    @Column(name = "date_applied")
    LocalDate dateApplied;

    @Column(name = "date_unassigned")
    LocalDate dateUnassigned;

    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<ConsultationOfflineRequest> consultationOfflineRequests;

    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<CounsellorLeaveRequestDetail> counsellorLeaveRequestDetailList;

}
