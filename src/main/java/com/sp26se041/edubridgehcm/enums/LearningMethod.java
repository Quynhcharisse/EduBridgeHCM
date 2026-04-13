package com.sp26se041.edubridgehcm.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LearningMethod {

    PROJECT_BASED(
            "Project-Based Learning (PBL)",
            "Dạy học dựa trên dự án",
            "Học sinh thực hiện dự án thực tế để giải quyết vấn đề và nâng cao kỹ năng thực hành."
    ),

    COOPERATIVE(
            "Cooperative Learning",
            "Dạy học hợp tác",
            "Tăng cường làm việc nhóm, trao đổi và tương tác giữa các học sinh."
    ),

    EXPERIENTIAL(
            "Experiential Learning",
            "Dạy học qua trải nghiệm",
            "Học thông qua các hoạt động tham quan, thực hành và mô phỏng thực tế."
    ),

    PROBLEM_BASED(
            "Problem-Based Learning",
            "Dạy học giải quyết vấn đề",
            "Học sinh chủ động thảo luận tìm giải pháp cho các tình huống thực tế do giáo viên đưa ra."
    ),

    PERSONALIZED(
            "Personalized Learning",
            "Cá nhân hóa học tập",
            "Thiết kế lộ trình học tập riêng biệt phù hợp với năng lực và nhu cầu của từng học sinh."
    ),

    BLENDED(
            "Blended Learning",
            "Dạy học tích hợp/Trực tuyến",
            "Kết hợp giữa học tập truyền thống tại lớp và học trực tuyến qua ứng dụng công nghệ."
    ),

    VISUAL_PRACTICE(
            "Visual and Practical Learning",
            "Dạy học trực quan và thực hành",
            "Sử dụng hình ảnh, mô hình và luyện tập trực tiếp để tăng cường khả năng ghi nhớ."
    ),

    STEM_STEAM(
            "STEM/STEAM Integrated",
            "Tích hợp STEM/STEAM",
            "Học tập liên môn giữa Khoa học, Công nghệ, Kỹ thuật, Nghệ thuật và Toán học."
    );

    private final String value;        // Tên tiếng Anh kỹ thuật
    private final String displayName;  // Tên hiển thị tiếng Việt trên UI
    private final String description;  // Mô tả chi tiết phương pháp
}