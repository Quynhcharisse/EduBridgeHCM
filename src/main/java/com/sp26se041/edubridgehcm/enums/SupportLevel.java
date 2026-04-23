package com.sp26se041.edubridgehcm.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SupportLevel {
    BASIC_SUPPORT("Phản hồi qua email support and hotline"),
    STANDARD_SUPPORT("AI Chatbot (Cơ bản) - Trả lời dựa trên kho FAQ của trường"),
    PREMIUM_SUPPORT("AI Chatbot (Cao cấp) & Có kênh hỗ trợ từ phía Admin hệ thông & Có thêm phân tích nhu cầu học sinh theo các tiêu chí của hệ thốn");
    private final String value;
}
