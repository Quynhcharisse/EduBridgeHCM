package com.sp26se041.edubridgehcm.models;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.UUID;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "counsellor")
@FieldDefaults(level = AccessLevel.PRIVATE)
@NullMarked
public class Counsellor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @OneToOne
    @JoinColumn(name = "`account_id`")
    Account account;

    @Column(name = "employee_code")
    UUID employeeCode; // ở ngoài tự tạo

    String name;

    @ManyToOne
    @JoinColumn(name = "`campus_id`")
    Campus campus;

    @OneToMany(mappedBy = "counsellor", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<Assignment> assignmentList;

    @OneToMany(mappedBy = "counsellor")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<Application> applicationList;
}
