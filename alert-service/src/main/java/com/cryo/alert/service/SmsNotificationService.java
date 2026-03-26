package com.cryo.alert.service;
import com.cryo.alert.dto.AlertMessage;
public interface SmsNotificationService {
    void sendAlert(String mobileNumber, AlertMessage message);
}