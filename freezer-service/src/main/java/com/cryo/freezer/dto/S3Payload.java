package com.cryo.freezer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class S3Payload {

    private String topic;
    private String po;
    private String timestamp;

    private Double temperature;
    private Double ambientTemperature;
    private Double ambientHumidity;


    private String freezerDoor;
    private String doorAlarm;

    private String freezerPower;
    private String powerAlarm;

    private Double compressorTemp;
    private String freezerCompressor;
    private Double condenserTemp;

    private Double setTemp;
    private Double highTemp;
    private String highTempAlarm;

    private Double lowTemp;
    private String lowTempAlarm;

    private Double batteryPercentage;
    private String batteryAlarm;



    @JsonProperty("acVolatage") // NOTE: spelling matches payload
    private Double acVolatage;

    private Double acCurrent;


    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getPo() {
        return po;
    }

    public void setPo(String po) {
        this.po = po;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemp(Double temperature) {
        this.temperature = temperature;
    }

    public Double getAmbientTemperature() {
        return ambientTemperature;
    }

    public void setAmbientTemperature(Double ambientTemperature) {
        this.ambientTemperature = ambientTemperature;
    }

    public Double getambientHumidity() {
        return ambientHumidity;
    }

    public void setambientHumidity(Double ambientHumidity) {
        this.ambientHumidity = ambientHumidity;
    }


    public String getFreezerDoor() {
        return freezerDoor;
    }

    public void setFreezerDoor(String freezerDoor) {
        this.freezerDoor = freezerDoor;
    }

    public String getDoorAlarm() {
        return doorAlarm;
    }

    public void setDoorAlarm(String doorAlarm) {
        this.doorAlarm = doorAlarm;
    }

    public String getFreezerPower() {
        return freezerPower;
    }

    public void setFreezerPower(String freezerPower) {
        this.freezerPower = freezerPower;
    }

    public String getPowerAlarm() {
        return powerAlarm;
    }

    public void setPowerAlarm(String powerAlarm) {
        this.powerAlarm = powerAlarm;
    }

    public Double getCompressorTemp() {
        return compressorTemp;
    }

    public void setCompressorTemp(Double compressorTemp) {
        this.compressorTemp = compressorTemp;
    }

    public String getFreezerCompressor() {
        return freezerCompressor;
    }

    public void setFreezerCompressor(String freezerCompressor) {
        this.freezerCompressor = freezerCompressor;
    }

    public Double getCondenserTemp() {
        return condenserTemp;
    }

    public void setCondenserTemp(Double condenserTemp) {
        this.condenserTemp = condenserTemp;
    }

    public Double getSetTemp() {
        return setTemp;
    }

    public void setSetTemp(Double setTemp) {
        this.setTemp = setTemp;
    }

    public Double getHighTemp() {
        return highTemp;
    }

    public void setHighTemp(Double highTemp) {
        this.highTemp = highTemp;
    }

    public String getHighTempAlarm() {
        return highTempAlarm;
    }

    public void setHighTempAlarm(String highTempAlarm) {
        this.highTempAlarm = highTempAlarm;
    }

    public Double getLowTemp() {
        return lowTemp;
    }

    public void setLowTemp(Double lowTemp) {
        this.lowTemp = lowTemp;
    }

    public String getLowTempAlarm() {
        return lowTempAlarm;
    }

    public void setLowTempAlarm(String lowTempAlarm) {
        this.lowTempAlarm = lowTempAlarm;
    }

    public Double getBatteryPercentage() {
        return batteryPercentage;
    }

    public void setBatteryPercentage(Double batteryPercentage) {
        this.batteryPercentage = batteryPercentage;
    }

    public String getbatteryAlarm() {
        return batteryAlarm;
    }

    public void setbatteryAlarm(String batteryAlarm) {
        this.batteryAlarm = batteryAlarm;
    }



    public Double getAcVolatage() {
        return acVolatage;
    }

    public void setAcVolatage(Double acVolatage) {
        this.acVolatage = acVolatage;
    }

    public Double getAcCurrent() {
        return acCurrent;
    }

    public void setAcCurrent(Double acCurrent) {
        this.acCurrent = acCurrent;
    }
}
