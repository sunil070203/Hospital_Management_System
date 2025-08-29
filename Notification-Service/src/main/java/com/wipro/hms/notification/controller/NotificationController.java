package com.wipro.hms.notification.controller;

package com.hms.notification.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hms.notification.model.SmsRequest;
import com.hms.notification.service.NotificationLogService;
import com.hms.notification.service.SmsService;
import com.wipro.hms.notification.entity.EmailRequest;
import com.wipro.hms.notification.entity.Notification;
import com.wipro.hms.notification.service.EmailService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notification Management", description = "APIs for sending notifications and viewing notification history")
public class NotificationController {
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private SmsService smsService;
    
    @Autowired
    private NotificationLogService notificationLogService;
    
    @PostMapping("/email")
    @Operation(summary = "Send an email")
    public ResponseEntity<Notification> sendEmail(@Valid @RequestBody EmailRequest emailRequest) {
        Notification notification;
        
        if (emailRequest.isHtml()) {
            notification = emailService.sendHtmlEmail(
                emailRequest.getTo(), 
                emailRequest.getSubject(), 
                emailRequest.getMessage()
            );
        } else {
            notification = emailService.sendSimpleEmail(
                emailRequest.getTo(), 
                emailRequest.getSubject(), 
                emailRequest.getMessage()
            );
        }
        
        return ResponseEntity.ok(notification);
    }
    
    @PostMapping("/sms")
    @Operation(summary = "Send an SMS")
    public ResponseEntity<Notification> sendSms(@Valid @RequestBody SmsRequest smsRequest) {
        Notification notification = smsService.sendSms(smsRequest.getTo(), smsRequest.getMessage());
        return ResponseEntity.ok(notification);
    }
    
    @GetMapping
    @Operation(summary = "Get notification history")
    public ResponseEntity<Page<Notification>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String recipient,
            @RequestParam(required = false) Notification.NotificationType type,
            @RequestParam(required = false) Notification.NotificationStatus status) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Notification> notifications;
        
        if (recipient != null) {
            notifications = notificationLogService.getNotificationsByRecipient(recipient, pageable);
        } else if (type != null) {
            notifications = notificationLogService.getNotificationsByType(type, pageable);
        } else if (status != null) {
            notifications = notificationLogService.getNotificationsByStatus(status, pageable);
        } else {
            notifications = notificationLogService.getAllNotifications(pageable);
        }
        
        return ResponseEntity.ok(notifications);
    }
    
    @GetMapping("/stats")
    @Operation(summary = "Get notification statistics")
    public ResponseEntity<NotificationStats> getNotificationStats() {
        long totalSent = notificationLogService.getNotificationCountByStatus(Notification.NotificationStatus.SENT);
        long totalFailed = notificationLogService.getNotificationCountByStatus(Notification.NotificationStatus.FAILED);
        long totalPending = notificationLogService.getNotificationCountByStatus(Notification.NotificationStatus.PENDING);
        
        NotificationStats stats = new NotificationStats(totalSent, totalFailed, totalPending);
        return ResponseEntity.ok(stats);
    }
    
    // Add this method to NotificationLogService
    public Page<Notification> getAllNotifications(Pageable pageable) {
        return notificationRepository.findAll(pageable);
    }
    
    // Inner class for statistics
    public static class NotificationStats {
        private long sent;
        private long failed;
        private long pending;
        
        public NotificationStats(long sent, long failed, long pending) {
            this.sent = sent;
            this.failed = failed;
            this.pending = pending;
        }
        
        // Getters
        public long getSent() { return sent; }
        public long getFailed() { return failed; }
        public long getPending() { return pending; }
    }
}