package com.cryo.freezer.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class NormalFreezerExportDto {

    public static class Common {

        public String topic;
        public String po;

        public BigDecimal ambientTemperature;
        public BigDecimal ambientHumidity;

        public Boolean freezerOn;

        public BigDecimal compressorTemp;
        public BigDecimal condenserTemp;

        public BigDecimal setTemp;
        public BigDecimal highTemp;
        public BigDecimal lowTemp;

        public BigDecimal batteryPercentage;
    }

    public static class Reading {

        public LocalDateTime timestamp;
        public BigDecimal temperature;

        public Boolean doorAlarm;
        public Boolean highTempAlarm;
        public Boolean lowTempAlarm;
        public Boolean batteryAlarm;
    }

    private Common common;
    private List<Reading> readings;

    public Common getCommon() { return common; }
    public void setCommon(Common common) { this.common = common; }

    public List<Reading> getReadings() { return readings; }
    public void setReadings(List<Reading> readings) { this.readings = readings; }
}