package com.example.evaluation_service.client;

import com.example.evaluation_service.DTO.RewardEvaluationSyncRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "recompense-service", url = "${recompense.service.url:http://localhost:8094}")
public interface RecompenseClient {

    @PostMapping("/api/rewards/process-evaluation")
    void processEvaluation(@RequestBody RewardEvaluationSyncRequest request);
}
