package com.sp26se041.edubridgehcm.enums;

public enum DevicePlatform { 
    //FCM token đó thuộc loại thiết bị nào. WEB, ANDROID, IOS
    // lợi ích : Lọc đúng kênh gửi (ví dụ chỉ gửi web trước). 
    // Debug dễ hơn khi lỗi theo nền tảng.
    //  Mở rộng đa nền tảng mà không đổi schema. 
    // Dễ làm rule khác nhau theo platform (payload/web route khác mobile deep link).
    WEB,
    ANDROID,
    IOS
}
