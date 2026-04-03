package com.sp26se041.edubridgehcm.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CategoryPost {

    PLATFORM_NEWS("platform news"),
    DOET_NEWS("doet news"),
    SCHOOL_NEWS("school news");

    private final String value;
}
