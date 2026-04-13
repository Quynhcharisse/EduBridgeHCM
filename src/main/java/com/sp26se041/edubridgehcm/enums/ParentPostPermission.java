package com.sp26se041.edubridgehcm.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ParentPostPermission {
    NONE("none"), // Không có quyền gì (thậm chí không thấy mục cộng đồng)
    VIEW_ONLY("view only"), // Chỉ được xem bài viết của trường
    CREATE_POST("create post"); // Được phép đăng bài mới
    private final String value;
}
