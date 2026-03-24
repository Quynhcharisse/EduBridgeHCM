package com.sp26se041.edubridgehcm.requests;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateConfigDataRequest {

    BusinessData businessData;

    MediaData mediaData;

    DesignData designData;

    ReportData reportData;

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
