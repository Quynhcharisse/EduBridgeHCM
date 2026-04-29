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
@Table(name = "notification_recipients")
@FieldDefaults(level = AccessLevel.PRIVATE)
@NullMarked
public class NotificationRecipients {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @ManyToOne
    @JoinColumn(name = "notification_id", nullable = false)
    Notifications notification;

    @ManyToOne
    @JoinColumn(name = "recipient_user_id", nullable = false)
    Account recipientUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status", nullable = false)
    Status deliveryStatus;

    @Column(name = "delivered_at")
    LocalDateTime deliveredAt;

    @Column(name = "is_read", nullable = false)
    boolean isRead;

    @Column(name = "read_at")
    LocalDateTime readAt;

    @Column(name = "created_at", nullable = false)
    LocalDateTime createdAt;
}
