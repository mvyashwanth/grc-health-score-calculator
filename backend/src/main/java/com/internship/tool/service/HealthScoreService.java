package com.internship.tool.service;

import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class HealthScoreService {

    private final AiServiceClient aiServiceClient;

    public HealthScoreService(AiServiceClient aiServiceClient) {
        this.aiServiceClient = aiServiceClient;
    }

    /**
     * Day 5 Task: Integrate AiServiceClient into Service — call AI on create @Async, attach result, handle null gracefully
     */
    @Async
    public CompletableFuture<Void> enrichHealthScoreAsync(Long recordId, Map<String, Object> healthData) {
        // Step 1: Call AI asynchronously
        Map<String, Object> aiResult = aiServiceClient.describeHealthScore(healthData);

        // Step 2: Handle null gracefully
        if (aiResult == null) {
            System.err.println("AI Service failed to describe health score for record: " + recordId);
            return CompletableFuture.completedFuture(null);
        }

        // Step 3: Attach result (In real impl, this would update the repository record)
        System.out.println("Successfully generated AI insights for record " + recordId + ": " + aiResult);
        
        return CompletableFuture.completedFuture(null);
    }
}
