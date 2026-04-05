package com.sp26se041.edubridgehcm.models;

import com.sp26se041.edubridgehcm.enums.BoardingType;
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

    @Column(name = "phone_number")
    String phoneNumber;

    @Column(name = "address")
    String address;

    @Column(name = "city")
    String city;

    @Column(name = "district")
    String district;

    @Column(name = "latitude")
    Double latitude;

    @Column(name = "longitude")
    Double longitude;

    @Enumerated(EnumType.STRING)
    @Column(name = "boarding_type")
    BoardingType boardingType;

    @Enumerated(EnumType.STRING)
    Status status;

    @Column(name = "is_primary_branch")
    Boolean isPrimaryBranch; // campus 1 sẽ có quyền duyệt campust 2,3,4...

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb", name = "image_json")
    Object imageJson;

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb", name = "facility")
    Object facility;

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb", name = "policy_detail")
    Object policyDetail; //quy định riêng của từng cơ sở (open time, close time)

    @OneToMany(mappedBy = "campus")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<CampusScheduleTemplate> campusScheduleTemplateList;

    @OneToMany(mappedBy = "campus")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<Counsellor> counsellorList;

    @OneToMany(mappedBy = "campus")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<CampusProgramOffering> programOfferingList;

    @OneToMany(mappedBy = "campus")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<OpenDayEvent> openDayEvents;

}
