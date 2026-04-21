package com.sp26se041.edubridgehcm.models;

import com.sp26se041.edubridgehcm.enums.PackageType;
import com.sp26se041.edubridgehcm.enums.Status;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Type;
import org.jspecify.annotations.NullMarked;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "subscription")
@FieldDefaults(level = AccessLevel.PRIVATE)
@NullMarked
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(name = "name")
    String name;

    @Column(name = "description", columnDefinition = "TEXT")
    String description;

    @Column(name = "package_type")
    @Enumerated(EnumType.STRING)
    PackageType packageType;

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb", name = "features")
    Object features;

    @Column(name = "package_status")
    @Enumerated(EnumType.STRING)
    Status packageStatus;

    @Column(name = "price")
    Double price;

    @Column(name = "final_price")
    Double finalPrice; // giá cuối cùng bao gồm thuế/phí

    @Column(name = "service_fee")
    Double serviceFee;

    @Column(name = "tax_fee")
    Double taxFee;

    @Column(name = "duration_days")
    Integer durationDays;

    @OneToMany(mappedBy = "subscription", cascade = CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<SchoolSubscription> schoolSubscriptionList;
}
