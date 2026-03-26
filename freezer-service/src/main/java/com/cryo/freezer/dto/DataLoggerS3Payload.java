package com.cryo.freezer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DataLoggerS3Payload {

    private Common common;
    private List<Channel> channels;


    public Common getCommon() {
        return common;
    }

    public void setCommon(Common common) {
        this.common = common;
    }

    public List<Channel> getChannels() {
        return channels;
    }

    public void setChannels(List<Channel> channels) {
        this.channels = channels;
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Common {

        public String topic;
        public String po;
        public String timestamp;

        public String power;
        public String powerAlarm;

        public String batteryPercentage;
        public String batteryAlarm;

        public String ambientTemperature;
        public String ambientHumidity;

        //public String setTemp;
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Channel {

        public String channelNumber;
        public String temperature;
        public String status;

        public String setTemp;

        public String highTemp;
        public String highTempAlarm;

        public String lowTemp;
        public String lowTempAlarm;
    }
}
