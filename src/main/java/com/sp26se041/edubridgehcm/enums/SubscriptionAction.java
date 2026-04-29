package com.sp26se041.edubridgehcm.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SubscriptionAction {
    BUY_NEW("buy new"),
    UPGRADE("nâng cấp"),
    RENEW("gia hạn");
    private final String value;
}
