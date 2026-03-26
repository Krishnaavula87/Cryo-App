package com.cryo.freezer.service;

import com.cryo.freezer.entity.DataLoggerChannelReading;
import com.cryo.freezer.entity.DataLoggerDevice;
//import com.cryo.freezer.event.DataLoggerChannelAlertEvent;
//import com.cryo.freezer.event.DataLoggerDeviceAlertEvent;
import com.cryo.common.event.DataLoggerChannelAlertEvent;
import com.cryo.common.event.DataLoggerDeviceAlertEvent;

import com.cryo.freezer.repository.DataLoggerChannelRepository;
import com.cryo.freezer.repository.DataLoggerDeviceRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class DataLoggerService {
    private static final Logger logger = LoggerFactory.getLogger(FreezerReadingService.class);

    private final DataLoggerChannelRepository channelRepository;
    private final DataLoggerDeviceRepository deviceRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public DataLoggerService(
            DataLoggerChannelRepository channelRepository,
            DataLoggerDeviceRepository deviceRepository,
            KafkaTemplate<String, Object> kafkaTemplate) {

        this.channelRepository = channelRepository;
        this.deviceRepository = deviceRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public void saveChannelReading(DataLoggerChannelReading channel) {

        // 1️⃣ Save channel reading
        channelRepository.save(channel);

        // 2️⃣ Fetch device using PRIMARY KEY (topic)
        DataLoggerDevice device = deviceRepository
                .findById(channel.getTopic())   // ✅ CORRECT
                .orElseThrow(() ->
                        new RuntimeException("DataLoggerDevice not found: " + channel.getTopic()));

        Boolean highTempAlarm = "ON".equalsIgnoreCase(channel.gethighTempAlarm());
        Boolean lowTempAlarm  = "ON".equalsIgnoreCase(channel.getlowTempAlarm());

        // 3️⃣ Create event with ownerUserId
        DataLoggerChannelAlertEvent event =
                new DataLoggerChannelAlertEvent(
                        device.getTopic(),
                        device.getOwnerUserId(),
                        channel.getChannelNumber(),
                        channel.getStatus(),
                        channel.getTemperature(),
                        channel.gethighTemp(),
                        channel.getlowTemp(),
                        highTempAlarm,
                        lowTempAlarm,
                        channel.getTimestamp()
                );

        // 4️⃣ Publish to Kafka
        kafkaTemplate
                .send("datalogger-channel-alert-topic",
                        event.getDeviceId(),
                        event);
    }

    // ============================================================
    // 🔹 DEVICE LEVEL ALERT
    // ============================================================

    @Transactional
    public void publishDeviceAlert(DataLoggerDevice device) {

        Boolean powerAlarm = "ON".equalsIgnoreCase(device.getPowerAlarm());
        Boolean batteryAlarm = "ON".equalsIgnoreCase(device.getBatteryAlarm());

        // 1️⃣ Create event including ownerUserId
        DataLoggerDeviceAlertEvent event =
                new DataLoggerDeviceAlertEvent(
                        device.getTopic(),
                        device.getOwnerUserId(),
                        powerAlarm,
                        batteryAlarm,
                        device.getBatteryPercentage(),
                        device.getLastTimestamp()
                );

        // 2️⃣ Publish to Kafka
        kafkaTemplate
                .send("datalogger-device-alert-topic",
                        event.getDeviceId(),
                        event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        logger.info("Device alert event sent");
                    } else {
                        logger.error("Device alert send failed", ex);
                        ex.printStackTrace();
                    }
                });
    }
}