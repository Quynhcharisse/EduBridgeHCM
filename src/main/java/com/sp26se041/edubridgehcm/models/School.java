package com.sp26se041.edubridgehcm.models;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "school")
@FieldDefaults(level = AccessLevel.PRIVATE)
@NullMarked
public class School {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    String name;

    String address;

    @Column(name = "created_at")
    LocalDate createdAt;

    @Column(name = "tax_code", length = 50)
    String taxCode;

    @Column(name = "logo_url")
    String logoUrl;

    @Column(name = "website_url")
    String websiteUrl;

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb", name = "document_urls")
    Object documentUrls; // Link ảnh giấy phép kinh doanh/giấy phép giáo dục (có thể lưu dạng JSON array hoặc comma-separated)

    @Column(name = "is_verified")
    Boolean isVerified;

    @OneToMany(mappedBy = "school")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<Campus> campusList;

    @OneToMany(mappedBy = "school")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<SchoolSubscription> schoolSubscriptionList;

    @OneToMany(mappedBy = "school")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<PaymentTransaction> paymentTransactionList;
}
