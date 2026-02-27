package com.sp26se041.edubridgehcm.models;

import com.sp26se041.edubridgehcm.enums.ParentType;
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
import org.jspecify.annotations.NullMarked;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "image")
@FieldDefaults(level = AccessLevel.PRIVATE)
@NullMarked
public class Image {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    Integer id;

    @Column(name = "parent_id")
    Integer parentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "parent_type")
    ParentType parentType;

    @Column(name = "alt_name")
    String altName;

    String url;
}
