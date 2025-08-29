package com.wipro.hms.notification.entity;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    private NotificationType type;
    
    private String recipient;
    private String subject;
    private String message;
    
    @Enumerated(EnumType.STRING)
    private NotificationStatus status;
    
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;
    
    private String errorMessage;
    
    public enum NotificationType {
        EMAIL, SMS, PUSH
    }
    
    public enum NotificationStatus {
        PENDING, SENT, FAILED
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = NotificationStatus.PENDING;
        }
    }
}
