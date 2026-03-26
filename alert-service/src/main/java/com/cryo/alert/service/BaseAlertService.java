/*package com.cryo.alert.service;
import com.cryo.alert.dto.AlertMessage;
import com.cryo.alert.dto.UserProfileDto;
import com.cryo.alert.entity.Alert;
import com.cryo.alert.entity.FreezerAlertState;
import com.cryo.alert.repository.AlertRepository;
import com.cryo.alert.repository.FreezerAlertStateRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Transactional
public class BaseAlertService {

    private static final Logger logger = LoggerFactory.getLogger(BaseAlertService.class);

    private final AlertRepository alertRepository;
    private final FreezerAlertStateRepository stateRepository;
    private final AlertNotificationService notificationService;
    private final RestTemplate restTemplate;

    @Value("${auth.service.url:http://localhost:8081}")
    private String authServiceUrl;

    public BaseAlertService(AlertRepository alertRepository,
                            FreezerAlertStateRepository stateRepository,
                            AlertNotificationService notificationService,
                            RestTemplateBuilder builder) {

        this.alertRepository = alertRepository;
        this.stateRepository = stateRepository;
        this.notificationService = notificationService;
        this.restTemplate = builder.build();
    }

    public void handleAlert(String deviceId,
                            String ownerUserId,
                            BigDecimal temperature,
                            LocalDateTime timestamp,
                            boolean hasAlert,
                            String fullMessage) {

        logger.info("🔔 handleAlert called for {} | hasAlert={}", deviceId, hasAlert);

        if (ownerUserId == null || ownerUserId.isBlank()) {
            logger.error("❌ ownerUserId is empty. Cannot send alert.");
            return;
        }

        FreezerAlertState state = stateRepository
                .findById(deviceId)
                .orElse(new FreezerAlertState(deviceId));

        if (!hasAlert) {
            if (Boolean.TRUE.equals(state.getActive())) {
                state.setActive(false);
                state.setAcknowledged(false);
                stateRepository.save(state);
            }
            return;
        }

        if (Boolean.TRUE.equals(state.getActive())
                && !Boolean.TRUE.equals(state.getAcknowledged())) {
            logger.info("⚠ Duplicate alert prevented for {}", deviceId);
            return;
        }

        state.setActive(true);
        state.setAcknowledged(false);
        state.setLastAlertTime(LocalDateTime.now());
        stateRepository.save(state);

        Alert alert = new Alert();
        alert.setFreezerId(deviceId);
        alert.setOwnerUserId(ownerUserId);
        alert.setTemp(temperature);
        alert.setTimestamp(timestamp);
        alert.setAlertType(Alert.AlertType.RED_ALERT);
        alertRepository.save(alert);

        // 🔥 FETCH USER FROM AUTH SERVICE

        String url = authServiceUrl + "/auth/users/" + ownerUserId;

        UserProfileDto user;
        try {
            user = restTemplate.getForObject(url, UserProfileDto.class);
        } catch (Exception e) {
            logger.error("❌ Failed to fetch user from auth service", e);
            return;
        }

        if (user == null) {
            logger.error("❌ User not found in auth service");
            return;
        }

        String mobile = user.getMobileNumber();
        String email = user.getEmail();

        logger.info("📞 Fetched user mobile={} email={}", mobile, email);

        AlertMessage alertMessage = new AlertMessage(
                deviceId,
                "DEVICE",
                ownerUserId,
                "CRITICAL ALERT",
                fullMessage,
                temperature,
                null,
                null,
                null,
                null,
                timestamp
        );

        notificationService.sendAlertNotifications(
                mobile,
                email,
                alertMessage,
                user.getNotifyWhatsapp(),
                user.getNotifySms(),
                user.getNotifyEmail()
        );

        logger.info("✅ Alert notification triggered for {}", deviceId);
    }
}*/
package com.cryo.alert.service;
import com.cryo.alert.dto.AlertMessage;
import com.cryo.alert.dto.UserProfileDto;
import com.cryo.alert.entity.Alert;
import com.cryo.alert.entity.FreezerAlertState;
import com.cryo.alert.repository.AlertRepository;
import com.cryo.alert.repository.FreezerAlertStateRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Service
@Transactional
public class BaseAlertService {

