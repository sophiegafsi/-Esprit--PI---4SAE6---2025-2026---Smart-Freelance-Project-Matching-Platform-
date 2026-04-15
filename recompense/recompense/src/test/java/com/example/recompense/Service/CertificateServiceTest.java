package com.example.recompense.Service;

import com.example.recompense.DTO.RewardEvaluationSyncRequest;
import com.example.recompense.Entity.RewardHistory;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CertificateServiceTest {

    private final CertificateService certificateService = new CertificateService();

    @Test
    void generateCertificate_fromRequest_shouldProducePdfBytes() {
        RewardEvaluationSyncRequest request = new RewardEvaluationSyncRequest();
        request.setFreelancerName("Amal");
        request.setFreelancerEmail("amal@test.com");
        request.setAverageScore(4.8);
        request.setTotalPoints(250);
        request.setCurrentScore(5);
        request.setEvaluatedAt(LocalDateTime.of(2026, 4, 14, 16, 10));

        byte[] pdf = certificateService.generateCertificate(request, "Expert");

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 4, StandardCharsets.US_ASCII)).isEqualTo("%PDF");
    }

    @Test
    void generateCertificate_fromHistory_shouldProducePdfBytes() {
        RewardHistory history = new RewardHistory();
        history.setUserName("Hajer");
        history.setUserEmail("hajer@test.com");
        history.setRewardName("Gold");
        history.setEventDate(LocalDateTime.of(2026, 4, 14, 17, 0));
        history.setAverageScoreSnapshot(5.0);
        history.setTotalPointsSnapshot(500);
        history.setScoreSnapshot(5);

        byte[] pdf = certificateService.generateCertificate(history);

        assertThat(pdf.length).isGreaterThan(1000);
        assertThat(new String(pdf, 0, 4, StandardCharsets.US_ASCII)).isEqualTo("%PDF");
    }
}
