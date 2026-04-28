package com.sp26se041.edubridgehcm.models;

import com.sp26se041.edubridgehcm.enums.Gender;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
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

    @Enumerated(EnumType.STRING)
    Gender gender;

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb", name = "academic_profile_metadata")
    Object academicProfileMetadata;

    @Column(name = "personality_type")
    String personalityTypeName;

    @Column(name = "favourite_job")
    String favouriteJob;

    @Type(JsonBinaryType.class)
    @Column(name = "transcript_images",columnDefinition = "jsonb")
    Object transcriptImages;

    @OneToMany(mappedBy = "studentProfile", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<Conversation> conversationList;

}
