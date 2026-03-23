package com.sp26se041.edubridgehcm.requests;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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

        String name;

        String phoneNumber;

        String policyDetail;

        String address;

        ImageJsonData imageJson;

        FacilityData facilityJson;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class SchoolData {

        String name;

        String description;

        String logoUrl;

        String websiteUrl;

        String representativeName;

        String hotline;

        String businessLicenseUrl;

        LocalDate foundingDate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class ImageJsonData {

        String coverUrl;

        List<ImageItem> itemList;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class ImageItem {

        String name;

        String url;

        String altName;

        LocalDateTime uploadDate;

        boolean isUsage;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class FacilityData {

        String overview;

        List<FacilityItem> itemList;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class FacilityItem {
        // Ví dụ: "LIBRARY", "SWIMMING_POOL", "LAB"
        // Dùng code để Frontend dễ map với bộ Icon
        String facilityCode;

        String name; // Ví dụ: Thư viện trung tâm

        String value; // Ví dụ: 500

        String unit; // Ví dụ: "m2" hoặc "phòng

        // Phân loại: Học tập, Thể thao, Nội trú
        String category;
    }
}
