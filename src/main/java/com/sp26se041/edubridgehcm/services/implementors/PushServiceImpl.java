package com.sp26se041.edubridgehcm.services.implementors;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.sp26se041.edubridgehcm.models.Account;
import com.sp26se041.edubridgehcm.models.DeviceTokens;
import com.sp26se041.edubridgehcm.models.Notifications;
import com.sp26se041.edubridgehcm.repositories.DeviceTokensRepo;
import com.sp26se041.edubridgehcm.services.PushService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PushServiceImpl implements PushService {

    private final DeviceTokensRepo deviceTokensRepo;

    @Override
    public void sendToUser(Account user, Notifications notification) {
        List<DeviceTokens> devices = deviceTokensRepo.findByUserIdAndIsActiveTrue(user.getId());

        for (DeviceTokens device : devices) {
            try {
                sendFCM(device.getToken(), notification);
            } catch (FirebaseMessagingException e) {
                if (e.getMessagingErrorCode() != null &&
                        (e.getMessagingErrorCode().name().equals("UNREGISTERED")
                                || e.getMessagingErrorCode().name().equals("INVALID_ARGUMENT"))) {

                    device.setActive(false);
                    deviceTokensRepo.save(device);
                }
            }
        }
    }

    private void sendFCM(String token, Notifications notification) throws FirebaseMessagingException {
        Message message = Message.builder()
                .setToken(token)
                .setNotification(
                        Notification.builder()
                                .setTitle(notification.getTitle())
                                .setBody(notification.getBody())
                                .build()
                )
                .putData("notificationId", String.valueOf(notification.getId()))
                .putData("eventType", notification.getEventType().name())
                .build();

        FirebaseMessaging.getInstance().send(message);
    }
}
