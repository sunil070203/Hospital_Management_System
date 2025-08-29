package com.wipro.hms.notification.repository;


import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.wipro.hms.notification.entity.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByRecipient(String recipient, Pageable pageable);
    Page<Notification> findByType(Notification.NotificationType type, Pageable pageable);
    Page<Notification> findByStatus(Notification.NotificationStatus status, Pageable pageable);
    Page<Notification> findBySentAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    List<Notification> findByStatus(Notification.NotificationStatus status);
    long countByStatus(Notification.NotificationStatus status);
}
