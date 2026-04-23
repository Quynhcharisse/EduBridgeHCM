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
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CurriculumRequest {

    Integer curriculumId;

    String subTypeName;

    String description;

    String curriculumType;

    List<String> methodLearningList;

    List<SubjectOptionRequest> subjectOptions;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class SubjectOptionRequest {

        Boolean isMandatory; // môn học bắt buộc hay tùy chọn

        String name;

        String description;
    }
}
