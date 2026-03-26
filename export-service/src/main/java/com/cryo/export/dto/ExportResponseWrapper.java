package com.cryo.export.dto;

public class ExportResponseWrapper {

    private String deviceType;
    private Object common;
    private Object readings;

    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }

    public Object getCommon() { return common; }
    public void setCommon(Object common) { this.common = common; }

    public Object getReadings() { return readings; }
    public void setReadings(Object readings) { this.readings = readings; }
}