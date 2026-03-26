package com.cryo.alert.service;

//import com.cryo.freezer.event.FreezerAlertEvent;
import com.cryo.common.event.*;
import org.springframework.stereotype.Service;

@Service
public class FreezerAlertService {
    private final BaseAlertService baseService;

    public FreezerAlertService(BaseAlertService baseService) {
        this.baseService = baseService;
    }

    public void process(FreezerAlertEvent event) {

        StringBuilder msg = new StringBuilder();
        boolean hasAlert = false;

        msg.append("🚨 CRITICAL ALERT\n\n")
                .append("Freezer ID: ").append(event.getFreezerId()).append("\n")
                .append("Time: ").append(event.getTimestamp()).append("\n\n");

        if (Boolean.TRUE.equals(event.getHighTempAlarm())) {
            hasAlert = true;
            msg.append("🔥 HIGH TEMPERATURE ALERT\n")
                    .append("Current: ").append(event.getTemperature()).append("°C\n")
                    .append("Max Allowed: ").append(event.getHighTemp()).append("°C\n\n");
        }

        if (Boolean.TRUE.equals(event.getLowTempAlarm())) {
            hasAlert = true;
            msg.append("❄ LOW TEMPERATURE ALERT\n")
                    .append("Current: ").append(event.getTemperature()).append("°C\n")
                    .append("Min Allowed: ").append(event.getLowTemp()).append("°C\n\n");
        }

        if (Boolean.TRUE.equals(event.getBatteryAlarm())) {
            hasAlert = true;
            msg.append("🔋 BATTERY LOW\n")
                    .append("Battery: ").append(event.getBatteryPercentage()).append("%\n\n");
        }

        if (Boolean.TRUE.equals(event.getDoorAlarm())) {
            hasAlert = true;
            msg.append("🚪 DOOR OPEN\n\n");
        }

        if (Boolean.TRUE.equals(event.getPowerAlarm())) {
            hasAlert = true;
            msg.append("⚡ POWER FAILURE\n\n");
        }

        baseService.handleAlert(
                event.getFreezerId(),
                event.getOwnerUserId(),
                event.getTemperature(),
                event.getTimestamp(),
                hasAlert,
                msg.toString()
        );
    }

}
