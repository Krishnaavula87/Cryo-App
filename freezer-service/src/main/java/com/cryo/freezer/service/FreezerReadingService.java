package com.cryo.freezer.service;
import com.cryo.freezer.entity.Freezer;
import com.cryo.freezer.entity.FreezerReading;
//import com.cryo.freezer.event.FreezerAlertEvent;
import com.cryo.common.event.FreezerAlertEvent;

import com.cryo.freezer.repository.FreezerReadingRepository;
import com.cryo.freezer.repository.FreezerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FreezerReadingService {
    private static final Logger logger = LoggerFactory.getLogger(FreezerReadingService.class);
    private final FreezerReadingRepository freezerReadingRepository;
    private final FreezerRepository freezerRepository;



    private final WebClient authServiceClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    // ✅ MEMORY CACHE: Stores last 60 readings for every freezerL
    // Key: FreezerID, Value: List of temperatures
    private final Map<String, LinkedList<BigDecimal>> rollingWindowCache = new ConcurrentHashMap<>();

    public FreezerReadingService(FreezerReadingRepository freezerReadingRepository,
                                 FreezerRepository freezerRepository,

                                 @Value("${auth.service.url:http://localhost:8081}") String authServiceUrl, KafkaTemplate<String, Object> kafkaTemplate) {

        this.freezerReadingRepository = freezerReadingRepository;
        this.freezerRepository = freezerRepository;

        this.authServiceClient = WebClient.builder().baseUrl(authServiceUrl).build();
        this.kafkaTemplate = kafkaTemplate;
    }


    @Transactional
    public FreezerReading saveReading(FreezerReading reading) {

        FreezerReading saved = freezerReadingRepository.save(reading);
        // 🔹 Fetch freezer to get ownerUserId
        Freezer freezer = freezerRepository
                .findByFreezerId(reading.getFreezerId())
                .orElseThrow(() ->
                        new RuntimeException("Freezer not found: " + reading.getFreezerId()));


        // Update rolling average
        if (reading.getTemperature() != null) {
            updateRollingAverage(reading.getFreezerId(), reading.getTemperature());
        }

        // 🔥 Publish event to Kafka
        FreezerAlertEvent event = new FreezerAlertEvent(
                reading.getFreezerId(),
                freezer.getOwnerUserId(),  // ✅ ADD THIS
                reading.getTemperature(),
                reading.getHighTemp(),
                reading.getLowTemp(),
                reading.getBatteryPercentage(),
                reading.getDoorAlarm(),
                reading.getPowerAlarm(),
                reading.getFreezerCompressor(),
                reading.getHighTempAlarm(),
                reading.getLowTempAlarm(),
                reading.getBatteryAlarm(),
                reading.getTimestamp()
        );



        // kafkaTemplate.send("freezer-alert-topic", event.getFreezerId(), event);
        kafkaTemplate
                .send("freezer-alert-topic", event.getFreezerId(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        logger.info("Message sent to Kafka topic freezer-alert-topic");
                    } else {
                        logger.error("Kafka send failed", ex);
                        ex.printStackTrace();
                    }
                });

        return saved;
    }

    // ✅ HELPER: Updates the list in memory (Max 60 items)
    private void updateRollingAverage(String freezerId, BigDecimal temp) {
        rollingWindowCache.compute(freezerId, (key, list) -> {
            if (list == null) list = new LinkedList<>();
            list.add(temp);
            // Keep only last 60 seconds
            if (list.size() > 60) {
                list.removeFirst();
            }
            return list;
        });
    }

    // ✅ PUBLIC METHOD: Called by Dashboard to get instant average
    public Double getFastOneMinuteAverage(String freezerId) {
        LinkedList<BigDecimal> list = rollingWindowCache.get(freezerId);

        if (list == null || list.isEmpty()) {
            return null;
        }

        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal val : list) {
            sum = sum.add(val);
        }

        // Calculate Average: Sum / Count
        return sum.divide(BigDecimal.valueOf(list.size()), 2, RoundingMode.HALF_UP).doubleValue();
    }
}