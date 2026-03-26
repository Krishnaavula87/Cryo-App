package com.cryo.alert.consumer;

import com.cryo.alert.service.DataLoggerDeviceAlertService;
//import com.cryo.freezer.event.DataLoggerDeviceAlertEvent;
import com.cryo.common.event.*;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class DataLoggerDeviceAlertConsumer {

    private final DataLoggerDeviceAlertService service;

    public DataLoggerDeviceAlertConsumer(DataLoggerDeviceAlertService service) {
        this.service = service;
    }

    @KafkaListener(
            topics = "datalogger-device-alert-topic", groupId = "alert-service-group")
    public void consume(DataLoggerDeviceAlertEvent event) {
        System.out.println("event consumed:" + event.getDeviceId());
        service.process(event);
    }

}
