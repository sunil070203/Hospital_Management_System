package com.wipro.hms.notification.service;

package com.hms.notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;

import com.wipro.hms.notification.entity.Notification;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired
    private NotificationLogService notificationLogService;
    
    public Notification sendSimpleEmail(String to, String subject, String text) {
        Notification notification = new Notification();
        notification.setType(Notification.NotificationType.EMAIL);
        notification.setRecipient(to);
        notification.setSubject(subject);
        notification.setMessage(text);
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            
            mailSender.send(message);
            
            notification.setStatus(Notification.NotificationStatus.SENT);
            notification.setSentAt(java.time.LocalDateTime.now());
            logger.info("Email sent successfully to: {}", to);
            
        } catch (Exception e) {
            notification.setStatus(Notification.NotificationStatus.FAILED);
            notification.setErrorMessage(e.getMessage());
            logger.error("Failed to send email to {}: {}", to, e.getMessage());
        }
        
        return notificationLogService.saveNotification(notification);
    }
    
    public Notification sendHtmlEmail(String to, String subject, String htmlContent) {
        Notification notification = new Notification();
        notification.setType(Notification.NotificationType.EMAIL);
        notification.setRecipient(to);
        notification.setSubject(subject);
        notification.setMessage(htmlContent);
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            
            notification.setStatus(Notification.NotificationStatus.SENT);
            notification.setSentAt(java.time.LocalDateTime.now());
            logger.info("HTML email sent successfully to: {}", to);
            
        } catch (MessagingException e) {
            notification.setStatus(Notification.NotificationStatus.FAILED);
            notification.setErrorMessage(e.getMessage());
            logger.error("Failed to send HTML email to {}: {}", to, e.getMessage());
        }
        
        return notificationLogService.saveNotification(notification);
    }
}
