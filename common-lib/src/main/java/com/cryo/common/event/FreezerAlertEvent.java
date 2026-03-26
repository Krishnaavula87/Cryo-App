package com.cryo.common.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class FreezerAlertEvent {

    private String freezerId;
    private String ownerUserId;


    private BigDecimal temperature;
    private BigDecimal highTemp;
    private BigDecimal lowTemp;
    private BigDecimal batteryPercentage;

    private Boolean doorAlarm;
    private Boolean powerAlarm;
    private Boolean freezerCompressor;
    private Boolean highTempAlarm;
    private Boolean lowTempAlarm;
    private Boolean batteryAlarm;
    private LocalDateTime timestamp;

    public FreezerAlertEvent() {}

    public FreezerAlertEvent(
            String freezerId,
            String ownerUserId,
            BigDecimal temperature,
            BigDecimal highTemp,
            BigDecimal lowTemp,
            BigDecimal batteryPercentage,
            Boolean doorAlarm,
            Boolean powerAlarm,
            Boolean freezerCompressor,
            Boolean highTempAlarm,
            Boolean lowTempAlarm,
            Boolean batteryAlarm,
            LocalDateTime timestamp) {

        this.freezerId = freezerId;
        this.ownerUserId=ownerUserId;
        this.temperature = temperature;
        this.highTemp = highTemp;
        this.lowTemp = lowTemp;
        this.batteryPercentage = batteryPercentage;
        this.doorAlarm = doorAlarm;
        this.powerAlarm = powerAlarm;
        this.freezerCompressor = freezerCompressor;
        this.highTempAlarm = highTempAlarm;
        this.lowTempAlarm = lowTempAlarm;
        this.batteryAlarm = batteryAlarm;
        this.timestamp = timestamp;
    }

    public String getFreezerId() {
        return freezerId;
    }

    public void setFreezerId(String freezerId) {
        this.freezerId = freezerId;
    }

    public String getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(String ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    public BigDecimal getTemperature() {
        return temperature;
    }

    public void setTemp(BigDecimal temperature) {
        this.temperature = temperature;
    }

    public BigDecimal getHighTemp() {
        return highTemp;
    }

    public void setHighTemp(BigDecimal highTemp) {
        this.highTemp = highTemp;
    }

    public BigDecimal getLowTemp() {
        return lowTemp;
    }

    public void setLowTemp(BigDecimal lowTemp) {
        this.lowTemp = lowTemp;
    }

    public BigDecimal getBatteryPercentage() {
        return batteryPercentage;
    }

    public void setBatteryPercentage(BigDecimal batteryPercentage) {
        this.batteryPercentage = batteryPercentage;
    }

    public Boolean getDoorAlarm() {
        return doorAlarm;
    }

    public void setDoorAlarm(Boolean doorAlarm) {
        this.doorAlarm = doorAlarm;
    }

    public Boolean getPowerAlarm() {
        return powerAlarm;
    }

    public void setPowerAlarm(Boolean powerAlarm) {
        this.powerAlarm = powerAlarm;
    }

    public Boolean getFreezerCompressor() {
        return freezerCompressor;
    }

    public void setFreezerCompressor(Boolean freezerCompressor) {
        this.freezerCompressor = freezerCompressor;
    }

    public Boolean getHighTempAlarm() {
        return highTempAlarm;
    }

    public void setHighTempAlarm(Boolean highTempAlarm) {
        this.highTempAlarm = highTempAlarm;
    }

    public Boolean getLowTempAlarm() {
        return lowTempAlarm;
    }

    public void setLowTempAlarm(Boolean lowTempAlarm) {
        this.lowTempAlarm = lowTempAlarm;
    }

    public Boolean getBatteryAlarm() {
        return batteryAlarm;
    }

    public void setBatteryAlarm(Boolean batteryAlarm) {
        this.batteryAlarm = batteryAlarm;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

}