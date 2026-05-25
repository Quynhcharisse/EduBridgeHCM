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
import com.sp26se041.edubridgehcm.services.PushService;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private static final Map<NotificationEventType, EventTemplateConfig> EVENT_TEMPLATE_CONFIGS = buildEventTemplateConfigs();

    private final AccountRepo accountRepo;

    private final DeviceTokensRepo deviceTokensRepo;

    private final NotificationsRepo notificationsRepo;

    private final NotificationRecipientsRepo notificationRecipientsRepo;

    private final PushService pushService;

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
        if (deviceToken != null && actor.getId().equals(deviceToken.getUser().getId())) {
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
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void publish(NotificationEventType eventType, Account actor, Map<String, Object> contextData) {
        if (eventType == null) return;

        LocalDateTime now = LocalDateTime.now();
        EventTemplateConfig config = EVENT_TEMPLATE_CONFIGS.get(eventType);
        if (config == null) return;

        // 1. RESOLVE RECIPIENTS
        // Với REQUIRES_NEW, các Account từ TX ngoài đã bị detach → phải re-fetch bằng ID
        Set<Account> recipients = new HashSet<>();
        if (contextData != null && contextData.get("specificRecipients") instanceof List<?> specificList) {
            List<Integer> ids = new java.util.ArrayList<>();
            for (Object obj : specificList) {
                if (obj instanceof Account acc && acc.getId() != null) {
                    ids.add(acc.getId());
                }
            }
            if (!ids.isEmpty()) {
                recipients.addAll(accountRepo.findAllById(ids));
            }
        } else {
            for (Role role : config.recipientRoles()) {
                recipients.addAll(accountRepo.findByRole(role));
            }
        }

        if (recipients.isEmpty()) return;

        // Re-attach actor nếu có (cũng bị detach khi REQUIRES_NEW)
        Account freshActor = null;
        if (actor != null && actor.getId() != null) {
            freshActor = accountRepo.findById(actor.getId()).orElse(null);
        }

        // 2. TẠO DỮ LIỆU ĐỂ LƯU VÀO DATABASE (PERSISTENCE DATA)
        // Quan trọng: Phải loại bỏ các Object không Serializable như Account, List<Account>
        Map<String, Object> cleanContext = new HashMap<>();
        if (contextData != null) {
            contextData.forEach((key, value) -> {
                // Chỉ giữ lại các kiểu dữ liệu cơ bản để lưu vào cột JSON của DB
                if (!(value instanceof Account) && !(value instanceof Iterable)) {
                    cleanContext.put(key, value);
                }
            });
        }

        // Build template dựa trên dữ liệu đã làm sạch
        NotificationTemplate template = buildTemplate(config, eventType, freshActor, cleanContext);

        // 3. LƯU THÔNG BÁO VÀO BẢNG NOTIFICATIONS
        Notifications notification = Notifications.builder()
                .eventType(eventType)
                .actorUser(freshActor)   // null-safe: actor là optional
                .title(template.title())
                .body(template.body())
                .data(template.payload())
                .createdAt(now)
                .build();

        notificationsRepo.save(notification);

        for (Account recipient : recipients) {
            NotificationRecipients item = NotificationRecipients.builder()
                    .notification(notification)
                    .recipientUser(recipient)
                    .deliveryStatus(Status.NOTIFICATION_PENDING)
                    .isRead(false)
                    .createdAt(now)
                    .build();
            notificationRecipientsRepo.save(item);

            try {
                pushService.sendToUser(recipient, notification);
                item.setDeliveryStatus(Status.NOTIFICATION_SENT);
                item.setDeliveredAt(LocalDateTime.now());
            } catch (Exception e) {
                item.setDeliveryStatus(Status.NOTIFICATION_FAILED);
            }
            notificationRecipientsRepo.save(item);
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

    private NotificationTemplate buildTemplate(EventTemplateConfig config,
                                               NotificationEventType eventType,
                                               Account actor,
                                               Map<String, Object> contextData) {
        String actorName = resolveActorName(eventType, actor, contextData);
        String packageName = resolvePackageName(contextData);
        String parentName = (contextData != null && contextData.get("parentName") != null)
                ? contextData.get("parentName").toString() : "Phụ huynh";
        String appointmentDate = (contextData != null && contextData.get("appointmentDate") != null)
                ? contextData.get("appointmentDate").toString() : "";

        String title = config.titleTemplate()
                .replace("{actorName}", actorName)
                .replace("{packageName}", packageName)
                .replace("{parentName}", parentName)
                .replace("{appointmentDate}", appointmentDate);

        String body = config.bodyTemplate()
                .replace("{actorName}", actorName)
                .replace("{packageName}", packageName)
                .replace("{parentName}", parentName)
                .replace("{appointmentDate}", appointmentDate);

        Map<String, Object> payload = new HashMap<>();
        payload.put("eventType", eventType.name());
        payload.put("route", config.route());
        payload.put("actorName", actorName);
        payload.put("parentName", parentName);
        if (contextData != null && !contextData.isEmpty()) {
            payload.putAll(contextData);
        }

        return new NotificationTemplate(title, body, payload);
    }

    private String resolveSchoolName(Account actor) {
        if (actor != null && actor.getCampus().getSchool() != null) {
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

    private String resolveParentName(Account actor) {
        if (actor != null && Role.PARENT.equals(actor.getRole())) {
            com.sp26se041.edubridgehcm.models.Parent p = actor.getParent();
            if (p != null && p.getName() != null && !p.getName().isBlank()) {
                return p.getName();
            }
            return actor.getEmail() != null ? actor.getEmail() : "Một phụ huynh";
        }
        return "Một phụ huynh";
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

        if (eventType == NotificationEventType.FAVORITE_SCHOOL
                || eventType == NotificationEventType.REMOVE_FAVORITE_SCHOOL
                || eventType == NotificationEventType.CONSULTATION_BOOKED) {
            return resolveParentName(actor);
        }

        // Consultation events directed at parent → show campus name
        if (eventType == NotificationEventType.CONSULTATION_CONFIRMED
                || eventType == NotificationEventType.CONSULTATION_CANCELLED
                || eventType == NotificationEventType.CONSULTATION_COMPLETED
                || eventType == NotificationEventType.CONSULTATION_NO_SHOW) {
            if (contextData != null && contextData.get("campusName") != null) {
                return contextData.get("campusName").toString();
            }
            return "Cơ sở trường";
        }

        // Counsellor slot assign/unassign → show campus name
        if (eventType == NotificationEventType.COUNSELLOR_SLOT_ASSIGNED
                || eventType == NotificationEventType.COUNSELLOR_SLOT_UNASSIGNED) {
            if (contextData != null && contextData.get("campusName") != null) {
                return contextData.get("campusName").toString();
            }
            return resolveSchoolName(actor);
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

        configs.put(
                NotificationEventType.FAVORITE_SCHOOL,
                new EventTemplateConfig(
                        "Phụ huynh mới quan tâm",
                        "{actorName} vừa thêm trường bạn vào danh sách yêu thích.",
                        "/school/parents-interest",
                        List.of()
                )
        );

        configs.put(
                NotificationEventType.REMOVE_FAVORITE_SCHOOL,
                new EventTemplateConfig(
                        "Phụ huynh bỏ quan tâm trường",
                        "{actorName} đã xoá trường bạn khỏi danh sách yêu thích.",
                        "/school/parents-interest",
                        List.of()
                )
        );

        configs.put(
                NotificationEventType.CONSULTATION_BOOKED,
                new EventTemplateConfig(
                        "Lịch hẹn tư vấn mới",
                        "{actorName} vừa đặt lịch tư vấn tại cơ sở vào ngày {appointmentDate}.",
                        "/school/counselor-schedule",
                        List.of()
                )
        );

        configs.put(
                NotificationEventType.CONSULTATION_CONFIRMED,
                new EventTemplateConfig(
                        "Lịch hẹn đã được xác nhận",
                        "Lịch tư vấn ngày {appointmentDate} của bạn đã được xác nhận. Vui lòng đến đúng giờ.",
                        "/parent/offline-consultations",
                        List.of()
                )
        );

        configs.put(
                NotificationEventType.CONSULTATION_CANCELLED,
                new EventTemplateConfig(
                        "Lịch hẹn tư vấn bị huỷ",
                        "Lịch tư vấn ngày {appointmentDate} của bạn đã bị huỷ.",
                        "/parent/offline-consultations",
                        List.of()
                )
        );

        configs.put(
                NotificationEventType.CONSULTATION_COMPLETED,
                new EventTemplateConfig(
                        "Buổi tư vấn đã hoàn thành",
                        "Buổi tư vấn ngày {appointmentDate} của bạn đã kết thúc. Cảm ơn bạn đã tin tưởng.",
                        "/parent/offline-consultations",
                        List.of()
                )
        );

        configs.put(
                NotificationEventType.CONSULTATION_NO_SHOW,
                new EventTemplateConfig(
                        "Vắng mặt buổi tư vấn",
                        "Bạn đã không đến buổi tư vấn ngày {appointmentDate}. Vui lòng liên hệ cơ sở để được hỗ trợ.",
                        "/parent/offline-consultations",
                        List.of()
                )
        );

        // ── Counsellor slot assignment ─────────────────────────────────────────
        configs.put(
                NotificationEventType.COUNSELLOR_SLOT_ASSIGNED,
                new EventTemplateConfig(
                        "Bạn được phân công lịch tư vấn",
                        "Cơ sở {actorName} vừa phân công lịch tư vấn cho bạn trong chiến dịch {packageName}.",
                        "/counsellor/calendar",
                        List.of()   // specificRecipients: counsellor accounts
                )
        );

        configs.put(
                NotificationEventType.COUNSELLOR_SLOT_UNASSIGNED,
                new EventTemplateConfig(
                        "Lịch tư vấn của bạn bị huỷ phân công",
                        "Cơ sở {actorName} vừa huỷ phân công một số lịch tư vấn của bạn.",
                        "/counsellor/calendar",
                        List.of()   // specificRecipients: counsellor accounts
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
