package com.cryo.freezer.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "data_logger_channel_readings")
public class DataLoggerChannelReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "topic")
    private String topic; // FK reference to DataLoggerDevice.topic

    @Column(name = "channel_number")
    private String channelNumber;

    @Column(precision = 5, scale = 2)
    private BigDecimal temperature;

    @Column(name = "status")
    private String status;

    @Column(name = "set_temperature", precision = 5, scale = 2)
    private BigDecimal setTemp;

    @Column(name = "high_temperature", precision = 5, scale = 2)
    private BigDecimal highTemp;

    @Column(name = "high_alarm")
    private String highTempAlarm;

    @Column(name = "low_temperature", precision = 5, scale = 2)
    private BigDecimal lowTemp;

    @Column(name = "low_alarm")
    private String lowTempAlarm;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getChannelNumber() {
        return channelNumber;
    }

    public void setChannelNumber(String channelNumber) {
        this.channelNumber = channelNumber;
    }

    public BigDecimal getTemperature() {
        return temperature;
    }

    public void setTemp(BigDecimal temperature) {
        this.temperature = temperature;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getsetTemp() {
        return setTemp;
    }

    public void setsetTemp(BigDecimal setTemp) {
        this.setTemp = setTemp;
    }

    public BigDecimal gethighTemp() {
        return highTemp;
    }

    public void sethighTemp(BigDecimal highTemp) {
        this.highTemp = highTemp;
    }

    public String gethighTempAlarm() {
        return highTempAlarm;
    }

    public void sethighTempAlarm(String highTempAlarm) {
        this.highTempAlarm = highTempAlarm;
    }

    public BigDecimal getlowTemp() {
        return lowTemp;
    }

    public void setlowTemp(BigDecimal lowTemp) {
        this.lowTemp = lowTemp;
    }

    public String getlowTempAlarm() {
        return lowTempAlarm;
    }

    public void setlowTempAlarm(String lowTempAlarm) {
        this.lowTempAlarm = lowTempAlarm;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

}
