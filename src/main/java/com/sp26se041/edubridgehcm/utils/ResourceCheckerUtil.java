package com.sp26se041.edubridgehcm.utils;

import com.sp26se041.edubridgehcm.enums.ResourceType;
import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.repositories.CampusResourceQuotaRepo;

public class ResourceCheckerUtil {

    //kiem tra de lock tinh nang
    public static Status checkAccessStatus(int campusId, ResourceType type, CampusResourceQuotaRepo campusResourceQuotaRepo, long currentUsage) {

        var quotaOpt = campusResourceQuotaRepo.findByCampusIdAndResourceType(campusId, type);

        if (quotaOpt.isEmpty()) {
            return Status.FEATURE_LOCKED_NO_PACKAGE;
        }

        if (currentUsage >= quotaOpt.get().getMaxQuota()) {
            return Status.FEATURE_LOCKED_QUOTA_FULL;
        }

        return Status.FEATURE_AVAILABLE;
    }
}
