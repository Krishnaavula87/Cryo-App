package com.cryo.common.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DataLoggerChannelAlertEvent {

    private String deviceId;
    private String ownerUserId;
    private String channelNumber;
    private String status;
    private BigDecimal temperature;
    private BigDecimal highTemp;
    private BigDecimal lowTemp;
    private Boolean highTempAlarm;
    private Boolean lowTempAlarm;
    private LocalDateTime timestamp;

    public DataLoggerChannelAlertEvent() {
    }

    public DataLoggerChannelAlertEvent(String deviceId, String ownerUserId,
                                       String channelNumber,
                                       String status,
                                       BigDecimal temperature,
                                       BigDecimal highTemp,
                                       BigDecimal lowTemp,
                                       Boolean highTempAlarm,
                                       Boolean lowTempAlarm,
                                       LocalDateTime timestamp) {
        this.deviceId = deviceId;
        this.ownerUserId = ownerUserId;
        this.channelNumber = channelNumber;
        this.status = status;
        this.temperature = temperature;
        this.highTemp = highTemp;
        this.lowTemp = lowTemp;
        this.highTempAlarm = highTempAlarm;
        this.lowTempAlarm = lowTempAlarm;
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

    public String getChannelNumber() {
        return channelNumber;
    }

    public void setChannelNumber(String channelNumber) {
        this.channelNumber = channelNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getTemperature() {
        return temperature;
    }

    public void setTemp(BigDecimal temperature) {
        this.temperature = temperature;
    }

    public BigDecimal gethighTemp() {
        return highTemp;
    }

    public void sethighTemp(BigDecimal highTemp) {
        this.highTemp = highTemp;
    }

    public BigDecimal getlowTemp() {
        return lowTemp;
    }

    public void setlowTemp(BigDecimal lowTemp) {
        this.lowTemp = lowTemp;
    }

    public Boolean gethighTempAlarm() {
        return highTempAlarm;
    }

    public void sethighTempAlarm(Boolean highTempAlarm) {
        this.highTempAlarm = highTempAlarm;
    }

    public Boolean getlowTempAlarm() {
        return lowTempAlarm;
    }

    public void setlowTempAlarm(Boolean lowTempAlarm) {
        this.lowTempAlarm = lowTempAlarm;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
