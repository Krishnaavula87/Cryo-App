package com.cryo.alert.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AlertMessage {

    private String deviceId;
    private String deviceType;
    private String ownerUserId;

    private String alertTitle;
    private String alertBody;

    private BigDecimal temperature;
    private BigDecimal batteryPercentage;
    private String channelNumber;

    private Boolean doorOpen;
    private Boolean powerFailure;

    private LocalDateTime timestamp;

    public AlertMessage() {}

    public AlertMessage(String deviceId,
                        String deviceType,
                        String ownerUserId,
                        String alertTitle,
                        String alertBody,
                        BigDecimal temperature,
                        BigDecimal batteryPercentage,
                        String channelNumber,
                        Boolean doorOpen,
                        Boolean powerFailure,
                        LocalDateTime timestamp) {

        this.deviceId = deviceId;
        this.deviceType = deviceType;
        this.ownerUserId = ownerUserId;
        this.alertTitle = alertTitle;
        this.alertBody = alertBody;
        this.temperature = temperature;
        this.batteryPercentage = batteryPercentage;
        this.channelNumber = channelNumber;
        this.doorOpen = doorOpen;
        this.powerFailure = powerFailure;
        this.timestamp = timestamp;
    }

    // ✅ ALL GETTERS

    public String getDeviceId() { return deviceId; }
    public String getDeviceType() { return deviceType; }
    public String getOwnerUserId() { return ownerUserId; }
    public String getAlertTitle() { return alertTitle; }
    public String getAlertBody() { return alertBody; }
    public BigDecimal getTemperature() { return temperature; }
    public BigDecimal getBatteryPercentage() { return batteryPercentage; }
    public String getChannelNumber() { return channelNumber; }
    public Boolean getDoorOpen() { return doorOpen; }
    public Boolean getPowerFailure() { return powerFailure; }
    public LocalDateTime getTimestamp() { return timestamp; }

    // ✅ ALL SETTERS

    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }
    public void setOwnerUserId(String ownerUserId) { this.ownerUserId = ownerUserId; }
    public void setAlertTitle(String alertTitle) { this.alertTitle = alertTitle; }
    public void setAlertBody(String alertBody) { this.alertBody = alertBody; }
    public void setTemp(BigDecimal temperature) { this.temperature = temperature; }
    public void setBatteryPercentage(BigDecimal batteryPercentage) { this.batteryPercentage = batteryPercentage; }
    public void setChannelNumber(String channelNumber) { this.channelNumber = channelNumber; }
    public void setDoorOpen(Boolean doorOpen) { this.doorOpen = doorOpen; }
    public void setPowerFailure(Boolean powerFailure) { this.powerFailure = powerFailure; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}