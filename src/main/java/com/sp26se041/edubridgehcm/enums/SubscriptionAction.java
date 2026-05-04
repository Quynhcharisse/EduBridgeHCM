package com.sp26se041.edubridgehcm.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SubscriptionAction {
    BUY_NEW("mua gói mới"),
    SWITCH_PLAN("chuyển gói"),
    RENEW("gia hạn");
    private final String value;
}
