package com.sp26se041.edubridgehcm.utils;

import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.models.School;

public class SchoolUtil {

    public static String checkSchoolStatus(School school) {
        return school.getCampusList().stream()
                .noneMatch(campus -> campus.getStatus().equals(Status.ACCOUNT_ACTIVE))
                ? Status.ACCOUNT_INACTIVE.getValue()
                : Status.ACCOUNT_ACTIVE.getValue();
    }
}
