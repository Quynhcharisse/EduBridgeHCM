package com.sp26se041.edubridgehcm.models;

import com.sp26se041.edubridgehcm.enums.ResourceType;
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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "campus_resource_quota",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"campus_id", "resource_type"})
        }
)
@FieldDefaults(level = AccessLevel.PRIVATE)

public class CampusResourceQuota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campus_id", nullable = false)
    Campus campus;

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false)
    ResourceType resourceType;

    @Column(name = "max_quota", nullable = false)
    Integer maxQuota;

    @Column(name = "updated_at")
    LocalDateTime updatedAt;
}
