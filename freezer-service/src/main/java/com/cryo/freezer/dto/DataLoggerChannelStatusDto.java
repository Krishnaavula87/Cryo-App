package com.cryo.freezer.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DataLoggerChannelStatusDto {

    private String channelNumber;
    private BigDecimal temperature;
    private String status;

    private BigDecimal setTemp;
    private BigDecimal highTemp;
    private Boolean highTempAlarm;

    private BigDecimal lowTemp;
    private Boolean lowTempAlarm;

    private LocalDateTime timestamp;

    public DataLoggerChannelStatusDto() {}

    // ✅ FULL CONSTRUCTOR
    public DataLoggerChannelStatusDto(
            String channelNumber,
            BigDecimal temperature,
            String status,
            BigDecimal setTemp,
            BigDecimal highTemp,
            Boolean highTempAlarm,
            BigDecimal lowTemp,
            Boolean lowTempAlarm,
            LocalDateTime timestamp) {

        this.channelNumber = channelNumber;
        this.temperature = temperature;
        this.status = status;
        this.setTemp = setTemp;
        this.highTemp = highTemp;
        this.highTempAlarm = highTempAlarm;
        this.lowTemp = lowTemp;
        this.lowTempAlarm = lowTempAlarm;
        this.timestamp = timestamp;
    }


    public DataLoggerChannelStatusDto(
            String channelNumber,
            BigDecimal temperature,
            String status,
            Boolean highTempAlarm,
            Boolean lowTempAlarm,
            LocalDateTime timestamp) {

        this.channelNumber = channelNumber;
        this.temperature = temperature;
        this.status = status;
        this.highTempAlarm = highTempAlarm;
        this.lowTempAlarm = lowTempAlarm;
        this.timestamp = timestamp;
    }

    public String getChannelNumber() { return channelNumber; }
    public BigDecimal getTemperature() { return temperature; }
    public String getStatus() { return status; }

    public BigDecimal getsetTemp() { return setTemp; }
    public BigDecimal gethighTemp() { return highTemp; }
    public Boolean gethighTempAlarm() { return highTempAlarm; }

    public BigDecimal getlowTemp() { return lowTemp; }
    public Boolean getlowTempAlarm() { return lowTempAlarm; }

    public LocalDateTime getTimestamp() { return timestamp; }
}