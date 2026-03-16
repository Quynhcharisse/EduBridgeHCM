package com.sp26se041.edubridgehcm.utils;

import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.models.School;

public class SchoolUtil {

    public static String checkSchoolStatus(School school) {

        if (school == null || school.getCampusList() == null || school.getCampusList().isEmpty()) {
            return Status.ACCOUNT_INACTIVE.name();
        }

        return school.getCampusList().stream()
                .anyMatch(campus -> campus.getStatus() == Status.ACCOUNT_ACTIVE)
                ? Status.ACCOUNT_INACTIVE.name()
                : Status.ACCOUNT_ACTIVE.name();
    }
}
