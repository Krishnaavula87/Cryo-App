package com.cryo.alert.consumer;

import com.cryo.alert.service.FreezerAlertService;
//import com.cryo.freezer.event.FreezerAlertEvent;
import com.cryo.common.event.*;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class FreezerAlertConsumer {
private final FreezerAlertService freezerAlertService;

    public FreezerAlertConsumer(FreezerAlertService freezerAlertService) {
        this.freezerAlertService = freezerAlertService;
    }

    @KafkaListener(
            topics = "freezer-alert-topic",
            groupId = "alert-service-group"
    )
    public void consume(FreezerAlertEvent event) {
        System.out.println("event consumed:"+event.getFreezerId());
        freezerAlertService.process(event);
    }
}