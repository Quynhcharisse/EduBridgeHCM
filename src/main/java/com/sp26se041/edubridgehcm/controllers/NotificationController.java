package com.sp26se041.edubridgehcm.controllers;

import com.sp26se041.edubridgehcm.requests.RegisterDeviceTokenRequest;
import com.sp26se041.edubridgehcm.requests.RemoveDeviceTokenRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.NotificationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/device-tokens")
    @PreAuthorize("hasAnyRole('ADMIN','SCHOOL','PARENT','COUNSELLOR')")
    public ResponseEntity<ResponseObject> registerDeviceToken(@RequestBody RegisterDeviceTokenRequest request) {
        return notificationService.registerDeviceToken(request);
    }

    @PostMapping("/device-tokens/remove")
    @PreAuthorize("hasAnyRole('ADMIN','SCHOOL','PARENT','COUNSELLOR')")
    public ResponseEntity<ResponseObject> removeDeviceToken(@RequestBody RemoveDeviceTokenRequest request) {
        return notificationService.removeDeviceToken(request);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SCHOOL','PARENT','COUNSELLOR')")
    public ResponseEntity<ResponseObject> getMyNotifications(@RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "20") int pageSize) {
        return notificationService.getMyNotifications(page, pageSize);
    }

    @GetMapping("/unread-count")
    @PreAuthorize("hasAnyRole('ADMIN','SCHOOL','PARENT','COUNSELLOR')")
    public ResponseEntity<ResponseObject> getMyUnreadCount() {
        return notificationService.getMyUnreadCount();
    }

    @PutMapping("/{recipientId}/read")
    @PreAuthorize("hasAnyRole('ADMIN','SCHOOL','PARENT','COUNSELLOR')")
    public ResponseEntity<ResponseObject> markAsRead(@PathVariable Integer recipientId) {
        return notificationService.markAsRead(recipientId);
    }
}