    private static final Logger logger =
            LoggerFactory.getLogger(BaseAlertService.class);

    private final AlertRepository alertRepository;
    private final FreezerAlertStateRepository stateRepository;
    private final AlertNotificationService notificationService;
    private final RestTemplate restTemplate;

    @Value("${auth.service.url:http://localhost:8081}")
    private String authServiceUrl;

    public BaseAlertService(AlertRepository alertRepository,
                            FreezerAlertStateRepository stateRepository,
                            AlertNotificationService notificationService,
                            RestTemplateBuilder builder) {

        this.alertRepository = alertRepository;
        this.stateRepository = stateRepository;
        this.notificationService = notificationService;
        this.restTemplate = builder.build();
    }
    public void handleAlert(String deviceId,
                            String ownerUserId,
                            BigDecimal temperature,
                            LocalDateTime timestamp,
                            boolean hasAlert,
                            String fullMessage) {

        logger.info("handleAlert called for {} | hasAlert={}", deviceId, hasAlert);

        if (ownerUserId == null || ownerUserId.isBlank()) {
            logger.error("ownerUserId missing for {}", deviceId);
            return;
        }

        FreezerAlertState state = stateRepository
                .findById(deviceId)
                .orElse(new FreezerAlertState(deviceId));

        // -----------------------------
        // ALERT CLEARED
        // -----------------------------
        if (!hasAlert) {

            if (Boolean.TRUE.equals(state.getActive())) {

                logger.info("Alert cleared for {}", deviceId);

                state.setActive(false);
                state.setAcknowledged(false);
                state.setLastAlertTime(null);
                state.setLastNotificationTime(null);

                stateRepository.save(state);
            }

            return;
        }

        LocalDateTime now = LocalDateTime.now();

        boolean sendNotification = false;

        // -----------------------------
        // NEW ALERT
        // -----------------------------
        if (!Boolean.TRUE.equals(state.getActive())) {

            logger.info("New alert detected for {}", deviceId);

            state.setActive(true);
            state.setAcknowledged(false);
            state.setLastAlertTime(now);

            sendNotification = true;
        }

        // -----------------------------
        // EXISTING ALERT
        // -----------------------------
        else {

            if (Boolean.TRUE.equals(state.getAcknowledged())) {

                logger.info("Alert acknowledged already for {}", deviceId);
                return;
            }

            if (state.getLastNotificationTime() == null ||
                    state.getLastNotificationTime()
                            .plusMinutes(360)
                            .isBefore(now)) {

                logger.info("Reminder alert for {}", deviceId);

                sendNotification = true;
            }
        }

        if (!sendNotification) {
            logger.info("Notification not required yet for {}", deviceId);
            return;
        }

        state.setLastNotificationTime(now);
        stateRepository.save(state);

        sendNotification(deviceId, ownerUserId, temperature, timestamp, fullMessage);
    }
    private void sendNotification(String deviceId,
                                  String ownerUserId,
                                  BigDecimal temperature,
                                  LocalDateTime timestamp,
                                  String fullMessage) {

        Alert alert = new Alert();

        alert.setFreezerId(deviceId);
        alert.setOwnerUserId(ownerUserId);
        alert.setTemp(temperature);
        alert.setTimestamp(timestamp);
        alert.setAlertType(Alert.AlertType.RED_ALERT);


        alertRepository.save(alert);

        String url = authServiceUrl + "/auth/users/" + ownerUserId;

        UserProfileDto user;

        try {
            user = restTemplate.getForObject(url, UserProfileDto.class);
        } catch (Exception e) {
            logger.error("Failed to fetch user profile", e);
            return;
        }

        if (user == null) {
            logger.error("User not found for {}", ownerUserId);
            return;
        }

        AlertMessage message = new AlertMessage(
                deviceId,
                "DEVICE",
                ownerUserId,
                "CRITICAL ALERT",
                fullMessage,
                temperature,
                null,
                null,
                null,
                null,
                timestamp
        );

        notificationService.sendAlertNotifications(
                user.getMobileNumber(),
                user.getEmail(),
                message,
                user.getNotifyWhatsapp(),
                user.getNotifySms(),
                user.getNotifyEmail()
        );

        logger.info("Alert notification sent for {}", deviceId);
    }
}