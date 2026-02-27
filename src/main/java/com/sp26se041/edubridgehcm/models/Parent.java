package com.sp26se041.edubridgehcm.models;

import com.sp26se041.edubridgehcm.enums.Status;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
@Table(name = "parent")
@FieldDefaults(level = AccessLevel.PRIVATE)
@NullMarked
public class Parent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @OneToOne
    @JoinColumn(name = "account_id")
    Account account;

    String name;

    String phone;

    String relationship;

    @Enumerated(EnumType.STRING)
    Status status;

    // Dùng crmMetadata để lưu các thông tin "phi cấu trúc" khác
    // như: sở thích, thói quen, lịch sử tư vấn...
    /* - ekyc_data: { id_card_number, dob, address, ocr_accuracy }
      - workplace: "Công ty FPT"
      - residency_status: "Thường trú"
      - expected_budget: (Có thể đẩy vào đây nếu không dùng để lọc thường xuyên)
    */
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb", name = "crm_metadata")
    Object crmMetadata;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<StudentProfile> studentProfileList;
}
