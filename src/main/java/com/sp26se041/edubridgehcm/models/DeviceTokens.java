package com.sp26se041.edubridgehcm.models;

import com.sp26se041.edubridgehcm.enums.DevicePlatform;
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
import org.jspecify.annotations.NullMarked;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "device_tokens")
@FieldDefaults(level = AccessLevel.PRIVATE)
@NullMarked
public class DeviceTokens {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    Account user;

    @Column(nullable = false, unique = true, columnDefinition = "text")
    String token;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    DevicePlatform platform;

    @Column(name = "is_active", nullable = false)
    boolean isActive;

    @Column(name = "last_seen_at", nullable = false)
    LocalDateTime lastSeenAt;

    @Column(name = "created_at", nullable = false)
    LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt;
}
