package com.sp26se041.edubridgehcm.services.implementors;

import com.sp26se041.edubridgehcm.enums.DevicePlatform;
import com.sp26se041.edubridgehcm.enums.NotificationEventType;
import com.sp26se041.edubridgehcm.enums.Role;
import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.models.Account;
import com.sp26se041.edubridgehcm.models.DeviceTokens;
import com.sp26se041.edubridgehcm.models.NotificationRecipients;
import com.sp26se041.edubridgehcm.models.Notifications;
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
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private static final Map<NotificationEventType, EventTemplateConfig> EVENT_TEMPLATE_CONFIGS =
            buildEventTemplateConfigs();


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
    public void publish(NotificationEventType eventType, Account actor, Map<String, Object> contextData) {
        if (eventType == null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        EventTemplateConfig config = EVENT_TEMPLATE_CONFIGS.get(eventType);
        if (config == null) {
            return;
        }

        NotificationTemplate template = buildTemplate(config, eventType, actor, contextData);
        if (template == null) {
            return;
        }

        List<Role> recipientRoles = config.recipientRoles();
        if (recipientRoles.isEmpty()) {
            return;
        }

        Notifications notification = Notifications.builder()
                .eventType(eventType)
                .actorUser(actor)
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

    private NotificationTemplate buildTemplate(EventTemplateConfig config,
                                               NotificationEventType eventType,
                                               Account actor,
                                               Map<String, Object> contextData) {
        String actorName = resolveActorName(eventType, actor, contextData);
        String packageName = resolvePackageName(contextData);
        String title = config.titleTemplate()
                .replace("{actorName}", actorName)
                .replace("{packageName}", packageName);
        String body = config.bodyTemplate()
                .replace("{actorName}", actorName)
                .replace("{packageName}", packageName);

        Map<String, Object> payload = new HashMap<>();
        payload.put("eventType", eventType.name());
        payload.put("route", config.route());
        payload.put("actorName", actorName);
        if (contextData != null && !contextData.isEmpty()) {
            payload.putAll(contextData);
        }

        return new NotificationTemplate(title, body, payload);
    }

    private String resolveSchoolName(Account actor) {
        if (actor != null && actor.getCampus() != null && actor.getCampus().getSchool() != null) {
            return actor.getCampus().getSchool().getName();
        }
        return "Trường học";
    }

    private String resolveAdminName(Account actor) {
        if (actor != null && Role.ADMIN.equals(actor.getRole())) {
            return "Hệ thống EduBridge";
        }
        return "Quản trị viên";
    }

    private String resolvePackageName(Map<String, Object> contextData) {
        if (contextData == null || contextData.isEmpty()) {
            return "gói dịch vụ";
        }
        Object packageName = contextData.get("packageName");
        if (packageName == null || packageName.toString().isBlank()) {
            return "gói dịch vụ";
        }
        return packageName.toString().trim();
    }

    private String resolveActorName(NotificationEventType eventType, Account actor, Map<String, Object> contextData) {
        if (contextData != null) {
            Object actorName = contextData.get("actorName");
            if (actorName != null && !actorName.toString().isBlank()) {
                return actorName.toString().trim();
            }
        }

        if (eventType == NotificationEventType.SCHOOL_POST_PUBLISHED) {
            return resolveSchoolName(actor);
        }

        if (eventType == NotificationEventType.ADMIN_POST_PUBLISHED) {
            return resolveAdminName(actor);
        }

        if (eventType == NotificationEventType.NEW_USER_REGISTERED) {
            return resolveAdminName(actor);
        }

        if (eventType == NotificationEventType.BUY_PACKAGE_FEE) {
            return resolveSchoolName(actor);
        }

        if (eventType == NotificationEventType.CREATE_PACKAGE_FEE) {
            return resolveAdminName(actor);
        }

        return "Hệ thống EduBridge";
    }

    private static Map<NotificationEventType, EventTemplateConfig> buildEventTemplateConfigs() {
        Map<NotificationEventType, EventTemplateConfig> configs = new EnumMap<>(NotificationEventType.class);
        configs.put(
                NotificationEventType.SCHOOL_POST_PUBLISHED,
                new EventTemplateConfig(
                        "Bài viết mới từ {actorName}",
                        "{actorName} vừa đăng bài mới",
                        "/posts",
                        List.of(Role.ADMIN, Role.PARENT)
                )
        );
        configs.put(
                NotificationEventType.ADMIN_POST_PUBLISHED,
                new EventTemplateConfig(
                        "Bài viết mới từ {actorName}",
                        "{actorName} vừa đăng bài mới",
                        "/posts",
                        List.of(Role.SCHOOL, Role.PARENT)
                )
        );
        configs.put(
                NotificationEventType.NEW_USER_REGISTERED,
                new EventTemplateConfig(
                        "Người dùng mới đăng ký",
                        "{actorName} vừa đăng ký tài khoản mới",
                        "/admin/users",
                        List.of(Role.ADMIN)
                )
        );

        configs.put(
                NotificationEventType.BUY_PACKAGE_FEE,
                new EventTemplateConfig(
                        "Giao dịch gói dịch vụ mới",
                        "{actorName} vừa thanh toán đăng ký gói {packageName}.",
                        "/admin/transaction-statistics",
                        List.of(Role.ADMIN)
                )
        );

        configs.put(
                NotificationEventType.CREATE_PACKAGE_FEE,
                new EventTemplateConfig(
                        "Tạo gói doanh nghiệp",
                        "{actorName} vừa tạo gói doanh nghiệp mới {packageName}.",
                        "/admin/package-fees",
                        List.of(Role.SCHOOL)
                )
        );
        return configs;
    }

    private record EventTemplateConfig(String titleTemplate, String bodyTemplate, String route,
                                       List<Role> recipientRoles) {
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
