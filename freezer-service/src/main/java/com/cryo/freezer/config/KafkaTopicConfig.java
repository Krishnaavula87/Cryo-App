package com.cryo.freezer.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {
    @Bean
    public NewTopic freezerAlertTopic()
    {
        return new NewTopic("freezer-alert-topic",10, (short) 1);
    }

    // 🔹 DataLogger Channel Alerts
    @Bean
    public NewTopic dataloggerChannelAlertTopic() {
        return new NewTopic("datalogger-channel-alert-topic", 10, (short) 1);
    }

    // 🔹 DataLogger Device Alerts
    @Bean
    public NewTopic dataloggerDeviceAlertTopic() {
        return new NewTopic("datalogger-device-alert-topic", 10, (short) 1);
    }

//    @Bean
//    public ObjectMapper objectMapper() {
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.registerModule(new JavaTimeModule());
//        return mapper;
//    }
}
