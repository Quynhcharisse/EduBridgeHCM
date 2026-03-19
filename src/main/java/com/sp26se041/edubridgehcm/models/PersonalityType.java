package com.sp26se041.edubridgehcm.models;

import com.sp26se041.edubridgehcm.enums.PersonalityTypeGroup;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Type;
import org.jspecify.annotations.NullMarked;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "personality_type")
@FieldDefaults(level = AccessLevel.PRIVATE)
@NullMarked
public class PersonalityType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "code", nullable = false, unique = true, length = 10)
    String code;

    @Column(name = "name")
    String name;

    String image;

    @Enumerated(EnumType.STRING)
    @Column(name = "personality_type_group")
    PersonalityTypeGroup personalityTypeGroup;

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb", name = "mbti_traits")
    Object traits;

    @Column(name = "description", length = 1200)
    String description;

    @Column( name = "strengths", length = 600)
    List<String> strengths;

    @Column( name = "weaknesses", length = 600)
    List<String> weaknesses;

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb", name = "quotes")
    Object quote;

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb", name = "recommendedCareers")
    Object recommendedCareers;

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb", name = "sources")
    Object sources;

    @Column(name = "created_at")
    LocalDateTime createdAt;

    @Column(name = "updated_at")
    LocalDateTime updatedAt;

}
