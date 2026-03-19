package com.sp26se041.edubridgehcm.requests;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.NullMarked;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@NullMarked
public class CreatePersonalityTypeRequest {

    String code;

    String name;

    String description;

    QuoteInfo quoteInfo;

    List<TraitInfo> traits;

    List<String> strengths;

    List<String> weaknesses; // 👉 sửa lại cho đúng naming

    List<SourceInfo> sources;

    List<CareerInfo> recommendedCareers;

    // ================== INNER CLASSES ==================

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class TraitInfo implements Serializable {
        String name;
        String description;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class SourceInfo implements Serializable {
        String title;
        String url;
    }
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class QuoteInfo implements Serializable {
        String content;
        String author;
    }
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class CareerInfo implements Serializable {
        String name;
        String explainText;
    }
}







