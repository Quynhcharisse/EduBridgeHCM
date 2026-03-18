    package com.sp26se041.edubridgehcm.models;

    import com.sp26se041.edubridgehcm.enums.CurriculumType;
    import com.sp26se041.edubridgehcm.enums.LearningMethod;
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
    import jakarta.persistence.Table;
    import lombok.AccessLevel;
    import lombok.AllArgsConstructor;
    import lombok.Builder;
    import lombok.Data;
    import lombok.NoArgsConstructor;
    import lombok.experimental.FieldDefaults;
    import org.hibernate.annotations.Type;

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

        String description;

        @Enumerated(EnumType.STRING)
        @Column(name = "curriculum_type")
        CurriculumType curriculumType;

        @Type(JsonBinaryType.class)
        @Column(name = "subjects_jsonb", columnDefinition = "jsonb")
        Object subjectsJsonb;

        @Enumerated(EnumType.STRING)
        @Column(name = "learning_method")
        LearningMethod methodLearning;

        @Column(name = "enrollment_year")
        int enrollmentYear;

        @Column(name = "group_code")
        String groupCode;

        long version;

        @Column(name = "is_latest")
        boolean isLatest;

        @Enumerated(EnumType.STRING)
        @Column(name = "curriculum_status")
        Status curriculumStatus;

        @ManyToOne
        @JoinColumn(name = "school_id")
        School school;

        @ManyToOne
        @JoinColumn(name = "parent_id")
        Curriculum parent; // liên kết các phiên bản của cùng một chương trình đào tạo
    }
