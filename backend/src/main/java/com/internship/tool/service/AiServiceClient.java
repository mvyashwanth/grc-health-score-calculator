package com.internship.tool.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Map;
import java.util.HashMap;

@Service
public class AiServiceClient {

    private final RestTemplate restTemplate;

    @Value("${ai.service.url:http://localhost:5000}")
    private String aiServiceUrl;

    public AiServiceClient() {
        this.restTemplate = new RestTemplate();
        // Note: 10s timeout would be configured in RestTemplate builder in production.
    }

    public Map<String, Object> describeHealthScore(Map<String, Object> healthData) {
        try {
            String url = aiServiceUrl + "/describe";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(healthData, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            
            return response.getBody();
        } catch (Exception e) {
            // Null return on error as per Day 4 AI Dev 2 spec
            return null;
        }
    }
}
