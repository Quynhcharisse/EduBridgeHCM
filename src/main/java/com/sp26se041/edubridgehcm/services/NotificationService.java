package com.sp26se041.edubridgehcm.services;

import com.sp26se041.edubridgehcm.enums.NotificationEventType;
import com.sp26se041.edubridgehcm.models.Post;
import com.sp26se041.edubridgehcm.requests.RegisterDeviceTokenRequest;
import com.sp26se041.edubridgehcm.requests.RemoveDeviceTokenRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import org.springframework.http.ResponseEntity;
import java.util.Map;

public interface NotificationService {
    ResponseEntity<ResponseObject> registerDeviceToken(RegisterDeviceTokenRequest request);

    ResponseEntity<ResponseObject> removeDeviceToken(RemoveDeviceTokenRequest request);

    ResponseEntity<ResponseObject> getMyNotifications(int page, int pageSize);

    ResponseEntity<ResponseObject> getMyUnreadCount();

    ResponseEntity<ResponseObject> markAsRead(Integer recipientId);

    void publish(NotificationEventType eventType, Post post, Map<String, Object> extraData);
}
