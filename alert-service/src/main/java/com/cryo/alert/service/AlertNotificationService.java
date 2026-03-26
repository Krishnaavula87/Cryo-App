package com.cryo.alert.service;
import com.cryo.alert.dto.AlertMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AlertNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(AlertNotificationService.class);

    private final SmsNotificationService smsNotificationService;
    private final EmailNotificationService emailNotificationService;
    private final Fast2SmsService fast2SmsService;

    public AlertNotificationService(SmsNotificationService smsNotificationService,
                                    EmailNotificationService emailNotificationService,
                                    Fast2SmsService fast2SmsService) {
        this.smsNotificationService = smsNotificationService;
        this.emailNotificationService = emailNotificationService;
        this.fast2SmsService = fast2SmsService;
    }

    @Async
    public void sendAlertNotifications(String mobileNumber,
                                       String email,
                                       AlertMessage message,
                                       Boolean notifyWhatsapp,
                                       Boolean notifySms,
                                       Boolean notifyEmail) {

        logger.info("📢 Triggering alert notifications for device: {}", message.getDeviceId());

        if (Boolean.TRUE.equals(notifyWhatsapp)) {
            try {
                smsNotificationService.sendAlert(mobileNumber, message);
                logger.info("✅ WhatsApp notification sent to: {}", mobileNumber);
            } catch (Exception e) {
                logger.error("❌ Failed to send WhatsApp notification", e);
            }
        }

        if (Boolean.TRUE.equals(notifyEmail)) {
            try {
                emailNotificationService.sendAlert(email, message);
                logger.info("✅ Email notification sent to: {}", email);
            } catch (Exception e) {
                logger.error("❌ Failed to send email notification", e);
            }
        }

        if (Boolean.TRUE.equals(notifySms)) {
            try {
                fast2SmsService.sendAlert(mobileNumber, message);
                logger.info("✅ SMS sent to: {}", mobileNumber);
            } catch (Exception e) {
                logger.error("❌ Failed to send SMS", e);
            }
        }
    }
}