package com.cryo.alert.service;

//import com.cryo.freezer.event.DataLoggerChannelAlertEvent;
import com.cryo.common.event.*;
import org.springframework.stereotype.Service;

@Service
public class DataLoggerChannelAlertService {

    private final BaseAlertService baseService;

    public DataLoggerChannelAlertService(BaseAlertService baseService) {
        this.baseService = baseService;
    }

    public void process(DataLoggerChannelAlertEvent event) {

        StringBuilder msg = new StringBuilder();
        boolean hasAlert = false;

        msg.append("🚨 CHANNEL ALERT\n\n")
                .append("Device ID: ").append(event.getDeviceId()).append("\n")
                .append("Channel No: ").append(event.getChannelNumber()).append("\n")
                .append("Time: ").append(event.getTimestamp()).append("\n\n");

        // 🔥 HIGH TEMP
        if (Boolean.TRUE.equals(event.gethighTempAlarm())) {
            hasAlert = true;
            msg.append("🔥 HIGH TEMPERATURE ALERT\n")
                    .append("Current: ").append(event.getTemperature()).append("°C\n")
                    .append("Max Allowed: ").append(event.gethighTemp()).append("°C\n\n");
        }

        // ❄ LOW TEMP
        if (Boolean.TRUE.equals(event.getlowTempAlarm())) {
            hasAlert = true;
            msg.append("❄ LOW TEMPERATURE ALERT\n")
                    .append("Current: ").append(event.getTemperature()).append("°C\n")
                    .append("Min Allowed: ").append(event.getlowTemp()).append("°C\n\n");
        }

        // 🔌 STATUS OFF (since status is String)
        if ("OFF".equalsIgnoreCase(event.getStatus())) {
            hasAlert = true;
            msg.append("🔌 CHANNEL STATUS: OFF\n\n");
        }

        baseService.handleAlert(
                event.getDeviceId() + "-CH-" + event.getChannelNumber(),
                event.getOwnerUserId(),   // ADD THIS
                event.getTemperature(),
                event.getTimestamp(),
                hasAlert,
                msg.toString()

        );
    }
}