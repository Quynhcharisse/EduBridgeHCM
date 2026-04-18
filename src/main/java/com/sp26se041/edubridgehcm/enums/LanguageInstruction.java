package com.sp26se041.edubridgehcm.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LanguageInstruction {
    VIETNAMESE("vietnamese"),   // Dạy bằng Tiếng Việt 100%
    ENGLISH("english"),         // Dạy bằng Tiếng Anh 100% (Thường là hệ Quốc tế)
    FRENCH("french"),           // Tiếng Pháp
    JAPANESE("japanese"),       // Tiếng Nhật
    CHINESE("chinese"),         // Tiếng Trung
    KOREAN("korean"),
    GERMAN("german");           // Tiếng Hàn
    private final String value;
}
