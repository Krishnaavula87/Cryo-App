package com.cryo.alert.controller;
import com.cryo.alert.dto.FreezerStatusResponse;
import com.cryo.alert.repository.FreezerAlertStateRepository;
import com.cryo.alert.service.WhatsappSmsService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/webhook")
public class WebhookController {

    @Value("${whatsapp.verify.token:cryo_secret_token}")
    private String verifyToken;

    @Value("${freezer.service.url:http://localhost:8082}")
    private String freezerServiceUrl;

    @Value("${auth.service.url:http://localhost:8081}")
    private String authServiceUrl;

    private final RestTemplate restTemplate;
    private final WhatsappSmsService whatsappService;
    private final FreezerAlertStateRepository alertStateRepository;

    public WebhookController(RestTemplateBuilder builder,
                             WhatsappSmsService whatsappService,
                             FreezerAlertStateRepository alertStateRepository) {

        this.restTemplate = builder.build();
        this.whatsappService = whatsappService;
        this.alertStateRepository = alertStateRepository;
    }

    // =====================================================
    // VERIFY
    // =====================================================

    @GetMapping
    public ResponseEntity<String> verify(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String token,
            @RequestParam("hub.challenge") String challenge) {

        if ("subscribe".equals(mode) && verifyToken.equals(token))
            return ResponseEntity.ok(challenge);

        return ResponseEntity.status(403).build();
    }

    // =====================================================
    // RECEIVE
    // =====================================================

