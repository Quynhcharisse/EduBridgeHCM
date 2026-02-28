package com.sp26se041.edubridgehcm.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "school_subscription")
@FieldDefaults(level = AccessLevel.PRIVATE)
@NullMarked
public class SchoolSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @ManyToOne
    @JoinColumn(name = "subscription_id")
    Subscription subscription;

    @ManyToOne
    @JoinColumn(name = "school_id")
    School school;

    @Column(name = "start_date")
    LocalDate startDate;

    @Column(name = "end_date")
    LocalDate endDate;

    @Column(name = "is_selected")
    Boolean isSelected;

    @Column(name = "license_key")
    String licenseKey; //giải quyết yêu cầu về "License provided by admin" và quản lý thời hạn sử dụng dịch vụ của từng trường.
}
