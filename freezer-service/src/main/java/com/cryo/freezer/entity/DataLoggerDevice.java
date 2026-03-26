package com.cryo.freezer.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "data_logger_devices")
public class DataLoggerDevice {

    @Id
    @Column(name = "topic")
    private String topic; // DL202512001

    @Column(name = "po_number")
    private String poNumber;

    @Column(name = "owner_user_id", nullable = false)
    private String ownerUserId;

    @Column(name = "power")
    private String power;

    @Column(name = "power_alarm")
    private String powerAlarm;

    @Column(name = "battery_percentage", precision = 5, scale = 2)
    private BigDecimal batteryPercentage;

    @Column(name = "battery_alarm")
    private String batteryAlarm;

    @Column(name = "ambient_temperature", precision = 5, scale = 2)
    private BigDecimal ambientTemperature;

    @Column(name = "ambient_ambientHumidity", precision = 5, scale = 2)
    private BigDecimal ambientHumidity;

//    @Column(name = "set_temperature", precision = 5, scale = 2)
//    private BigDecimal setTemp;

    @Column(name = "last_timestamp")
    private LocalDateTime lastTimestamp;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getPoNumber() {
        return poNumber;
    }

    public void setPoNumber(String poNumber) {
        this.poNumber = poNumber;
    }

    public String getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(String ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    public String getPower() {
        return power;
    }

    public void setPower(String power) {
        this.power = power;
    }

    public String getPowerAlarm() {
        return powerAlarm;
    }

    public void setPowerAlarm(String powerAlarm) {
        this.powerAlarm = powerAlarm;
    }

    public BigDecimal getBatteryPercentage() {
        return batteryPercentage;
    }

    public void setBatteryPercentage(BigDecimal batteryPercentage) {
        this.batteryPercentage = batteryPercentage;
    }

    public String getBatteryAlarm() {
        return batteryAlarm;
    }

    public void setBatteryAlarm(String batteryAlarm) {
        this.batteryAlarm = batteryAlarm;
    }

    public BigDecimal getAmbientTemperature() {
        return ambientTemperature;
    }

    public void setAmbientTemperature(BigDecimal ambientTemperature) {
        this.ambientTemperature = ambientTemperature;
    }

    public BigDecimal getambientHumidity() {
        return ambientHumidity;
    }

    public void setambientHumidity(BigDecimal ambientHumidity) {
        this.ambientHumidity = ambientHumidity;
    }

    /*public BigDecimal getsetTemp() {
        return setTemp;
    }

    public void setsetTemp(BigDecimal setTemp) {
        this.setTemp = setTemp;
    }*/

    public LocalDateTime getLastTimestamp() {
        return lastTimestamp;
    }

    public void setLastTimestamp(LocalDateTime lastTimestamp) {
        this.lastTimestamp = lastTimestamp;
    }

}
