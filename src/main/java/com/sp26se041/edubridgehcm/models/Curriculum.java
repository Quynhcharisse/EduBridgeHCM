package com.sp26se041.edubridgehcm.models;

import com.sp26se041.edubridgehcm.enums.CurriculumType;
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
import org.hibernate.annotations.Type;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "curriculum")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Curriculum {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    String name;

    @Column(columnDefinition = "TEXT")
    String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "curriculum_type")
    CurriculumType curriculumType;

    @Type(JsonBinaryType.class)
    @Column(name = "subjects_jsonb", columnDefinition = "jsonb")
    Object subjectsJsonb;

    @Type(JsonBinaryType.class)
    @Column(name = "learning_methods", columnDefinition = "jsonb")
    Object learningMethodList;

    @Column(name = "application_year")
    Integer applicationYear;

    @Column(name = "group_code")
    String groupCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "curriculum_status")
    Status curriculumStatus;

    @ManyToOne
    @JoinColumn(name = "school_id")
    School school;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    Curriculum parent;

    @OneToMany(mappedBy = "curriculum")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<Program> programs;
}
