package com.cryo.export.service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class NextcloudStorageService {

    private final WebClient webClient;

    @Value("${nextcloud.base-url}")
    private String baseUrl;

    @Value("${nextcloud.username}")
    private String username;

    @Value("${nextcloud.password}")
    private String password;

    @Value("${nextcloud.root-folder}")
    private String rootFolder;

    public NextcloudStorageService(WebClient.Builder builder) {
        this.webClient = builder.build();
    }

    public void createFolder(String path) {

        try {

            webClient
                    .method(org.springframework.http.HttpMethod.valueOf("MKCOL"))
                    .uri(baseUrl + "/" + path)
                    .headers(h -> h.setBasicAuth(username, password))
                    .exchangeToMono(response -> {

                        if (response.statusCode().is2xxSuccessful()
                                || response.statusCode().value() == 405) {
                            return response.releaseBody();
                        }

                        return response.createException().flatMap(Mono::error);
                    })
                    .block();

            System.out.println("Folder ensured: " + path);

        } catch (Exception e) {
            System.out.println("Folder creation failed: " + path);
            e.printStackTrace();
        }
    }
    public void uploadFile(String path, byte[] data) {

        try {

            webClient.put()
                    .uri(baseUrl + "/" + path)
                    .headers(h -> h.setBasicAuth(username, password))
                    .bodyValue(data)
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            System.out.println("File uploaded: " + path);

        } catch (Exception e) {
            System.out.println("Upload failed: " + path);
            e.printStackTrace();
        }
    }

    public String root() {
        return rootFolder;
    }
}