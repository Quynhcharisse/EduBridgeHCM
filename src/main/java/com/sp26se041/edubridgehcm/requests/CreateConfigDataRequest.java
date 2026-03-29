package com.sp26se041.edubridgehcm.requests;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateConfigDataRequest {

    BusinessData businessData;

    MediaData mediaData;

    DesignData designData;

    ReportData reportData;

    SubscriptionData subscriptionData;

    AdmissionQuotaData admissionQuotaData;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class BusinessData {

        double taxRate; // VAT

        double serviceRate; // Phí dịch vụ mà nền tảng thu

        double minPay; // Số tiền thanh toán tối thiểu

        double maxPay; //Số tiền thanh toán tối đa
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class MediaData {

        int maxImgSize;

        int maxVideoSize;

        int maxDesignRefImg;

        int maxFeedbackImg;

        int maxFeedbackVideo;

        int maxReportImg;

        int maxReportVideo;

        List<MediaFormat> imgFormats;

        List<MediaFormat> videoFormats;
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
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class DesignData {
        String illustrationImage;
        List<LogoPosition> positions;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class LogoPosition {
        String position;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class SubscriptionData {
        int trialDays;      // Số ngày dùng thử mặc định
        int gracePeriod;    // Thời gian gia hạn (ân hạn) trước khi khóa
        double taxRate;     // Thuế VAT riêng cho dịch vụ phần mềm
        int minSubscriptionMonth; // Số tháng mua tối thiểu (VD: 3 tháng)
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class AdmissionQuotaData {
        String year;
        String sourceUrl;
        Map<Integer, Integer> quotas;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class ReportData {
        int maxDisbursementDay;
        List<SeverityLevel> levels;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class SeverityLevel {
        String name;
        String compensation;
    }
}
