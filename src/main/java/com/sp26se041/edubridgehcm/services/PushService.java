package com.sp26se041.edubridgehcm.services;

import com.sp26se041.edubridgehcm.models.Account;
import com.sp26se041.edubridgehcm.models.Notifications;

public interface PushService {
    void sendToUser(Account user, Notifications notification);
}
