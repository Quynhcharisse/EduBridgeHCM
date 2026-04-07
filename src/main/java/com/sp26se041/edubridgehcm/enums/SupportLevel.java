package com.sp26se041.edubridgehcm.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SupportLevel {

    BASIC("Hỗ trợ qua Email hoặc gọi điện hotline của trường"),

    STANDARD("Nhắn tin với tư vấn viên hỗ trợ trực tiếp 24/7"),

    ENTERPRISE("Hệ thống AI thông minh & Chuyên viên tư vấn cấp cao");

    private final String value;
}
