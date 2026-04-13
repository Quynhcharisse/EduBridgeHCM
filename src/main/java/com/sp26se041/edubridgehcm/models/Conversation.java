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
@Table(name = "conversation")
@FieldDefaults(level = AccessLevel.PRIVATE)
@NullMarked
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    Status status;

    @Column(name = "parent_email")
    String parentEmail;

    @Column(name = "counsellor_email")
    String counsellorEmail;

    @Column(name = "campus_id")
    int campusId;

    @Column(name = "admin_acc_id")
    int accAdminId;

    @Column(name = "created_date")
    LocalDateTime createdDate;

    @Column(name = "updated_date")
    LocalDateTime updatedDate;

    @ManyToOne
    @JoinColumn(name = "student_profile_id")
    StudentProfile studentProfile;

}
