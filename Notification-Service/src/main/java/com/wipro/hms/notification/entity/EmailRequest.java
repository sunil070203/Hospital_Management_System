package com.wipro.hms.notification.entity;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmailRequest {
    @NotBlank(message = "Recipient email is required")
    @Email(message = "Email should be valid")
    private String to;
    
    @NotBlank(message = "Subject is required")
    private String subject;
    
    @NotBlank(message = "Message content is required")
    private String message;
    
    private boolean isHtml = false;
}
