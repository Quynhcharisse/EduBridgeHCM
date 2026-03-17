package com.sp26se041.edubridgehcm.utils;

import com.sp26se041.edubridgehcm.models.Account;

public final class AccountRestrictionUtil {

    private AccountRestrictionUtil() {
    }

    public static boolean isRestrictedActor() {
        return isRestricted(AuthRequestUtil.extractAuthenticatedAccount());
    }

    public static boolean isRestricted(Account account) {
        return account != null && account.isRestricted();
    }
}


