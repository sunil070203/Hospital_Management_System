package com.wipro.hms.notification.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private SmsService smsService;
    
    @Autowired
    private NotificationLogService notificationLogService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @KafkaListener(topics = "appointment-events")
    public void handleAppointmentEvents(String event) {
        logger.info("Received appointment event: {}", event);
        
        try {
            JsonNode jsonNode = objectMapper.readTree(event);
            String type = jsonNode.get("type").asText();
            JsonNode data = jsonNode.get("data");
            
            switch (type) {
                case "APPOINTMENT_CREATED":
                    sendAppointmentConfirmation(data);
                    break;
                case "APPOINTMENT_UPDATED":
                    sendAppointmentUpdate(data);
                    break;
                case "APPOINTMENT_CANCELLED":
                    sendAppointmentCancellation(data);
                    break;
                default:
                    logger.warn("Unknown appointment event type: {}", type);
            }
        } catch (Exception e) {
            logger.error("Error processing appointment event: {}", e.getMessage(), e);
        }
    }
    
    @KafkaListener(topics = "billing-events")
    public void handleBillingEvents(String event) {
        logger.info("Received billing event: {}", event);
        
        try {
            JsonNode jsonNode = objectMapper.readTree(event);
            String type = jsonNode.get("type").asText();
            JsonNode data = jsonNode.get("data");
            
            if ("BILL_CREATED".equals(type)) {
                sendBillNotification(data);
            } else if ("BILL_PAID".equals(type)) {
                sendPaymentConfirmation(data);
            }
        } catch (Exception e) {
            logger.error("Error processing billing event: {}", e.getMessage(), e);
        }
    }
    
    @KafkaListener(topics = "patient-events")
    public void handlePatientEvents(String event) {
        logger.info("Received patient event: {}", event);
        
        try {
            JsonNode jsonNode = objectMapper.readTree(event);
            String type = jsonNode.get("type").asText();
            JsonNode data = jsonNode.get("data");
            
            if ("PATIENT_REGISTERED".equals(type)) {
                sendWelcomeEmail(data);
            }
        } catch (Exception e) {
            logger.error("Error processing patient event: {}", e.getMessage(), e);
        }
    }
    
    private void sendAppointmentConfirmation(JsonNode appointmentData) {
        try {
            Long patientId = appointmentData.get("patientId").asLong();
            Long doctorId = appointmentData.get("doctorId").asLong();
            String appointmentDate = appointmentData.get("appointmentDate").asText();
            
            // In a real application, you would fetch these details from other services
            String patientEmail = "patient@example.com"; // Fetch from patient service
            String patientPhone = "+1234567890"; // Fetch from patient service
            String doctorName = "Dr. Smith"; // Fetch from doctor service
            
            LocalDateTime dateTime = LocalDateTime.parse(appointmentDate);
            String formattedDate = dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm"));
            
            // Email content
            String emailSubject = "Appointment Confirmation";
            String emailMessage = String.format(
                "Dear Patient,\n\nYour appointment with %s has been confirmed for %s.\n\n" +
                "Please arrive 15 minutes before your scheduled time.\n\n" +
                "Thank you,\nHospital Management Team",
                doctorName, formattedDate
            );
            
            // SMS content
            String smsMessage = String.format(
                "Appointment confirmed with %s on %s. Please arrive 15 mins early.",
                doctorName, formattedDate
            );
            
            // Send notifications
            emailService.sendSimpleEmail(patientEmail, emailSubject, emailMessage);
            smsService.sendSms(patientPhone, smsMessage);
            
        } catch (Exception e) {
            logger.error("Error sending appointment confirmation: {}", e.getMessage(), e);
        }
    }
    
    private void sendBillNotification(JsonNode billData) {
        try {
            Long patientId = billData.get("patientId").asLong();
            BigDecimal totalAmount = new BigDecimal(billData.get("totalAmount").asText());
            
            // In a real application, you would fetch these details from other services
            String patientEmail = "patient@example.com";
            String patientPhone = "+1234567890";
            
            // Email content
            String emailSubject = "New Bill Generated";
            String emailMessage = String.format(
                "Dear Patient,\n\nA new bill of $%.2f has been generated for your recent visit.\n\n" +
                "You can view and pay your bill through your patient portal.\n\n" +
                "Thank you,\nHospital Management Team",
                totalAmount
            );
            
            // SMS content
            String smsMessage = String.format(
                "New bill of $%.2f generated. Please check your email or patient portal for details.",
                totalAmount
            );
            
            // Send notifications
            emailService.sendSimpleEmail(patientEmail, emailSubject, emailMessage);
            smsService.sendSms(patientPhone, smsMessage);
            
        } catch (Exception e) {
            logger.error("Error sending bill notification: {}", e.getMessage(), e);
        }
    }
    
    private void sendWelcomeEmail(JsonNode patientData) {
        try {
            String patientEmail = patientData.get("email").asText();
            String patientName = patientData.get("firstName").asText() + " " + patientData.get("lastName").asText();
            
            String emailSubject = "Welcome to Our Hospital Management System";
            String emailMessage = String.format(
                "Dear %s,\n\nWelcome to our Hospital Management System!\n\n" +
                "Your account has been successfully created. You can now:\n" +
                "- Book appointments with doctors\n" +
                "- View your medical records\n" +
                "- Pay bills online\n" +
                "- Receive notifications about your healthcare\n\n" +
                "If you have any questions, please contact our support team.\n\n" +
                "Thank you for choosing us for your healthcare needs!\n\n" +
                "Best regards,\nHospital Management Team",
                patientName
            );
            
            // Send welcome email
            emailService.sendSimpleEmail(patientEmail, emailSubject, emailMessage);
            
        } catch (Exception e) {
            logger.error("Error sending welcome email: {}", e.getMessage(), e);
        }
    }
    
    // Other notification methods would be implemented here
    private void sendAppointmentUpdate(JsonNode appointmentData) {
        // Implementation for appointment updates
    }
    
    private void sendAppointmentCancellation(JsonNode appointmentData) {
        // Implementation for appointment cancellations
    }
    
    private void sendPaymentConfirmation(JsonNode paymentData) {
        // Implementation for payment confirmations
    }
}