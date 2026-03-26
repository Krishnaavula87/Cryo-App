package com.cryo.freezer.dto;

public class DeviceInfoDto {

    private String freezerId;
    private String ownerUserId;
    private String deviceType;

    public DeviceInfoDto() {}

    public DeviceInfoDto(String freezerId, String ownerUserId, String deviceType) {
        this.freezerId = freezerId;
        this.ownerUserId = ownerUserId;
        this.deviceType = deviceType;
    }

    public String getFreezerId() {
        return freezerId;
    }

    public void setFreezerId(String freezerId) {
        this.freezerId = freezerId;
    }

    public String getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(String ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }
}