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

    AdmissionQuotaData admissionQuotaData;

    AdmissionSettingsData admissionSettingsData;

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

        // Dung lượng (Size)
        int maxImgSize;
        int maxVideoSize;
        int maxDocSize;

        List<MediaFormat> imgFormats;   // Cho Avatar, Logo
        List<MediaFormat> videoFormats; // Cho Video giới thiệu trường
        List<MediaFormat> docFormats;   // Cho Giấy phép kinh doanh (PDF, DOCX)
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
        String year;
        String sourceUrl;
        Map<Integer, Integer> quotas;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class AdmissionSettingsData {
        List<AdmissionMethodDetail> allowedMethods;
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
}
