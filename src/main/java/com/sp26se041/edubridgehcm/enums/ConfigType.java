package com.sp26se041.edubridgehcm.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ConfigType {
    HOLIDAY("holiday"),
    MAINTENANCE("maintenance"),
    SPECIAL_EVENT("special event"),
    OTHER("other");

    private final String value;
}
