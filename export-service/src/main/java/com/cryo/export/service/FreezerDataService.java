
package com.cryo.export.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class FreezerDataService {

    private final WebClient client;
    private final ObjectMapper mapper = new ObjectMapper();

    public FreezerDataService(
            @Value("${freezer.service.url}") String baseUrl) {

        this.client = WebClient.builder()
                .baseUrl(baseUrl)
                .codecs(configurer ->
                        configurer.defaultCodecs()
                                .maxInMemorySize(20 * 1024 * 1024) // ✅ 20 MB
                )
                .build();
    }

    // ✅ MAIN METHOD (use this instead of old getExportData)
    public String getExportData(String freezerId,
                                LocalDateTime from,
                                LocalDateTime to,
                                String auth) {

        try {
            return getExportDataInChunks(freezerId, from, to, auth);
        } catch (Exception e) {
            throw new RuntimeException("Error while fetching chunked export data", e);
        }
    }

    // ✅ CHUNK-BASED IMPLEMENTATION
    private String getExportDataInChunks(String freezerId,
                                         LocalDateTime from,
                                         LocalDateTime to,
                                         String auth) throws Exception {

        ArrayNode finalArray = mapper.createArrayNode();

        LocalDateTime chunkStart = from;

        while (chunkStart.isBefore(to)) {

            LocalDateTime chunkEnd = chunkStart.plusHours(6);

            if (chunkEnd.isAfter(to)) {
                chunkEnd = to;
            }

            String chunkData = fetchSingleChunk(
                    freezerId, chunkStart, chunkEnd, auth
            );

            if (chunkData != null && !chunkData.isEmpty()) {

                JsonNode node = mapper.readTree(chunkData);

                // ✅ SAFE MERGING
                if (node.isArray()) {
                    node.forEach(finalArray::add);
                } else {
                    finalArray.add(node);
                }
            }

            chunkStart = chunkEnd;
        }

        return mapper.writeValueAsString(finalArray);
    }

    // ✅ SINGLE API CALL
    private String fetchSingleChunk(String freezerId,
                                    LocalDateTime from,
                                    LocalDateTime to,
                                    String auth) {

        return client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/freezers/api/internal/export-data/{id}")
                        .queryParam("from", from)
                        .queryParam("to", to)
                        .build(freezerId))
                .headers(headers -> headers.setBearerAuth(auth))
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(60))
                .block();
    }

    // ✅ CHART DATA (no change needed, but buffer fix applies)
    public String getChartData(String freezerId,
                               LocalDateTime from,
                               LocalDateTime to,
                               String channel,
                               String auth) {

        return client.get()
                .uri(uriBuilder -> {

                    var builder = uriBuilder
                            .path("/freezers/api/internal/chart/{id}")
                            .queryParam("from", from)
                            .queryParam("to", to);

                    if (channel != null && !channel.isBlank()) {
                        builder.queryParam("channel", channel);
                    }

                    return builder.build(freezerId);
                })
                .headers(headers -> headers.setBearerAuth(auth))
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(60))
                .block();
    }
}