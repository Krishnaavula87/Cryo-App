package com.cryo.alert.service;

//import com.cryo.freezer.event.DataLoggerDeviceAlertEvent;
import com.cryo.common.event.*;
import org.springframework.stereotype.Service;

@Service
public class DataLoggerDeviceAlertService {
    private final BaseAlertService baseService;

    public DataLoggerDeviceAlertService(BaseAlertService baseService) {
        this.baseService = baseService;
    }

    public void process(DataLoggerDeviceAlertEvent event) {

        StringBuilder msg = new StringBuilder();
        boolean hasAlert = false;

        msg.append("🚨 DEVICE ALERT\n\n")
                .append("Device ID: ").append(event.getDeviceId()).append("\n")
                .append("Time: ").append(event.getTimestamp()).append("\n\n");

        if (Boolean.TRUE.equals(event.getPowerAlarm())) {
            hasAlert = true;
            msg.append("⚡ POWER FAILURE\n\n");
        }

        if (Boolean.TRUE.equals(event.getBatteryAlarm())) {
            hasAlert = true;
            msg.append("🔋 BATTERY LOW\n")
                    .append("Battery: ").append(event.getBatteryPercentage()).append("%\n\n");
        }

        baseService.handleAlert(
                event.getDeviceId(),
                event.getOwnerUserId(),
                null,                       // temperature (not applicable)
                event.getTimestamp(),
                hasAlert,
                msg.toString()

        );
    }

}
