package com.sp26se041.edubridgehcm.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LearningMethod {
    TRADITIONAL("Traditional Learning", "Focus on lectures and textbooks"), //Học tập truyền thống
    PROJECT_BASED("Project-Based Learning (PBL)", "Learning through real-world projects"), //Học tập qua dự án
    INQUIRY_BASED("Inquiry-Based Learning", "Driven by student questions and research"), //Học tập truy vấn
    STEM_STEAM("STEM/STEAM Integrated", "Focus on Science, Tech, Engineering, Arts, Math"); //Tích hợp liên môn

    private final String value;

    private final String description;
}
