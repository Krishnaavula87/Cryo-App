package com.cryo.freezer.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "freezer_readings")
public class FreezerReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "freezer_id", nullable = false)
    private String freezerId;
    @Column(name = "po_number")
    private String poNumber;


    private BigDecimal temperature;  //temp
    private BigDecimal ambientTemperature;
    private BigDecimal ambientHumidity;

    private Boolean freezerOn;
    private Boolean doorOpen;
    private Boolean doorAlarm; // on
    private Boolean powerAlarm; //on

    private BigDecimal compressorTemp;
    private Boolean freezerCompressor; //off
    private BigDecimal condenserTemp;

    private BigDecimal setTemp;
    private BigDecimal highTemp;
    private Boolean highTempAlarm;   //on
    private BigDecimal lowTemp;
    private Boolean lowTempAlarm; //on

    private BigDecimal batteryPercentage;
    private Boolean batteryAlarm;  //on

    private BigDecimal acVoltage;
    private BigDecimal acCurrent;

    private LocalDateTime timestamp;

    @Column(name = "red_alert")
    private Boolean redAlert = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFreezerId() {
        return freezerId;
    }

    public void setFreezerId(String freezerId) {
        this.freezerId = freezerId;
    }

    public BigDecimal getTemperature() {
        return temperature;
    }

    public void setTemp(BigDecimal temperature) {
        this.temperature = temperature;
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

    public Boolean getFreezerOn() {
        return freezerOn;
    }

    public void setFreezerOn(Boolean freezerOn) {
        this.freezerOn = freezerOn;
    }

    public Boolean getDoorOpen() {
        return doorOpen;
    }

    public void setDoorOpen(Boolean doorOpen) {
        this.doorOpen = doorOpen;
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

    public BigDecimal getCompressorTemp() {
        return compressorTemp;
    }

    public void setCompressorTemp(BigDecimal compressorTemp) {
        this.compressorTemp = compressorTemp;
    }

    public Boolean getFreezerCompressor() {
        return freezerCompressor;
    }

    public void setFreezerCompressor(Boolean freezerCompressor) {
        this.freezerCompressor = freezerCompressor;
    }

    public BigDecimal getCondenserTemp() {
        return condenserTemp;
    }

    public void setCondenserTemp(BigDecimal condenserTemp) {
        this.condenserTemp = condenserTemp;
    }

    public BigDecimal getSetTemp() {
        return setTemp;
    }

    public void setSetTemp(BigDecimal setTemp) {
        this.setTemp = setTemp;
    }

    public BigDecimal getHighTemp() {
        return highTemp;
    }

    public void setHighTemp(BigDecimal highTemp) {
        this.highTemp = highTemp;
    }

    public Boolean getHighTempAlarm() {
        return highTempAlarm;
    }

    public void setHighTempAlarm(Boolean highTempAlarm) {
        this.highTempAlarm = highTempAlarm;
    }

    public BigDecimal getLowTemp() {
        return lowTemp;
    }

    public void setLowTemp(BigDecimal lowTemp) {
        this.lowTemp = lowTemp;
    }

    public Boolean getLowTempAlarm() {
        return lowTempAlarm;
    }

    public void setLowTempAlarm(Boolean lowTempAlarm) {
        this.lowTempAlarm = lowTempAlarm;
    }

    public BigDecimal getBatteryPercentage() {
        return batteryPercentage;
    }

    public void setBatteryPercentage(BigDecimal batteryPercentage) {
        this.batteryPercentage = batteryPercentage;
    }

    public Boolean getBatteryAlarm() {
        return batteryAlarm;
    }

    public void setBatteryAlarm(Boolean batteryAlarm) {
        this.batteryAlarm = batteryAlarm;
    }

    public BigDecimal getAcVoltage() {
        return acVoltage;
    }

    public void setAcVoltage(BigDecimal acVoltage) {
        this.acVoltage = acVoltage;
    }

    public BigDecimal getAcCurrent() {
        return acCurrent;
    }

    public void setAcCurrent(BigDecimal acCurrent) {
        this.acCurrent = acCurrent;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Boolean getRedAlert() {
        return redAlert;
    }

    public void setRedAlert(Boolean redAlert) {
        this.redAlert = redAlert;
    }

    // ✅ IMPORTANT: Add this method (this fixes your error)
    public Boolean isRedAlert() {
        return redAlert != null && redAlert;
    }

    public String getPoNumber() {
        return poNumber;
    }

    public void setPoNumber(String poNumber) {
        this.poNumber = poNumber;
    }
}
