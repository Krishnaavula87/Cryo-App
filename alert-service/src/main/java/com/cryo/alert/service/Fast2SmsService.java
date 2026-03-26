package com.cryo.alert.service;

import com.cryo.alert.dto.AlertMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class Fast2SmsService {

    private static final Logger logger = LoggerFactory.getLogger(Fast2SmsService.class);

    @Value("${sms.fast2sms.api-key}")
    private String apiKey;

    @Value("${sms.fast2sms.route}")
    private String route;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendAlert(String mobileNumber, AlertMessage message) {

        try {
            if (mobileNumber == null || mobileNumber.isBlank()) return;

            String smsText = message.getAlertTitle() + " - " +
                    message.getDeviceId() + " | " +
                    message.getAlertBody();

            logger.info("📲 Sending SMS to: {}", mobileNumber);

            String url = "https://www.fast2sms.com/dev/bulkV2?authorization=" + apiKey +
                    "&route=" + route +
                    "&message=" + smsText +
                    "&language=english&flash=0&numbers=" + mobileNumber;

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0");
            headers.set("cache-control", "no-cache");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        } catch (Exception e) {
            logger.error("❌ Failed to send SMS to {}: {}", mobileNumber, e.getMessage());
        }
    }
}