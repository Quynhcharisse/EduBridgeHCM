package com.sp26se041.edubridgehcm.services.implementors;

import com.sp26se041.edubridgehcm.enums.DevicePlatform;
import com.sp26se041.edubridgehcm.enums.NotificationEventType;
import com.sp26se041.edubridgehcm.enums.Role;
import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.models.Account;
import com.sp26se041.edubridgehcm.models.DeviceTokens;
import com.sp26se041.edubridgehcm.models.NotificationRecipients;
import com.sp26se041.edubridgehcm.models.Notifications;
import com.sp26se041.edubridgehcm.models.Post;
import com.sp26se041.edubridgehcm.repositories.AccountRepo;
import com.sp26se041.edubridgehcm.repositories.DeviceTokensRepo;
import com.sp26se041.edubridgehcm.repositories.NotificationRecipientsRepo;
import com.sp26se041.edubridgehcm.repositories.NotificationsRepo;
import com.sp26se041.edubridgehcm.requests.RegisterDeviceTokenRequest;
import com.sp26se041.edubridgehcm.requests.RemoveDeviceTokenRequest;
import com.sp26se041.edubridgehcm.responses.PageResponse;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.NotificationService;
import com.sp26se041.edubridgehcm.utils.AuthRequestUtil;
import com.sp26se041.edubridgehcm.utils.PaginationUtil;
import com.sp26se041.edubridgehcm.utils.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final AccountRepo accountRepo;

    private final DeviceTokensRepo deviceTokensRepo;

    private final NotificationsRepo notificationsRepo;

    private final NotificationRecipientsRepo notificationRecipientsRepo;

    @Override
    @Transactional
    public ResponseEntity<ResponseObject> registerDeviceToken(RegisterDeviceTokenRequest request) {
        Account actor = AuthRequestUtil.extractAuthenticatedAccount();
        if (actor == null) {
            return ResponseBuilder.build(HttpStatus.UNAUTHORIZED, "Không tìm thấy thông tin đăng nhập", null);
        }

        if (request == null || request.getToken() == null || request.getToken().isBlank()) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Token không được để trống", null);
        }

        String token = request.getToken().trim();
        DevicePlatform platform = parsePlatform(request.getPlatform());
        if (platform == null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Platform không hợp lệ", null);
        }

        LocalDateTime now = LocalDateTime.now();
        DeviceTokens deviceToken = deviceTokensRepo.findByToken(token).orElse(
                DeviceTokens.builder()
                        .token(token)
                        .createdAt(now)
                        .build()
        );

        deviceToken.setUser(actor);
        deviceToken.setPlatform(platform);
        deviceToken.setActive(true);
        deviceToken.setLastSeenAt(now);
        deviceToken.setUpdatedAt(now);
        deviceTokensRepo.save(deviceToken);

        return ResponseBuilder.build(HttpStatus.OK, "Đăng ký thiết bị nhận thông báo thành công", null);
    }

    @Override
    @Transactional
    public ResponseEntity<ResponseObject> removeDeviceToken(RemoveDeviceTokenRequest request) {
        Account actor = AuthRequestUtil.extractAuthenticatedAccount();
        if (actor == null) {
            return ResponseBuilder.build(HttpStatus.UNAUTHORIZED, "Không tìm thấy thông tin đăng nhập", null);
        }

        if (request == null || request.getToken() == null || request.getToken().isBlank()) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Token không được để trống", null);
        }

        String token = request.getToken().trim();
        DeviceTokens deviceToken = deviceTokensRepo.findByTokenAndIsActiveTrue(token).orElse(null);
        if (deviceToken != null && deviceToken.getUser() != null && actor.getId().equals(deviceToken.getUser().getId())) {
            deviceToken.setActive(false);
            deviceToken.setUpdatedAt(LocalDateTime.now());
            deviceTokensRepo.save(deviceToken);
        }

        return ResponseBuilder.build(HttpStatus.OK, "Hủy đăng ký thiết bị thành công", null);
    }

    @Override
    public ResponseEntity<ResponseObject> getMyNotifications(int page, int pageSize) {
        Account actor = AuthRequestUtil.extractAuthenticatedAccount();
        if (actor == null) {
            return ResponseBuilder.build(HttpStatus.UNAUTHORIZED, "Không tìm thấy thông tin đăng nhập", null);
        }

        try {
            var pageable = PaginationUtil.buildPageRequest(page, pageSize);
            var notificationPage = notificationRecipientsRepo.findByRecipientUserIdOrderByCreatedAtDesc(actor.getId(), pageable);
            PageResponse<Map<String, Object>> response = PaginationUtil.buildPageResponse(notificationPage, this::buildNotificationItem);
            return ResponseBuilder.build(HttpStatus.OK, "Lấy danh sách thông báo thành công", response);
        } catch (IllegalArgumentException e) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, e.getMessage(), null);
        }
    }

    @Override
    public ResponseEntity<ResponseObject> getMyUnreadCount() {
        Account actor = AuthRequestUtil.extractAuthenticatedAccount();
        if (actor == null) {
            return ResponseBuilder.build(HttpStatus.UNAUTHORIZED, "Không tìm thấy thông tin đăng nhập", null);
        }

        long unreadCount = notificationRecipientsRepo.countByRecipientUserIdAndIsReadFalse(actor.getId());
        Map<String, Object> body = new HashMap<>();
        body.put("unreadCount", unreadCount);
        return ResponseBuilder.build(HttpStatus.OK, "Lấy số lượng thông báo chưa đọc thành công", body);
    }

    @Override
    @Transactional
    public ResponseEntity<ResponseObject> markAsRead(Integer recipientId) {
        Account actor = AuthRequestUtil.extractAuthenticatedAccount();
        if (actor == null) {
            return ResponseBuilder.build(HttpStatus.UNAUTHORIZED, "Không tìm thấy thông tin đăng nhập", null);
        }

        if (recipientId == null || recipientId <= 0) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Mã thông báo nhận không hợp lệ", null);
        }

        NotificationRecipients recipient = notificationRecipientsRepo.findById(recipientId).orElse(null);
        if (recipient == null || recipient.getRecipientUser() == null
                || !actor.getId().equals(recipient.getRecipientUser().getId())) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Không tìm thấy thông báo", null);
        }

        if (!recipient.isRead()) {
            recipient.setRead(true);
            recipient.setReadAt(LocalDateTime.now());
            notificationRecipientsRepo.save(recipient);
        }

        return ResponseBuilder.build(HttpStatus.OK, "Đánh dấu đã đọc thành công", null);
    }

    @Override
    @Transactional
    public void publish(NotificationEventType eventType, Post post, Map<String, Object> extraData) {
        if (eventType == null || post == null || post.getAuthor() == null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        NotificationTemplate template = buildTemplate(eventType, post, extraData);
        if (template == null) {
            return;
        }

        List<Role> recipientRoles = resolveRecipientRoles(eventType);
        if (recipientRoles.isEmpty()) {
            return;
        }

        Notifications notification = Notifications.builder()
                .eventType(eventType)
                .actorUser(post.getAuthor())
                .title(template.title())
                .body(template.body())
                .data(template.payload())
                .createdAt(now)
                .build();
        notificationsRepo.save(notification);

        for (Role role : recipientRoles) {
            createRecipientsForRole(notification, role, now);
        }
    }

    private DevicePlatform parsePlatform(String platformRaw) {
        if (platformRaw == null || platformRaw.isBlank()) {
            return DevicePlatform.WEB;
        }
        try {
            return DevicePlatform.valueOf(platformRaw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private void createRecipientsForRole(Notifications notification, Role role, LocalDateTime now) {
        for (Account recipient : accountRepo.findByRole(role)) {
            NotificationRecipients item = NotificationRecipients.builder()
                    .notification(notification)
                    .recipientUser(recipient)
                    .deliveryStatus(Status.NOTIFICATION_SENT)
                    .deliveredAt(now)
                    .isRead(false)
                    .createdAt(now)
                    .build();
            notificationRecipientsRepo.save(item);
        }
    }

    private String resolveSchoolName(Post post) {
        if (post.getAuthor().getCampus() != null && post.getAuthor().getCampus().getSchool() != null) {
            return post.getAuthor().getCampus().getSchool().getName();
        }
        return "Truong hoc";
    }

    private List<Role> resolveRecipientRoles(NotificationEventType eventType) {
        List<Role> roles = new ArrayList<>();
        switch (eventType) {
            case SCHOOL_POST_PUBLISHED -> {
                roles.add(Role.ADMIN);
                roles.add(Role.PARENT);
            }
            default -> {
            }
        }
        return roles;
    }

    private NotificationTemplate buildTemplate(NotificationEventType eventType, Post post, Map<String, Object> extraData) {
        if (eventType == NotificationEventType.SCHOOL_POST_PUBLISHED) {
            String schoolName = resolveSchoolName(post);
            Map<String, Object> payload = new HashMap<>();
            payload.put("eventType", NotificationEventType.SCHOOL_POST_PUBLISHED.name());
            payload.put("route", "/posts");
            payload.put("postId", post.getId());
            payload.put("schoolName", schoolName);
            if (extraData != null && !extraData.isEmpty()) {
                payload.putAll(extraData);
            }
            return new NotificationTemplate("Bai viet moi tu truong", schoolName + " vua dang bai moi", payload);
        }
        return null;
    }

    private record NotificationTemplate(String title, String body, Map<String, Object> payload) {
    }

    private Map<String, Object> buildNotificationItem(NotificationRecipients recipient) {
        Map<String, Object> data = new HashMap<>();
        data.put("recipientId", recipient.getId());
        data.put("notificationId", recipient.getNotification().getId());
        data.put("eventType", recipient.getNotification().getEventType());
        data.put("title", recipient.getNotification().getTitle());
        data.put("body", recipient.getNotification().getBody());
        data.put("data", recipient.getNotification().getData());
        data.put("deliveryStatus", recipient.getDeliveryStatus());
        data.put("deliveredAt", recipient.getDeliveredAt());
        data.put("isRead", recipient.isRead());
        data.put("readAt", recipient.getReadAt());
        data.put("createdAt", recipient.getCreatedAt());
        return data;
    }
}
