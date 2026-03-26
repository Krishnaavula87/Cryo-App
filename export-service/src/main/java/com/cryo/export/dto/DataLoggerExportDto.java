package com.cryo.export.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class DataLoggerExportDto {

    public static class Common {
        public String topic;
        public String po;
        public String power;
        public String powerAlarm;
        public BigDecimal batteryPercentage;
        public String batteryAlarm;
        public BigDecimal ambientTemperature;
        public BigDecimal ambientHumidity;
       // public BigDecimal setTemp;
    }

    public static class ChannelReading {
        public String channelNumber;
        public LocalDateTime timestamp;
        public BigDecimal temperature;
        public String status;
        public BigDecimal setTemp;           // ✅ NEW
        public BigDecimal highTemp;
        public String highTempAlarm;
        public BigDecimal lowTemp;
        public String lowTempAlarm;
    }

    private Common common;
    private List<ChannelReading> readings;

    public Common getCommon() { return common; }
    public void setCommon(Common common) { this.common = common; }

    public List<ChannelReading> getReadings() { return readings; }
    public void setReadings(List<ChannelReading> readings) { this.readings = readings; }
}