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
public class UpdateStudentInfoRequest {

    int studentId;
    String studentName;
    String studentCode;
    String gender;
    String personalityTypeCode;
    String favouriteJob;
    List<UpdateStudentInfoRequest.AcademicInfo> academicInfos;
    List<UpdateStudentInfoRequest.TranscriptImage> transcriptImages;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class AcademicInfo {
        List<UpdateStudentInfoRequest.SubjectResult>  subjectResults;
        String gradeLevel;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class TranscriptImage {
        String grade;
        String imageUrl;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class SubjectResult {
        String subjectName;
        Double score;
    }
}
