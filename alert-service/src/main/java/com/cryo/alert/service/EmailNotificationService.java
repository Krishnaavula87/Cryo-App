package com.cryo.alert.service;
import com.cryo.alert.dto.AlertMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationService.class);

    private final JavaMailSender mailSender;

    public EmailNotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendAlert(String email, AlertMessage message) {

        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(email);
            mail.setSubject(message.getAlertTitle());
            mail.setText(message.getAlertBody());

            mailSender.send(mail);

            logger.info("Email alert sent successfully to: {}", email);

        } catch (Exception e) {
            logger.error("Failed to send email alert to: {}", email, e);
        }
    }
}