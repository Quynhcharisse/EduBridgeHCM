package com.sp26se041.edubridgehcm.models;

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

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "program")
@FieldDefaults(level = AccessLevel.PRIVATE)
@NullMarked
public class Program {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @ManyToOne
    @JoinColumn(name = "curriculum_id")
    Curriculum curriculum;

    @OneToMany(mappedBy = "program")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<CampusProgramOffering> campusProgramOfferingList;

    @Column(name = "graduation_standard")
    String graduationStandard;

    @Column(name = "target_student_description")
    String targetStudentDescription;

    @Column(name = "base_tuition_fee")
    double baseTuitionFee;

    @Enumerated(EnumType.STRING)
    Status status;
}
