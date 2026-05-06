package com.sp26se041.edubridgehcm.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LanguageInstruction {
    VIETNAMESE("vietnamese"),
    ENGLISH("english"),
    FRENCH("french"),
    JAPANESE("japanese"),
    CHINESE("chinese"),
    KOREAN("korean"),
    GERMAN("german");
    private final String value;
}
