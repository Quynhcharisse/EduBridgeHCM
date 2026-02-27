package com.sp26se041.edubridgehcm.models;

import com.sp26se041.edubridgehcm.enums.Gender;
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
import org.jspecify.annotations.NullMarked;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "student_profile")
@FieldDefaults(level = AccessLevel.PRIVATE)
@NullMarked
public class StudentProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    Parent parent;

    @Column(name = "student_name")
    String studentName;

    @Column(name = "dob")
    LocalDate dob;

    @Enumerated(EnumType.STRING)
    Gender gender;

    /* Gom 3 cột TEXT cũ và interests_json vào đây:
     - academic_history: "Điểm trung bình 8.5, học sinh giỏi..."
     - personality_traits: "Hướng nội, thích logic..."
     - goals_aspirations: "Muốn học CNTT, săn học bổng..."
     - interests: ["Code", "Game", "Music"]
   */
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb", name = "profile_metadata")
    String profileMetadata;

    @OneToMany(mappedBy = "studentProfile")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<Application> applicationList;
}
