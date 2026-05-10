package com.internship.tool.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
@Slf4j
public class AiServiceClient {

    private final RestTemplate restTemplate;
    private final String aiServiceUrl;

    public AiServiceClient(@Value("${ai.service.url}") String aiServiceUrl) {
        this.restTemplate = new RestTemplate();
        this.aiServiceUrl = aiServiceUrl;
        // Set 10s timeout
        var factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(10000);
        this.restTemplate.setRequestFactory(factory);
    }

    public String describe(Map<String, Object> healthData) {
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    aiServiceUrl + "/describe", healthData, Map.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Object desc = response.getBody().get("description");
                return desc != null ? desc.toString() : null;
            }
        } catch (Exception e) {
            log.error("AI describe call failed: {}", e.getMessage());
        }
        return null;
    }

    public String recommend(Map<String, Object> healthData) {
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    aiServiceUrl + "/recommend", healthData, Map.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Object recs = response.getBody().get("recommendations");
                return recs != null ? recs.toString() : null;
            }
        } catch (Exception e) {
            log.error("AI recommend call failed: {}", e.getMessage());
        }
        return null;
    }

    public String generateReport(Map<String, Object> healthData) {
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    aiServiceUrl + "/generate-report", healthData, Map.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Object report = response.getBody().get("report");
                return report != null ? report.toString() : null;
            }
        } catch (Exception e) {
            log.error("AI generate-report call failed: {}", e.getMessage());
        }
        return null;
    }
}
