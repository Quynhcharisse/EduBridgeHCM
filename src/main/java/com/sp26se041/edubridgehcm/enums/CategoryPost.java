package com.sp26se041.edubridgehcm.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CategoryPost {
    SYSTEM_NOTIFICATIONS("Thông báo hệ thống"),
    GENERAL_EDUCATION_NEWS("Tin tức giáo dục chung"),

    CAMPUS_ADMISSION("Tin tuyển sinh"),
    CAMPUS_EVENTS("Sự kiện của trường"),
    CAMPUS_SCHOLARSHIP("Thông tin học bổng");
    private final String value;
}
