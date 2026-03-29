package com.sp26se041.edubridgehcm.models;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.Column;
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

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "`school_config`")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SchoolConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(name = "school_id")
    Integer schoolId;

    String key;

    @Column(columnDefinition = "jsonb")
    @Type(JsonBinaryType.class)
    Object value;

    @Column(name = "updated_at")
    LocalDateTime updatedAt;
}
