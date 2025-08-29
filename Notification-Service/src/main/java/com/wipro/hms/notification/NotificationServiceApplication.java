package com.wipro.hms.notification.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationConsumer {

    private final EmailService emailService;
    private final SmsService smsService;

    public NotificationConsumer(EmailService emailService, SmsService smsService) {
        this.emailService = emailService;
        this.smsService = smsService;
    }

    @KafkaListener(topics = "appointment-events", groupId = "notification-service-group")
    public void listenAppointmentEvents(String eventMessage) {
        // 1. Parse the message (JSON) to get patient/doctor details, message type, etc.
        // 2. Decide whether to send SMS or Email
        // Example:
        // emailService.sendSimpleEmail(toEmail, subject, body);
        // smsService.sendSms(toPhone, message);

        System.out.println("Received Appointment Event: " + eventMessage);
    }
}
