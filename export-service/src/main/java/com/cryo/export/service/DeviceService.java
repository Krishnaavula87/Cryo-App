package com.cryo.export.service;

import com.cryo.export.dto.DeviceInfoDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class DeviceService {

    private final WebClient client;

    public DeviceService(@Value("${freezer.service.url}") String baseUrl) {

        this.client = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public List<DeviceInfoDto> getAllDevices() {

        try {

            DeviceInfoDto[] devices =
                    client.get()
                            .uri("/freezers/api/internal/devices")
                            .retrieve()
                            .bodyToMono(DeviceInfoDto[].class)
                            .onErrorResume(e -> {
                                System.out.println("Failed to fetch devices: " + e.getMessage());
                                return Mono.empty();
                            })
                            .block();

            if (devices == null) {
                return Collections.emptyList();
            }

            return Arrays.asList(devices);

        } catch (Exception e) {

            System.out.println("Device fetch error: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}