package com.sp26se041.edubridgehcm.requests;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateProfileRequest {

    ParentData parentData;

    CounsellorData counsellorData;

    CampusData campusData;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class ParentData {

        String gender;

        String name;

        String phone;

        String relationship;

        String workplace;

        String occupation;

        String currentAddress;

        String idCardNumber;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class CounsellorData {

        String name;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class CampusData {

        SchoolData schoolData;

        String phoneNumber;

        String city;

        String district;

        String ward;

        String boardingType;

        String address;

        Double latitude;

        Double longitude;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class SchoolData {

        String description;

        String logoUrl;

        String websiteUrl;

        String hotline;
    }
}
