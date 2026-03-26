
package com.cryo.alert.service;

import com.cryo.alert.dto.AlertMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class WhatsappSmsService implements SmsNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(WhatsappSmsService.class);

    @Value("${whatsapp.phone.number.id}")
    private String phoneNumberId;

    @Value("${whatsapp.access.token}")
    private String accessToken;

    private final RestTemplate restTemplate;

    public WhatsappSmsService(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    @Override
    public void sendAlert(String mobileNumber, AlertMessage message) {

        try {

            if (mobileNumber == null || mobileNumber.trim().isEmpty()) {
                logger.warn("Mobile number is empty. Skipping WhatsApp alert.");
                return;
            }

            String url = "https://graph.facebook.com/v17.0/" + phoneNumberId + "/messages";

            String bodyText =
                    message.getAlertTitle() +
                            "\n\n" +
                            message.getAlertBody();

            logger.info("📲 Sending WhatsApp alert to: {}", mobileNumber);

            // -------------------------
            // Build Reply Button
            // -------------------------

            Map<String, Object> reply = new HashMap<>();
            reply.put("id", "ACK_" + message.getDeviceId());
            reply.put("title", "✅ Acknowledge");

            Map<String, Object> button = new HashMap<>();
            button.put("type", "reply");
            button.put("reply", reply);

            List<Map<String, Object>> buttons = new ArrayList<>();
            buttons.add(button);

            Map<String, Object> action = new HashMap<>();
            action.put("buttons", buttons);

            Map<String, Object> body = new HashMap<>();
            body.put("text", bodyText);

            Map<String, Object> interactive = new HashMap<>();
            interactive.put("type", "button");
            interactive.put("body", body);
            interactive.put("action", action);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("messaging_product", "whatsapp");
            requestBody.put("to", mobileNumber);
            requestBody.put("type", "interactive");
            requestBody.put("interactive", interactive);

            // Call helper method
            sendRequest(url, requestBody);

            logger.info("✅ WhatsApp alert sent successfully to: {}", mobileNumber);

        } catch (Exception e) {
            logger.error("❌ Error sending WhatsApp alert to {}", mobileNumber, e);
        }
    }

    public void sendMainMenu(String mobileNumber) {

        try {

            String url = "https://graph.facebook.com/v17.0/" + phoneNumberId + "/messages";

            List<Map<String, Object>> rows = new ArrayList<>();

            rows.add(Map.of("id", "MENU_DASHBOARD", "title", "📊 Dashboard"));
            rows.add(Map.of("id", "MENU_ACTIVE", "title", "⚡ Active Devices"));
            rows.add(Map.of("id", "MENU_ALERTS", "title", "⚠️ Alert Devices"));
            rows.add(Map.of("id", "MENU_CHANNELS", "title", "📡 Channel Details"));
            rows.add(Map.of("id", "MENU_SEARCH", "title", "🔍 Search Device"));

            Map<String, Object> section = Map.of(
                    "title", "Cryo Monitoring",
                    "rows", rows
            );

            Map<String, Object> interactive = new HashMap<>();
            interactive.put("type", "list");
            interactive.put("body", Map.of("text",
                    "👋 Welcome to Cryo Monitoring System\n\nSupports:\n🧊 Freezers\n📊 Data Loggers"));
            interactive.put("action", Map.of(
                    "button", "Open Menu",
                    "sections", List.of(section)
            ));

            Map<String, Object> request = new HashMap<>();
            request.put("messaging_product", "whatsapp");
            request.put("to", mobileNumber);
            request.put("type", "interactive");
            request.put("interactive", interactive);

            sendRequest(url, request);

        } catch (Exception e) {
            logger.error("Menu error", e);
        }
    }

    // ✅ SEND REQUEST METHOD (YOU ASKED FOR THIS)

    private void sendRequest(String url, Map<String, Object> body) {

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);

            HttpEntity<Map<String, Object>> entity =
                    new HttpEntity<>(body, headers);

            restTemplate.postForEntity(url, entity, String.class);

        } catch (Exception e) {
            logger.error("❌ Error while calling WhatsApp API", e);
        }
    }

    public void sendSimpleText(String mobileNumber, String body) {

        try {

            String url = "https://graph.facebook.com/v17.0/" + phoneNumberId + "/messages";

            Map<String, Object> text = new HashMap<>();
            text.put("body", body);

            Map<String, Object> request = new HashMap<>();
            request.put("messaging_product", "whatsapp");
            request.put("to", mobileNumber);
            request.put("type", "text");
            request.put("text", text);

            sendRequest(url, request);

        } catch (Exception e) {
            logger.error("Error sending text", e);
        }
    }

    public void sendText(String mobile, String message) {

        String url = "https://graph.facebook.com/v17.0/" + phoneNumberId + "/messages";

        Map<String, Object> text = Map.of(
                "body", message
        );

        Map<String, Object> request = new HashMap<>();
        request.put("messaging_product", "whatsapp");
        request.put("to", mobile);
        request.put("type", "text");
        request.put("text", text);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        restTemplate.postForEntity(
                url,
                new HttpEntity<>(request, headers),
                String.class
        );
    }
}