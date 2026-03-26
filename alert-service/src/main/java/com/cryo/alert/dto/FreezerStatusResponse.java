package com.cryo.alert.dto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FreezerStatusResponse {
    private String freezerId;
    private String name;
    private String poNumber;
    private String deviceType;
    private String timestamp;
    // NORMAL FREEZER
    private BigDecimal currentTemp;
    private Boolean isFreezerOn;
    private Boolean isRedAlert;
    // DATA LOGGER
    private Integer totalChannels;
    private Integer activeChannels;
    private Integer alertChannels;
    private List<ChannelStatus> channels;

    // =========================
    // GETTERS & SETTERS
    // =========================

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

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

    public String getPoNumber() {
        return poNumber;
    }

    public void setPoNumber(String poNumber) {
        this.poNumber = poNumber;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
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

    public void setIsFreezerOn(Boolean freezerOn) {
        isFreezerOn = freezerOn;
    }

    public Boolean getIsRedAlert() {
        return isRedAlert;
    }

    public void setIsRedAlert(Boolean redAlert) {
        isRedAlert = redAlert;
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

    public List<ChannelStatus> getChannels() {
        return channels;
    }

    public void setChannels(List<ChannelStatus> channels) {
        this.channels = channels;
    }

    // =====================================================
    // 🔥 FIXED INNER CLASS
    // =====================================================

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ChannelStatus {

        private String channelNumber;
        private String status;
        private Double temperature;
        private Boolean highTempAlarm;
        private Boolean lowTempAlarm;

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

        public Double getTemperature() {
            return temperature;
        }

        public void setTemp(Double temperature) {
            this.temperature = temperature;
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
    }
}