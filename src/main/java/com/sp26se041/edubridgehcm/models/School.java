package com.sp26se041.edubridgehcm.models;

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
import org.jspecify.annotations.NullMarked;

import java.math.BigDecimal;
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

    String description;

    @Column(name = "tax_code", length = 50)
    String taxCode;

    @Column(name = "logo_url")
    String logoUrl;

    @Column(name = "website_url")
    String websiteUrl;

    @Column(name = "representative_name")
    String representativeName;

    String hotline;

    @Column(name = "average_rating")
    BigDecimal averageRating;

    @Column(name = "business_license_url")
    String businessLicenseUrl; // Link ảnh giấy phép kinh doanh

    @Column(name = "is_featured")
    Boolean isFeatured;

    @Column(name = "founding_date")
    LocalDate foundingDate;

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
    List<AdmissionCampaign> admissionCampaignList;

    @OneToMany(mappedBy = "school")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<PaymentTransaction> paymentTransactionList;

    @OneToMany(mappedBy = "school")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<Curriculum> curriculumList;
}
