package com.cryo.alert.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DataLoggerDeviceAlertEvent {

    private String deviceId;
    private String ownerUserId;
    private Boolean powerAlarm;
    private Boolean batteryAlarm;
    private BigDecimal batteryPercentage;
    private LocalDateTime timestamp;

    public DataLoggerDeviceAlertEvent() {
    }

    public DataLoggerDeviceAlertEvent(String deviceId, String ownerUserId,   // ✅ ADD
                                      Boolean powerAlarm,
                                      Boolean batteryAlarm,
                                      BigDecimal batteryPercentage,
                                      LocalDateTime timestamp) {
        this.deviceId = deviceId;
        this.ownerUserId = ownerUserId;  // ✅ SET
        this.powerAlarm = powerAlarm;
        this.batteryAlarm = batteryAlarm;
        this.batteryPercentage = batteryPercentage;
        this.timestamp = timestamp;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(String ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    public Boolean getPowerAlarm() {
        return powerAlarm;
    }

    public void setPowerAlarm(Boolean powerAlarm) {
        this.powerAlarm = powerAlarm;
    }

    public Boolean getBatteryAlarm() {
        return batteryAlarm;
    }

    public void setBatteryAlarm(Boolean batteryAlarm) {
        this.batteryAlarm = batteryAlarm;
    }

    public BigDecimal getBatteryPercentage() {
        return batteryPercentage;
    }

    public void setBatteryPercentage(BigDecimal batteryPercentage) {
        this.batteryPercentage = batteryPercentage;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

}