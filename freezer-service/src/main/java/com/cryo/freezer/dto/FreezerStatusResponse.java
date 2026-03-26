package com.cryo.freezer.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FreezerStatusResponse {

    // ================= COMMON =================
    private String freezerId;
    private String name;
    private String deviceType; // NORMAL_FREEZER / DATA_LOGGER
    private LocalDateTime timestamp;

    // ================= NORMAL FREEZER =================
    private BigDecimal currentTemp;
    private Boolean isFreezerOn;
    private Boolean isDoorOpen;
    private Boolean isRedAlert;

    // ================= DATA LOGGER SUMMARY =================
    private Integer totalChannels;
    private Integer activeChannels;
    private Integer alertChannels;

    // ================= OPTIONAL CHANNEL DETAILS =================
    private List<DataLoggerChannelStatusDto> channels;

    public String getFreezerId() {
        return freezerId;
    }

    public void setFreezerId(String freezerId) {
        this.freezerId = freezerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public BigDecimal getCurrentTemp() {
        return currentTemp;
    }

    public void setCurrentTemp(BigDecimal currentTemp) {
        this.currentTemp = currentTemp;
    }

    public Boolean getIsFreezerOn() {
        return isFreezerOn;
    }

    public void setIsFreezerOn(Boolean isFreezerOn) {
        this.isFreezerOn = isFreezerOn;
    }

    public Boolean getIsDoorOpen() {
        return isDoorOpen;
    }

    public void setIsDoorOpen(Boolean isDoorOpen) {
        this.isDoorOpen = isDoorOpen;
    }

    public Boolean getIsRedAlert() {
        return isRedAlert;
    }

    public void setIsRedAlert(Boolean isRedAlert) {
        this.isRedAlert = isRedAlert;
    }

    public Integer getTotalChannels() {
        return totalChannels;
    }

    public void setTotalChannels(Integer totalChannels) {
        this.totalChannels = totalChannels;
    }

    public Integer getActiveChannels() {
        return activeChannels;
    }

    public void setActiveChannels(Integer activeChannels) {
        this.activeChannels = activeChannels;
    }

    public Integer getAlertChannels() {
        return alertChannels;
    }

    public void setAlertChannels(Integer alertChannels) {
        this.alertChannels = alertChannels;
    }

    public List<DataLoggerChannelStatusDto> getChannels() {
        return channels;
    }

    public void setChannels(List<DataLoggerChannelStatusDto> channels) {
        this.channels = channels;
    }
}