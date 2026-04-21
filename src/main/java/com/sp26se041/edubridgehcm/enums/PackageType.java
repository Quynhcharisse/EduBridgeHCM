package com.sp26se041.edubridgehcm.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PackageType {
    TRIAL("gói dùng thử"),
    STANDARD("gói phổ thông"),
    ENTERPRISE("gói doanh nghiệp");
    private final String value;
}
