package com.example.recompense.RabbitMQ;

import com.example.recompense.DTO.EvaluationEvent;
import com.example.recompense.DTO.RewardEvaluationSyncRequest;
import com.example.recompense.Service.RewardEngineService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class EvaluationConsumer {

    private final RewardEngineService rewardEngineService;

    public EvaluationConsumer(RewardEngineService rewardEngineService) {
        this.rewardEngineService = rewardEngineService;
    }

    @RabbitListener(queues = "evaluation.queue")
    public void handleEvaluation(EvaluationEvent event) {
        RewardEvaluationSyncRequest request = new RewardEvaluationSyncRequest();
        request.setEvaluationId(event.getEvaluationId());
        request.setFreelancerEmail(event.getFreelancerEmail());
        request.setFreelancerName(event.getFreelancerName());
        request.setProjectName(event.getProjectName());
        request.setCurrentScore(event.getCurrentScore());
        request.setAverageScore(event.getAverageScore());
        request.setTotalPoints(event.getTotalPoints());
        request.setTotalEvaluations(event.getTotalEvaluations());
        request.setPositiveEvaluations(event.getPositiveEvaluations());
        request.setCompletedProjects(event.getCompletedProjects());
        rewardEngineService.processEvaluation(request);
    }
}
