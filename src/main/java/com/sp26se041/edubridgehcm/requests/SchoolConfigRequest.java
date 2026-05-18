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
public class SchoolConfigRequest {

    AdmissionSettingsData admissionSettingsData;

    DocumentRequirementsData documentRequirementsData;

    FinancePolicyData financePolicyData;

    OperationSettingsData operationSettingsData;

    FacilityData facilityData;

    QuotaConfigData quotaConfigData;

    ResourceDistributionData resourceDistributionData;

    BankInfoData bankInfoData;


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class BankInfoData {
        String bankId;
        String accountNo;
        String accountName;
        String bankName;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class AdmissionSettingsData {
        List<AdmissionMethodDetail> allowedMethods;
        List<MethodAdmissionProcess> methodAdmissionProcess;
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
        List<OcrCriterion> ocrCriteria;
        String templateUrl;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class OcrCriterion {
        String label;
        List<String> validations;
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
        PriceAdjustmentDetail priceAdjustment;
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
        int maxCounsellorsPerSlot;   // Tối đa TV gán cùng một khung + khoảng ngày; 0 = không giới hạn trên (chỉ còn min và rule khác)
        int slotDurationInMinutes;   // Thời lượng 1 ca (ví dụ: 30, 45, 60)
        int bufferBetweenSlotsMinutes; // Nghỉ giữa hai tiết tư vấn (phút), 0 = liền mạch
        int maxBookingPerSlot;       // Số học sinh tối đa trong 1 ca
        int allowBookingBeforeHours; // Chặn đặt lịch sát giờ (ví dụ: phải đặt trước 24h)
        WorkingConfig workingConfig; //Default Policy (Quy tắc nền) ==> áp dụng 365 ngày
        //Danh sách các mùa tuyển sinh/chiến dịch đặc biệt
        //Override Policy (Quy tắc đè). Nó chỉ tồn tại trong một khoảng thời gian ngắn (ví dụ 1 tháng cao điểm).
        List<AdmissionSeason> admissionSeasons;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class AdmissionSeason {
        String seasonName;    // "Tuyển sinh lớp 10 cao điểm"
        LocalDate startDate;  // 01/06
        LocalDate endDate;    // 15/07
        // Các quy tắc ghi đè (Overrides)
        Boolean enableSunday;            // Có mở ngày Chủ nhật không?
        List<WorkShift> extraShifts;     // Các ca làm việc tăng cường (ví dụ ca tối)
        Integer minCounsellorMultiplier; // Hệ số nhân nhân sự (ví dụ x2 người trực)
        String note;                     // Ghi chú chiến dịch
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class MethodAdmissionProcess {  // quy trình tuyển sinh của một phương thức
        String methodCode; // Ví dụ: "ACADEMIC_RECORD" ==> lấy từ admission method trong admissionSetting
        List<StepDetail> steps;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class StepDetail {
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
        String name;
        String startTime;
        String endTime;
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
