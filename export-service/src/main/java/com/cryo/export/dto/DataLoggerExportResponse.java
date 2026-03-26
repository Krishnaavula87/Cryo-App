//package com.cryo.export.dto;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//public class DataLoggerExportResponse {
//
//    private Common common;
//    private List<ChannelReading> readings;
//
//    public static class Common {
//        public String topic;
//        public String po;
//        public String power;
//        public String powerAlarm;
//        public String batteryPercentage;
//        public String batteryAlarm;
//        public String ambientTemperature;
//        public String ambientHumidity;
//        public String setTemp;
//    }
//
//    public static class ChannelReading {
//        public String channelNumber;
//        public LocalDateTime timestamp;
//        public Double temperature;
//        public String status;
//        public String highTempAlarm;
//        public String lowTempAlarm;
//    }
//
//    public Common getCommon() { return common; }
//    public void setCommon(Common common) { this.common = common; }
//
//    public List<ChannelReading> getReadings() { return readings; }
//    public void setReadings(List<ChannelReading> readings) { this.readings = readings; }
//}