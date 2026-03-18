package com.sp26se041.edubridgehcm.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LearningMethod {

    TRADITIONAL("Traditional Learning", "Focus on lectures and textbooks"),
    PROJECT_BASED("Project-Based Learning (PBL)", "Learning through real-world projects"),
    INQUIRY_BASED("Inquiry-Based Learning", "Driven by student questions and research"),
    STEM_STEAM("STEM/STEAM Integrated", "Focus on Science, Tech, Engineering, Arts, Math");

    private final String value;

    private final String description;
}
