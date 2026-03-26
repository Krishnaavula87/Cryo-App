package com.cryo.alert.consumer;

import com.cryo.alert.service.DataLoggerChannelAlertService;
//import com.cryo.freezer.event.DataLoggerChannelAlertEvent;
import com.cryo.common.event.*;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class DataLoggerChannelAlertConsumer {

private final DataLoggerChannelAlertService service;

    public DataLoggerChannelAlertConsumer(DataLoggerChannelAlertService service) {
        this.service = service;
    }

    @KafkaListener(
            topics = "datalogger-channel-alert-topic",
            groupId = "alert-service-group"
    )
    public void consume(DataLoggerChannelAlertEvent event) {
        System.out.println("event consumed:"+event.getChannelNumber());
        service.process(event);
    }
}
