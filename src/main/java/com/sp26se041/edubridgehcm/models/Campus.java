package com.sp26se041.edubridgehcm.models;

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
import org.hibernate.annotations.Type;
import org.jspecify.annotations.NullMarked;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "campus")
@FieldDefaults(level = AccessLevel.PRIVATE)
@NullMarked
public class Campus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @ManyToOne
    @JoinColumn(name = "school_id")
    School school;

    @OneToOne
    @JoinColumn(name = "account_id")
    Account account;

    String name;

    String address;

    @Column(name = "is_active")
    Boolean isActive; // đại diện cho trạng thái hoạt động của cơ sở đó ==> hoạt động thực tế

    @Enumerated(EnumType.STRING)
    Status status; // đại diện cho trạng thái xét duyệt của cơ sở đó (PENDING_APPROVAL, APPROVED, REJECTED) ==> xét duyệt pháp lý

    @Column(name = "is_primary_branch")
    Boolean isPrimaryBranch; // campus 1 sẽ có quyền duyệt campust 2,3,4...

    @Column(name = "is_boarding_school")
    Boolean isBoardingSchool;

    @Column(name = "is_day_school")
    Boolean isDaySchool;

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb", name = "image_json")
    String imageJson;

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb", name = "policy_details_jsonb")
    String policyDetailsJsonb; //lưu thông tin về ký túc xá, quy định riêng của từng cơ sở (open time, close time, mapUrl)

    @OneToMany(mappedBy = "campus")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<ConsultationSlot> consultationSlotList;

    @OneToMany(mappedBy = "campus")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<Counsellor> counsellorList;

    @OneToMany(mappedBy = "campus")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<AdmissionPlan> admissionPlanList;

    @OneToMany(mappedBy = "campus")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<SpecialDayConfig> specialDayConfigList;

    @OneToMany(mappedBy = "campus")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<FeeStructure> feeStructureList;
}
