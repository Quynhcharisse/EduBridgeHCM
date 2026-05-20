package com.sp26se041.edubridgehcm.models;

import jakarta.persistence.*;
import lombok.*;
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

    String avatar;

    @ManyToOne
    @JoinColumn(name = "`campus_id`")
    Campus campus;

    @OneToMany(mappedBy = "counsellor", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<CounsellorSlot> counsellorSlotList;
}