    @PostMapping
    public ResponseEntity<String> receive(@RequestBody JsonNode body) {

        try {

            if (body == null || !body.has("entry"))
                return ResponseEntity.ok("EVENT_RECEIVED");

            JsonNode value = body.get("entry")
                    .get(0)
                    .get("changes")
                    .get(0)
                    .get("value");

            if (value == null || !value.has("messages"))
                return ResponseEntity.ok("EVENT_RECEIVED");

            JsonNode message = value.get("messages").get(0);
            String mobile = message.get("from").asText();

            // ================= TEXT =================
            if (message.has("text")) {

                String text = message.get("text")
                        .get("body")
                        .asText()
                        .trim();

                if (text.equalsIgnoreCase("hello")
                        || text.equalsIgnoreCase("hi")) {

                    whatsappService.sendMainMenu(mobile);
                    return ResponseEntity.ok("EVENT_RECEIVED");
                }

                sendDeviceDetails(mobile, text.toUpperCase());
            }

            // ================= INTERACTIVE =================
            if (message.has("interactive")) {

                JsonNode interactive = message.get("interactive");

                // MENU CLICK
                if (interactive.has("list_reply")) {

                    String menuId = interactive
                            .get("list_reply")
                            .get("id")
                            .asText();

                    handleMenu(menuId, mobile);
                }

                // ACK BUTTON
                else if (interactive.has("button_reply")) {

                    String buttonId = interactive
                            .get("button_reply")
                            .get("id")
                            .asText();

                    if (buttonId.startsWith("ACK_")) {

                        String alertId = buttonId.substring(4);

                        alertStateRepository.findById(alertId)
                                .ifPresent(state -> {
                                    state.setAcknowledged(true);
                                    state.setActive(false);
                                    alertStateRepository.save(state);
                                });

                        whatsappService.sendText(
                                mobile,
                                "✅ Alert acknowledged for: " + alertId
                        );
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok("EVENT_RECEIVED");
    }

    // =====================================================
    // MENU HANDLER
    // =====================================================

    private void handleMenu(String menuId, String mobile) {


        FreezerStatusResponse[] devices = getDevices(mobile);

        if (devices == null || devices.length == 0) {
            whatsappService.sendText(mobile, "No devices found.");
            return;
        }

        switch (menuId) {

            case "MENU_DASHBOARD":
                sendDashboard(mobile, devices);
                break;

            case "MENU_ACTIVE":
                sendActiveDevices(mobile, devices);
                break;

            case "MENU_ALERTS":
                sendAlertDevices(mobile, devices);
                break;

            case "MENU_CHANNELS":
                whatsappService.sendText(mobile,
                        "Enter Data Logger ID (Example: DL202512001)");
                break;

            case "MENU_SEARCH":
                whatsappService.sendText(mobile,
                        "Enter Device ID to search");
                break;
        }
    }

    // =====================================================
    // DASHBOARD
    // =====================================================

    private void sendDashboard(String mobile,
                               FreezerStatusResponse[] devices) {
        //String deviceTime = getLatestTimestamp(devices);



        int total = devices.length;
        int freezerCount = 0;
        int loggerCount = 0;
        int activeCount = 0;

        for (var d : devices) {

            if ("NORMAL_FREEZER".equalsIgnoreCase(d.getDeviceType())) {
                freezerCount++;
                if (Boolean.TRUE.equals(d.getIsFreezerOn()))
                    activeCount++;
            }

            if ("DATA_LOGGER".equalsIgnoreCase(d.getDeviceType())) {
                loggerCount++;
                if (d.getActiveChannels() != null &&
                        d.getActiveChannels() > 0)
                    activeCount++;
            }
        }

        StringBuilder msg = new StringBuilder();

        msg.append("📊 *DASHBOARD*\n\n");
        //msg.append("🕒 Data Time: ").append(deviceTime).append("\n\n");
        msg.append("```\n");
        msg.append("+----------------------+--------+\n");
        msg.append("| Metric               | Count  |\n");
        msg.append("+----------------------+--------+\n");
        msg.append(String.format("| %-20s | %-6d |\n", "Total Devices", total));
        msg.append(String.format("| %-20s | %-6d |\n", "Normal Freezers", freezerCount));
        msg.append(String.format("| %-20s | %-6d |\n", "Data Loggers", loggerCount));
        msg.append(String.format("| %-20s | %-6d |\n", "Active Devices", activeCount));
        msg.append("+----------------------+--------+\n");
        msg.append("```");

        whatsappService.sendText(mobile, msg.toString());
    }

    // =====================================================
    // ACTIVE DEVICES
    // =====================================================

    private void sendActiveDevices(String mobile,
                                   FreezerStatusResponse[] devices) {
        //String deviceTime = getLatestTimestamp(devices);



        StringBuilder msg = new StringBuilder();
        msg.append("⚡ *ACTIVE DEVICES*\n\n");
       // msg.append("🕒 Data Time: ").append(deviceTime).append("\n\n");

        for (var d : devices) {

            // ================= NORMAL FREEZER =================
            if ("NORMAL_FREEZER".equalsIgnoreCase(d.getDeviceType())
                    && Boolean.TRUE.equals(d.getIsFreezerOn())) {

                msg.append("🧊 Device: ").append(d.getFreezerId()).append("\n");
                msg.append("```\n");
                msg.append("+--------------+----------+\n");
                msg.append("| Temp (°C)    | Status   |\n");
                msg.append("+--------------+----------+\n");
                msg.append(String.format("| %-12s | %-8s |\n",
                        d.getCurrentTemp(),
                        "ON"));
                msg.append("+--------------+----------+\n");
                msg.append("```\n\n");
            }

            // ================= DATA LOGGER =================
            if ("DATA_LOGGER".equalsIgnoreCase(d.getDeviceType())
                    && d.getChannels() != null) {

                boolean hasActive = false;

                StringBuilder block = new StringBuilder();
                block.append("📡 Device: ").append(d.getFreezerId()).append("\n");
                block.append("```\n");
                block.append("+----------+------------+----------+\n");
                block.append("| Channel  | Temp (°C)  | Status   |\n");
                block.append("+----------+------------+----------+\n");

                for (var ch : d.getChannels()) {

                    if ("ON".equalsIgnoreCase(ch.getStatus())) {
                        hasActive = true;

                        block.append(String.format("| %-8s | %-10s | %-8s |\n",
                                ch.getChannelNumber(),
                                ch.getTemperature(),
                                ch.getStatus()));
                    }
                }

                block.append("+----------+------------+----------+\n");
                block.append("```\n\n");

                if (hasActive)
                    msg.append(block);
            }
        }

        if (msg.toString().trim().equals("⚡ *ACTIVE DEVICES*")) {
            msg.append("No active devices found.");
        }

        whatsappService.sendText(mobile, msg.toString());
    }

    // =====================================================
    // ALERT DEVICES
    // =====================================================

    private void sendAlertDevices(String mobile,
                                  FreezerStatusResponse[] devices) {
        //String deviceTime = getLatestTimestamp(devices);
        StringBuilder msg = new StringBuilder();
        msg.append("⚠️ *ALERT DEVICES*\n\n");
       // msg.append("🕒 Data Time: ").append(deviceTime).append("\n\n");

        for (var d : devices) {

            // ================= DATA LOGGER ALERTS =================
            if ("DATA_LOGGER".equalsIgnoreCase(d.getDeviceType())
                    && d.getChannels() != null) {

                boolean deviceHasAlert = false;
                StringBuilder deviceBlock = new StringBuilder();

                deviceBlock.append("📡 Device: ")
                        .append(d.getFreezerId())
                        .append("\n");

                deviceBlock.append("```\n");
                deviceBlock.append("+----------+------------+----------+--------+\n");
                deviceBlock.append("| Channel  | Temp (°C)  | Status   | Alert  |\n");
                deviceBlock.append("+----------+------------+----------+--------+\n");

                for (var ch : d.getChannels()) {

                    boolean isAlert =
                            Boolean.TRUE.equals(ch.gethighTempAlarm()) ||
                                    Boolean.TRUE.equals(ch.getlowTempAlarm()) ||
                                    "OFF".equalsIgnoreCase(ch.getStatus());

                    if (isAlert) {

                        deviceHasAlert = true;

                        deviceBlock.append(String.format(
                                "| %-8s | %-10s | %-8s | %-6s |\n",
                                ch.getChannelNumber(),
                                ch.getTemperature(),
                                ch.getStatus(),
                                "YES"));
                    }
                }

                deviceBlock.append("+----------+------------+----------+--------+\n");
                deviceBlock.append("```\n\n");

                if (deviceHasAlert) {
                    msg.append(deviceBlock);
                }
            }

            // ================= NORMAL FREEZER ALERT =================
            if ("NORMAL_FREEZER".equalsIgnoreCase(d.getDeviceType())
                    && Boolean.TRUE.equals(d.getIsRedAlert())) {

                msg.append("🧊 Device: ")
                        .append(d.getFreezerId())
                        .append("\n");

                msg.append("```\n");
                msg.append("+--------------+----------+--------+\n");
                msg.append("| Temp (°C)    | Status   | Alert  |\n");
                msg.append("+--------------+----------+--------+\n");
                msg.append(String.format("| %-12s | %-8s | %-6s |\n",
                        d.getCurrentTemp(),
                        Boolean.TRUE.equals(d.getIsFreezerOn()) ? "ON" : "OFF",
                        "YES"));
                msg.append("+--------------+----------+--------+\n");
                msg.append("```\n\n");
            }
        }

        if (msg.toString().trim().equals("⚠️ *ALERT DEVICES*")) {
            msg.append("No alert devices found.");
        }

        whatsappService.sendText(mobile, msg.toString());
    }

    // =====================================================
    // DEVICE DETAILS (SEARCH + CHANNEL DETAILS)
    // =====================================================

    private void sendDeviceDetails(String mobile, String deviceId) {


        FreezerStatusResponse[] devices = getDevices(mobile);
        if (devices == null) return;

        for (var d : devices) {

            if (d.getFreezerId().equalsIgnoreCase(deviceId)) {

                // ================= NORMAL FREEZER =================
                if ("NORMAL_FREEZER".equalsIgnoreCase(d.getDeviceType())) {

                    boolean isAlert =
                            Boolean.TRUE.equals(d.getIsRedAlert()) ||
                                    !Boolean.TRUE.equals(d.getIsFreezerOn());

                    StringBuilder msg = new StringBuilder();
                    msg.append("🧊 *FREEZER DETAILS*\n\n");
                    msg.append("Device: ").append(d.getFreezerId()).append("\n");
                    msg.append("```\n");
                    msg.append("+--------------+----------+--------+\n");
                    msg.append("| Temp (°C)    | Status   | Alert  |\n");
                    msg.append("+--------------+----------+--------+\n");
                    msg.append(String.format("| %-12s | %-8s | %-6s |\n",
                            d.getCurrentTemp(),
                            Boolean.TRUE.equals(d.getIsFreezerOn()) ? "ON" : "OFF",
                            isAlert ? "YES" : "NO"));
                    msg.append("+--------------+----------+--------+\n");
                    msg.append("```");

                    whatsappService.sendText(mobile, msg.toString());
                    return;
                }

                // ================= DATA LOGGER =================
                if ("DATA_LOGGER".equalsIgnoreCase(d.getDeviceType())
                        && d.getChannels() != null) {

                    StringBuilder msg = new StringBuilder();
                    msg.append("📡 *DATA LOGGER DETAILS*\n\n");
                    msg.append("Device: ").append(d.getFreezerId()).append("\n");
                    msg.append("```\n");
                    msg.append("+----------+------------+----------+--------+\n");
                    msg.append("| Channel  | Temp (°C)  | Status   | Alert  |\n");
                    msg.append("+----------+------------+----------+--------+\n");

                    for (var ch : d.getChannels()) {

                        boolean isAlert =
                                Boolean.TRUE.equals(ch.gethighTempAlarm()) ||
                                        Boolean.TRUE.equals(ch.getlowTempAlarm()) ||
                                        "OFF".equalsIgnoreCase(ch.getStatus());

                        msg.append(String.format("| %-8s | %-10s | %-8s | %-6s |\n",
                                ch.getChannelNumber(),
                                ch.getTemperature(),
                                ch.getStatus(),
                                isAlert ? "YES" : "NO"));
                    }

                    msg.append("+----------+------------+----------+--------+\n");
                    msg.append("```");

                    whatsappService.sendText(mobile, msg.toString());
                    return;
                }
            }
        }

        whatsappService.sendText(mobile, "❌ Device not found.");
    }

    // =====================================================
    // GET DEVICES FROM FREEZER SERVICE
    // =====================================================

    private FreezerStatusResponse[] getDevices(String mobile) {

        try {

            String ownerId = restTemplate.getForObject(
                    authServiceUrl + "/auth/internal/mobile/+" + mobile,
                    String.class);

            return restTemplate.getForObject(
                    freezerServiceUrl + "/freezers/api/internal/full/" + ownerId,
                    FreezerStatusResponse[].class);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    private String getLatestTimestamp(FreezerStatusResponse[] devices) {

        if (devices == null || devices.length == 0)
            return "N/A";

        String latest = null;

        for (FreezerStatusResponse d : devices) {
            if (d.getTimestamp() != null) {
                latest = d.getTimestamp();
                break; // assuming same batch timestamp
            }
        }

        return latest != null ? latest : "N/A";
    }
}

