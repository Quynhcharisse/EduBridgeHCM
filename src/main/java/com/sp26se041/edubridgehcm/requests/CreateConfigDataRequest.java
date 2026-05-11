package com.sp26se041.edubridgehcm.requests;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateConfigDataRequest {

    BusinessData businessData;

    MediaData mediaData;

    AdmissionQuotaData admissionQuotaData;

    AdmissionSettingsData admissionSettingsData;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class BusinessData {
        double taxRate; // VAT
        double serviceRate; // Phí dịch vụ mà nền tảng thu
        BigDecimal minPay; // Số tiền thanh toán tối thiểu
        BigDecimal maxPay; //Số tiền thanh toán tối đa
        SubscriptionPricing subscriptionPricing; //config chung của package fee
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class SubscriptionPricing {
        BasePrices basePrices;
        FeatureUnitPrices featureUnitPrices;
        PackageQuotas packageQuotas;
        double trialRatioCap;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class BasePrices {
        BigDecimal trial;
        BigDecimal standard;
        BigDecimal enterprise;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class FeatureUnitPrices {
        BigDecimal aiChatbotMonthlyFee;
        BigDecimal premiumSupportFee;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class PackageQuotas {
        int durationDays;
        int trialCounsellor;  //Max value admin nhập
        int standardCounsellor;
        int enterpriseCounsellor;
        int trialPostLimit;  //Max value admin nhập
        int standardPostLimit;
        int enterprisePostLimit;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class MediaData {
        // Dung lượng (Size)
        int maxImgSize;
        int maxDocSize;

        List<MediaFormat> imgFormats;   // Cho Avatar, Logo
        List<MediaFormat> docFormats;   // Cho Giấy phép kinh doanh
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class MediaFormat {
        String format;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class AdmissionQuotaData {
        Source source;
        List<Quota> quotas;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Source {
        String sourceName;
        String sourceUrl;
        String sourceType;
        String year;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Quota {
        int schoolId;
        int value;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class AdmissionSettingsData {
        List<AdmissionMethodDetail> allowedMethods;
        List<MethodAdmissionProcess> methodAdmissionProcess;
        DocumentRequirementsData documentRequirements;
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
    public static class MethodAdmissionProcess {
        String methodCode;
        List<StepDetail> steps;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class StepDetail {
        int stepOrder;
        String stepName;
        String description;
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
    public static class DocumentDetail {
        String code;
        String name;
        boolean required;
        boolean nonRemovable;
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
}
