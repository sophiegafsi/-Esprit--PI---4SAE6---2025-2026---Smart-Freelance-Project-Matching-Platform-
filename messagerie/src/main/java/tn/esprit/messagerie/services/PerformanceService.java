package tn.esprit.messagerie.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.messagerie.Message;
import tn.esprit.messagerie.Conversation;
import tn.esprit.messagerie.DTO.PerformanceMetricsDTO;
import tn.esprit.messagerie.repository.MessageRepository;
import tn.esprit.messagerie.repository.ConversationRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PerformanceService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    private static final double ALPHA = 0.2; //
    private static final long MAX_ALLOWED_TIME_MINUTES = 480;

    public PerformanceMetricsDTO getFreelancerMetrics(Long freelancerId) {
        PerformanceMetricsDTO metrics = new PerformanceMetricsDTO();

        List<Conversation> freelancerConvs = conversationRepository.findAll().stream()
                .filter(c -> freelancerId.equals(c.getFreelancerId()))
                .collect(Collectors.toList());

        if (freelancerConvs.isEmpty()) {
            return metrics;
        }

        // 1. Response Speed Score & EWMA
        List<Long> responseTimes = calculateAllResponseTimes(freelancerId, freelancerConvs);
        double avgResponseTime = responseTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);

        double responseSpeedScore = Math.max(0, 100 - (avgResponseTime / MAX_ALLOWED_TIME_MINUTES * 100));
        metrics.setResponseSpeedScore(responseSpeedScore);

        double ewma = calculateEWMA(responseTimes);
        metrics.setEwma(ewma);

        // 2. Read Rate Score
        long totalReceived = 0;
        long totalRead = 0;
        for (Conversation conv : freelancerConvs) {
            List<Message> messages = messageRepository.findByConversation_Id(conv.getId());
            for (Message msg : messages) {
                if (!freelancerId.equals(msg.getSenderId())) {
                    totalReceived++;
                    if (Boolean.TRUE.equals(msg.getIsRead())) {
                        totalRead++;
                    }
                }
            }
        }
        double readRateScore = totalReceived == 0 ? 100 : (double) totalRead / totalReceived * 100;
        metrics.setReadRateScore(readRateScore);

        // 3. Resolution Rate
        long completed = freelancerConvs.stream().filter(c -> "COMPLETED".equalsIgnoreCase(c.getStatus())).count();
        double resolutionRate = (double) completed / freelancerConvs.size() * 100;
        metrics.setResolutionRate(resolutionRate);

        // 4. Professionalism Score
        metrics.setProfessionalismScore(calculateProfessionalismScore(freelancerId, freelancerConvs));

        // 5. PCS - Professional Communication Score
        double pcs = (0.4 * responseSpeedScore) + (0.3 * readRateScore) + (0.2 * resolutionRate)
                + (0.1 * metrics.getProfessionalismScore());
        metrics.setPcs(pcs);

        // 6. Productivity Efficiency Ratio (PER)
        metrics.setPer(calculatePER(freelancerId, freelancerConvs));

        // 7. Trust Index (Bayesian)
        metrics.setTrustIndex(calculateTrustIndex(freelancerId, pcs, freelancerConvs.size()));

        // 8. SLA Compliance
        metrics.setSlaCompliance(calculateSLA(responseTimes));

        return metrics;
    }

    public PerformanceMetricsDTO getConversationMetrics(Long conversationId) {
        PerformanceMetricsDTO metrics = new PerformanceMetricsDTO();
        Conversation conv = conversationRepository.findById(conversationId).orElse(null);
        if (conv == null)
            return metrics;

        List<Message> messages = messageRepository.findByConversation_Id(conversationId);
        if (messages.isEmpty())
            return metrics;

        // CEI - Conversation Engagement Index
        double interactionDensity = calculateInteractionDensity(messages);
        long durationMinutes = Duration
                .between(conv.getCreatedAt() != null ? conv.getCreatedAt() : messages.get(0).getSentAt(),
                        LocalDateTime.now())
                .toMinutes();
        if (durationMinutes == 0)
            durationMinutes = 1;
        double cei = (messages.size() * interactionDensity) / durationMinutes;
        metrics.setCei(cei);

        // Risk Score
        metrics.setRiskScore(calculateRiskScore(conv, messages));

        // Volatility
        double actions = messages.stream()
                .filter(m -> Boolean.TRUE.equals(m.getIsEdited()) || Boolean.TRUE.equals(m.getIsDeleted())).count();
        metrics.setVolatility(actions / messages.size());

        return metrics;
    }

    private List<Long> calculateAllResponseTimes(Long freelancerId, List<Conversation> conversations) {
        List<Long> times = new ArrayList<>();
        for (Conversation conv : conversations) {
            List<Message> messages = messageRepository.findByConversation_IdOrderBySentAtAsc(conv.getId());
            for (int i = 0; i < messages.size() - 1; i++) {
                Message current = messages.get(i);
                Message next = messages.get(i + 1);
                // If client sends message and freelancer replies
                if (!freelancerId.equals(current.getSenderId()) && freelancerId.equals(next.getSenderId())) {
                    times.add(Duration.between(current.getSentAt(), next.getSentAt()).toMinutes());
                }
            }
        }
        return times;
    }

    private double calculateEWMA(List<Long> responseTimes) {
        double ewma = 0;
        for (Long time : responseTimes) {
            ewma = (ALPHA * time) + ((1 - ALPHA) * ewma);
        }
        return ewma;
    }

    private double calculateProfessionalismScore(Long freelancerId, List<Conversation> conversations) {
        long totalSent = 0;
        long deleted = 0;
        long flagged = 0;
        long edited = 0;

        for (Conversation conv : conversations) {
            List<Message> messages = messageRepository.findByConversation_Id(conv.getId());
            for (Message msg : messages) {
                if (freelancerId.equals(msg.getSenderId())) {
                    totalSent++;
                    if (Boolean.TRUE.equals(msg.getIsDeleted()))
                        deleted++;
                    if (Boolean.TRUE.equals(msg.getIsFlagged()))
                        flagged++;
                    if (Boolean.TRUE.equals(msg.getIsEdited()))
                        edited++;
                }
            }
        }

        if (totalSent == 0)
            return 100;
        double penalty = ((double) deleted / totalSent * 20) + ((double) flagged / totalSent * 50)
                + ((double) edited / totalSent * 10);
        return Math.max(0, 100 - penalty);
    }

    private double calculatePER(Long freelancerId, List<Conversation> conversations) {
        long total = 0;
        long useful = 0;
        for (Conversation conv : conversations) {
            List<Message> messages = messageRepository.findByConversation_Id(conv.getId());
            for (Message msg : messages) {
                if (freelancerId.equals(msg.getSenderId())) {
                    total++;
                    if (!Boolean.TRUE.equals(msg.getIsDeleted()) && !Boolean.TRUE.equals(msg.getIsEdited())
                            && !Boolean.TRUE.equals(msg.getIsFlagged())) {
                        useful++;
                    }
                }
            }
        }
        return total == 0 ? 100 : (double) useful / total * 100;
    }

    private double calculateTrustIndex(Long freelancerId, double currentScore, int totalProjects) {
        double oldTrust = 80.0; // Assume baseline trust
        return ((oldTrust * totalProjects) + currentScore) / (totalProjects + 1);
    }

    private double calculateSLA(List<Long> responseTimes) {
        if (responseTimes.isEmpty())
            return 100;
        long withinTarget = responseTimes.stream().filter(t -> t <= 120).count(); // 2 hours target
        return (double) withinTarget / responseTimes.size() * 100;
    }

    private double calculateInteractionDensity(List<Message> messages) {
        int backAndForth = 0;
        for (int i = 0; i < messages.size() - 1; i++) {
            if (!messages.get(i).getSenderId().equals(messages.get(i + 1).getSenderId())) {
                backAndForth++;
            }
        }
        return (double) backAndForth / messages.size();
    }

    private double calculateRiskScore(Conversation conv, List<Message> messages) {
        long inactivityDays = Duration
                .between(conv.getLastMessageAt() != null ? conv.getLastMessageAt() : LocalDateTime.now(),
                        LocalDateTime.now())
                .toDays();
        double inactivityScore = Math.min(100, inactivityDays * 10);

        long unanswered = 0;
        if (!messages.isEmpty()) {
            Message last = messages.get(messages.size() - 1);
            // If last message is from client, it's unanswered
            if (conv.getClientId().equals(last.getSenderId())) {
                unanswered = 1;
            }
        }

        return (0.5 * inactivityScore) + (0.3 * unanswered * 100) + (0.2 * 0); // Late penalty simplified
    }
}
