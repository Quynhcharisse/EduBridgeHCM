package com.sp26se041.edubridgehcm.requests;

import com.sp26se041.edubridgehcm.enums.ResourceType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SchoolConfigRequest {

    AdmissionSettingsData admissionSettingsData;

    DocumentRequirementsData documentRequirementsData;

    FinancePolicyData financePolicyData;

    OperationSettingsData operationSettingsData;

    FacilityData facilityData;

    QuotaConfigData quotaConfigData;

    // 2. PHÂN BỔ TÀI NGUYÊN GÓI CƯỚC (Chia nhỏ gói cước đã mua cho các Campus)
    ResourceDistributionData resourceDistributionData;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class AdmissionSettingsData {
        List<AdmissionMethodDetail> allowedMethods;
        int quotaAlertThresholdPercent;
        boolean autoCloseOnFull;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class AdmissionMethodDetail {
        String code;
        String displayName;
        String description;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class DocumentRequirementsData {
        List<DocumentDetail> mandatoryAll;
        List<MethodDocumentRequirement> byMethod;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class DocumentDetail {
        String code;
        String name;
        boolean required;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class MethodDocumentRequirement {
        String methodCode;
        List<DocumentDetail> documents;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class FinancePolicyData {
        ReservationFeeDetail reservationFee;
        PriceAdjustmentDetail priceAdjustment;
        String paymentNotes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class ReservationFeeDetail {
        long amount;
        String currency;
        String display;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class PriceAdjustmentDetail {
        double minPercent;
        double maxPercent;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class OperationSettingsData {
        String hotline;
        String emailSupport;
        int minCounsellorPerSlot;
        int slotDurationInMinutes;   // Thời lượng 1 ca (ví dụ: 30, 45, 60)
        int maxBookingPerSlot;       // Số học sinh tối đa trong 1 ca
        int allowBookingBeforeHours; // Chặn đặt lịch sát giờ (ví dụ: phải đặt trước 24h)
        WorkingConfig workingConfig;
        // Phần quy trình đây:
        List<AdmissionStep> admissionSteps;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class AdmissionStep {
        int stepOrder;      // thứ tự bước (1, 2, 3...)
        String stepName;    // tên bước (ví dụ: "Mua hồ sơ", "Phỏng vấn")
        String description; // mô tả chi tiết (ví dụ: "Đóng 100k tại văn phòng")
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class WorkingConfig {
        List<String> regularDays;     // ["MON", "TUE", ...]
        List<String> weekendDays;     // ["SAT"]
        List<WorkShift> workShifts; // {"morning": "07:30 - 11:30", ...}
        Boolean openSunday;
        String note;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class WorkShift {
        String name; // MORNING, AFTERNOON, EVENING
        String startTime; // "07:30"
        String endTime;   // "11:30"
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class FacilityData {
        String overview;
        List<FacilityItem> itemList;
        FacilityImageData imageData;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class FacilityItem {
        String facilityCode;
        String name;
        int value;
        String unit;
        String category;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class FacilityImageData {
        String coverUrl;
        List<ImageItemDetail> imageList;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class ImageItemDetail {
        String name;
        String url;
        String altName;
        LocalDateTime uploadDate;
        Boolean isUsage;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class QuotaConfigData {
        String academicYear;
        int totalSystemQuota;
        List<CampusQuotaAssignment> campusAssignments;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        @FieldDefaults(level = AccessLevel.PRIVATE)
        public static class CampusQuotaAssignment {
            int campusId;
            String campusName;
            int allocatedQuota; // Số lượng HQ giao cho Campus này
        }
    }

    @Data
    public static class ResourceDistributionData {
        // Phân bổ các giới hạn từ gói cước (Counsellor, User, v.v.)
        List<ResourceAllocation> allocations;
    }

    @Data
    public static class ResourceAllocation {
        String resourceType; // COUNSELLOR, ADMISSION_SLOT...
        int campusId;
        int allocatedAmount;
    }
}
