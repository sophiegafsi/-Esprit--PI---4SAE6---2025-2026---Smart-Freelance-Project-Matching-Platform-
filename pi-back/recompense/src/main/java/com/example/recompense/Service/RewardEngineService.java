package com.example.recompense.Service;

import com.example.recompense.DTO.FreelancerRewardInsightDTO;
import com.example.recompense.DTO.MonthlyRewardProgressDTO;
import com.example.recompense.DTO.RewardDashboardDTO;
import com.example.recompense.DTO.RewardEvaluationSyncRequest;
import com.example.recompense.DTO.RewardOpportunityDTO;
import com.example.recompense.DTO.RewardProcessingResponse;
import com.example.recompense.DTO.TopFreelancerDTO;
import com.example.recompense.Entity.Badge;
import com.example.recompense.Entity.FreelancerRewardProfile;
import com.example.recompense.Entity.Recompense;
import com.example.recompense.Entity.RewardHistory;
import com.example.recompense.Entity.UserBadge;
import com.example.recompense.Entity.UserPoints;
import com.example.recompense.Repository.FreelancerRewardProfileRepository;
import com.example.recompense.Repository.RecompenseRepository;
import com.example.recompense.Repository.RewardHistoryRepository;
import com.example.recompense.Repository.UserBadgeRepository;
import com.example.recompense.Repository.UserPointsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class RewardEngineService {

    private static final String SCORE_BADGE_TYPE = "SCORE_BADGE";
    private static final String POINTS_BADGE_TYPE = "POINTS_BADGE";
    private static final String LEVEL_TYPE = "LEVEL";
    private static final String RECOMPENSE_TYPE = "RECOMPENSE";
    private static final String AWARDED_ACTION = "AWARDED";
    private static final String REVOKED_ACTION = "REVOKED";
    private static final String UPGRADED_ACTION = "UPGRADED";
    private static final String DOWNGRADED_ACTION = "DOWNGRADED";
    private static final String SCORE_CONDITION = "AVERAGE_SCORE";
    private static final String POINTS_CONDITION = "POINTS";

    private final BadgeService badgeService;
    private final UserBadgeRepository userBadgeRepository;
    private final UserPointsRepository userPointsRepository;
    private final FreelancerRewardProfileRepository profileRepository;
    private final RecompenseRepository recompenseRepository;
    private final RewardHistoryRepository rewardHistoryRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final CertificateService certificateService;

    public RewardEngineService(BadgeService badgeService,
                               UserBadgeRepository userBadgeRepository,
                               UserPointsRepository userPointsRepository,
                               FreelancerRewardProfileRepository profileRepository,
                               RecompenseRepository recompenseRepository,
                               RewardHistoryRepository rewardHistoryRepository,
                               NotificationService notificationService,
                               EmailService emailService,
                               CertificateService certificateService) {
        this.badgeService = badgeService;
        this.userBadgeRepository = userBadgeRepository;
        this.userPointsRepository = userPointsRepository;
        this.profileRepository = profileRepository;
        this.recompenseRepository = recompenseRepository;
        this.rewardHistoryRepository = rewardHistoryRepository;
        this.notificationService = notificationService;
        this.emailService = emailService;
        this.certificateService = certificateService;
    }

    public RewardProcessingResponse processEvaluation(RewardEvaluationSyncRequest request) {
        validate(request);

        FreelancerRewardProfile profile = profileRepository.findByUserEmail(request.getFreelancerEmail())
                .orElseGet(() -> createProfile(request));

        syncPoints(request);
        updateProfileSnapshot(profile, request);

        Badge scoreBadge = request.getTotalEvaluations() != null && request.getTotalEvaluations() > 0
                ? badgeService.findBestAutoBadge(SCORE_CONDITION, request.getAverageScore())
                : null;
        Badge pointsBadge = badgeService.findBestAutoBadge(
                POINTS_CONDITION,
                request.getTotalPoints() == null ? null : request.getTotalPoints().doubleValue()
        );

        String nextLevel = calculateLevel(request);
        transitionLevel(profile, nextLevel, request);

        profile.setCurrentScoreBadge(
                transitionBadge(profile, profile.getCurrentScoreBadge(), scoreBadge, SCORE_BADGE_TYPE, request)
        );
        profile.setCurrentPointsBadge(
                transitionBadge(profile, profile.getCurrentPointsBadge(), pointsBadge, POINTS_BADGE_TYPE, request)
        );

        profileRepository.save(profile);
        awardEligibleRecompenses(profile, request);

        RewardProcessingResponse response = new RewardProcessingResponse();
        response.setFreelancerEmail(profile.getUserEmail());
        response.setFreelancerName(profile.getUserName());
        response.setAverageScore(profile.getAverageScore());
        response.setTotalPoints(profile.getTotalPoints());
        response.setCurrentScoreBadge(profile.getCurrentScoreBadge());
        response.setCurrentPointsBadge(profile.getCurrentPointsBadge());
        response.setCurrentLevel(profile.getCurrentLevel());
        response.setMessage("Reward engine processed evaluation successfully.");
        return response;
    }

    public int recalculateStoredLevels() {
        List<FreelancerRewardProfile> profiles = profileRepository.findAll();
        int updated = 0;

        for (FreelancerRewardProfile profile : profiles) {
            String nextLevel = calculateLevel(
                    profile.getAverageScore(),
                    profile.getCompletedProjects(),
                    profile.getPositiveEvaluations(),
                    profile.getTotalPoints()
            );

            if (!Objects.equals(profile.getCurrentLevel(), nextLevel)) {
                profile.setCurrentLevel(nextLevel);
                profileRepository.save(profile);
                updated++;
            }
        }

        return updated;
    }

    public int assignPendingRecompenses() {
        int assigned = 0;

        for (FreelancerRewardProfile profile : profileRepository.findAll()) {
            assigned += awardEligibleRecompenses(profile, buildRewardRequestFromProfile(profile));
        }

        return assigned;
    }

    public RewardDashboardDTO getDashboard() {
        List<FreelancerRewardProfile> profiles = profileRepository.findAll();
        List<RewardHistory> history = rewardHistoryRepository.findAllByOrderByEventDateDesc();

        RewardDashboardDTO dashboard = new RewardDashboardDTO();
        dashboard.setTotalBadgesAssigned(
                history.stream()
                        .filter(this::isBadgeAwardEvent)
                        .count()
        );
        dashboard.setActiveBadges(userBadgeRepository.countByActiveTrue());
        dashboard.setMostFrequentBadge(findMostFrequentBadge(history));

        List<FreelancerRewardProfile> noRewardProfiles = profiles.stream()
                .filter(profile -> profile.getCurrentScoreBadge() == null && profile.getCurrentPointsBadge() == null)
                .sorted(Comparator.comparing(FreelancerRewardProfile::getUserEmail, String.CASE_INSENSITIVE_ORDER))
                .toList();

        dashboard.setFreelancersWithoutRewardCount(noRewardProfiles.size());
        dashboard.setFreelancersWithoutReward(
                noRewardProfiles.stream()
                        .map(FreelancerRewardProfile::getUserEmail)
                        .toList()
        );
        dashboard.setTopFreelancers(buildTopFreelancers(profiles));
        dashboard.setMonthlyProgress(buildMonthlyProgress(history));
        return dashboard;
    }

    @Transactional(readOnly = true)
    public List<FreelancerRewardProfile> getAllProfiles() {
        return profileRepository.findAll().stream()
                .sorted(Comparator.comparing(FreelancerRewardProfile::getUpdatedAt).reversed())
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<FreelancerRewardProfile> getProfile(String email) {
        return profileRepository.findByUserEmail(email);
    }

    @Transactional(readOnly = true)
    public List<FreelancerRewardInsightDTO> getAllRewardInsights() {
        return profileRepository.findAll().stream()
                .sorted(Comparator.comparing(FreelancerRewardProfile::getUpdatedAt).reversed())
                .map(this::buildRewardInsight)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<FreelancerRewardInsightDTO> getRewardInsight(String email) {
        return profileRepository.findByUserEmail(email)
                .map(this::buildRewardInsight);
    }

    @Transactional(readOnly = true)
    public List<RewardHistory> getAllHistory() {
        return rewardHistoryRepository.findAllByOrderByEventDateDesc();
    }

    @Transactional(readOnly = true)
    public List<RewardHistory> getHistoryForUser(String email) {
        return rewardHistoryRepository.findByUserEmailOrderByEventDateDesc(email);
    }

    @Transactional(readOnly = true)
    public byte[] generateCertificate(Long historyId) {
        RewardHistory history = rewardHistoryRepository.findById(historyId)
                .orElseThrow(() -> new IllegalArgumentException("Reward history not found with id " + historyId));

        if (history.getRewardName() == null || history.getRewardName().isBlank()) {
            throw new IllegalStateException("This reward history item is missing the reward name.");
        }

        return certificateService.generateCertificate(history);
    }

    public void resendRewardEmail(Long historyId, String recipientEmail) {
        RewardHistory history = rewardHistoryRepository.findById(historyId)
                .orElseThrow(() -> new IllegalArgumentException("Reward history not found with id " + historyId));

        String targetEmail = (recipientEmail != null && !recipientEmail.isBlank())
                ? recipientEmail.trim()
                : history.getUserEmail();

        if (targetEmail == null || targetEmail.isBlank()) {
            throw new IllegalStateException("This reward history item has no recipient email.");
        }

        byte[] certificate = Boolean.TRUE.equals(history.getCertificateGenerated())
                ? certificateService.generateCertificate(history)
                : null;

        Optional<FreelancerRewardProfile> profile = profileRepository.findByUserEmail(history.getUserEmail());
        String displayName = safe(history.getUserName(),
                profile.map(FreelancerRewardProfile::getUserName).orElse(history.getUserEmail()));
        String level = profile.map(FreelancerRewardProfile::getCurrentLevel).orElse("N/A");

        String emailBody = "Hello " + displayName + ",\n\n"
                + "Here is your reward summary for \"" + safe(history.getRewardName(), "reward") + "\".\n"
                + "Average score: " + formatDecimal(history.getAverageScoreSnapshot()) + "\n"
                + "Total points: " + safeInteger(history.getTotalPointsSnapshot()) + "\n"
                + "Level: " + level + "\n"
                + "Date: " + history.getEventDate() + "\n\n"
                + "Your certificate is attached when available.";

        emailService.sendBadgeEmail(
                targetEmail,
                "Your reward certificate: " + safe(history.getRewardName(), "reward"),
                emailBody,
                certificate,
                certificate == null ? null : buildCertificateFileName(history.getUserEmail(), history.getRewardName())
        );
    }

    private void validate(RewardEvaluationSyncRequest request) {
        if (request.getFreelancerEmail() == null || request.getFreelancerEmail().isBlank()) {
            throw new IllegalArgumentException("freelancerEmail is required");
        }
        if (request.getCurrentScore() == null) {
            throw new IllegalArgumentException("currentScore is required");
        }
        if (request.getAverageScore() == null) {
            throw new IllegalArgumentException("averageScore is required");
        }
        if (request.getTotalPoints() == null) {
            throw new IllegalArgumentException("totalPoints is required");
        }
        if (request.getTotalEvaluations() == null) {
            throw new IllegalArgumentException("totalEvaluations is required");
        }
        if (request.getPositiveEvaluations() == null) {
            throw new IllegalArgumentException("positiveEvaluations is required");
        }
        if (request.getCompletedProjects() == null) {
            throw new IllegalArgumentException("completedProjects is required");
        }
    }

    private FreelancerRewardProfile createProfile(RewardEvaluationSyncRequest request) {
        FreelancerRewardProfile profile = new FreelancerRewardProfile();
        profile.setUserEmail(request.getFreelancerEmail());
        profile.setUserName(request.getFreelancerName());
        return profile;
    }

    private void updateProfileSnapshot(FreelancerRewardProfile profile, RewardEvaluationSyncRequest request) {
        profile.setUserEmail(request.getFreelancerEmail());
        if (request.getFreelancerName() != null && !request.getFreelancerName().isBlank()) {
            profile.setUserName(request.getFreelancerName());
        }
        profile.setAverageScore(request.getAverageScore());
        profile.setLatestScore(request.getCurrentScore());
        profile.setTotalEvaluations(request.getTotalEvaluations());
        profile.setPositiveEvaluations(request.getPositiveEvaluations());
        profile.setCompletedProjects(request.getCompletedProjects());
        profile.setTotalPoints(request.getTotalPoints());
        profile.setLastEvaluationAt(resolveEventDate(request));
    }

    private void syncPoints(RewardEvaluationSyncRequest request) {
        UserPoints userPoints = userPointsRepository.findOptionalByUserEmail(request.getFreelancerEmail())
                .orElseGet(UserPoints::new);

        userPoints.setUserEmail(request.getFreelancerEmail());
        userPoints.setPoints(request.getTotalPoints());
        userPointsRepository.save(userPoints);
    }

    private String transitionBadge(FreelancerRewardProfile profile,
                                   String currentBadgeName,
                                   Badge newBadge,
                                   String rewardType,
                                   RewardEvaluationSyncRequest request) {

        String nextBadgeName = newBadge == null ? null : newBadge.getName();
        if (equalsIgnoreCase(currentBadgeName, nextBadgeName)) {
            return currentBadgeName;
        }

        if (currentBadgeName != null && !currentBadgeName.isBlank()) {
            revokeBadge(profile, currentBadgeName, rewardType, request);
        }

        if (newBadge != null) {
            awardBadge(profile, newBadge, rewardType, request);
            return newBadge.getName();
        }

        return null;
    }

    private void awardBadge(FreelancerRewardProfile profile,
                            Badge badge,
                            String rewardType,
                            RewardEvaluationSyncRequest request) {

        if (userBadgeRepository.existsByUserNameAndBadgeAndActiveTrue(profile.getUserEmail(), badge)) {
            return;
        }

        UserBadge userBadge = new UserBadge();
        userBadge.setUserName(profile.getUserEmail());
        userBadge.setDisplayName(profile.getUserName());
        userBadge.setBadge(badge);
        userBadge.setDateAssigned(resolveEventDate(request));
        userBadge.setActive(true);
        userBadge.setStatusReason(buildAwardReason(badge, request));
        userBadge.setEvaluationId(request.getEvaluationId());
        userBadge.setScoreSnapshot(request.getCurrentScore());
        userBadge.setAverageScoreSnapshot(request.getAverageScore());
        userBadge.setTotalPointsSnapshot(request.getTotalPoints());

        RewardHistory history = buildHistory(profile, badge.getName(), rewardType, AWARDED_ACTION, request,
                userBadge.getStatusReason());

        boolean certificateEligible = Boolean.TRUE.equals(badge.getCertificateEligible());
        byte[] certificate = null;
        String attachmentName = null;

        if (certificateEligible) {
            certificate = certificateService.generateCertificate(request, badge.getName());
            attachmentName = buildCertificateFileName(profile.getUserEmail(), badge.getName());
            userBadge.setCertificateGenerated(true);
            history.setCertificateGenerated(true);
        }

        userBadgeRepository.save(userBadge);
        rewardHistoryRepository.save(history);
        profile.setTotalBadgesAwarded(profile.getTotalBadgesAwarded() + 1);

        String notificationMessage = "New badge unlocked: " + badge.getName()
                + " | average score " + formatDecimal(request.getAverageScore())
                + " | total points " + request.getTotalPoints();
        notificationService.createAndBroadcast(profile.getUserEmail(), notificationMessage);

        String emailBody = "Hello " + safe(profile.getUserName(), profile.getUserEmail()) + ",\n\n"
                + "You unlocked the badge \"" + badge.getName() + "\".\n"
                + "Average score: " + formatDecimal(request.getAverageScore()) + "\n"
                + "Total points: " + request.getTotalPoints() + "\n"
                + "Level: " + safe(profile.getCurrentLevel(), "N/A") + "\n\n"
                + "Keep up the great work.";
        emailService.sendBadgeEmail(
                profile.getUserEmail(),
                "New badge unlocked: " + badge.getName(),
                emailBody,
                certificate,
                attachmentName
        );
    }

    private void revokeBadge(FreelancerRewardProfile profile,
                             String badgeName,
                             String rewardType,
                             RewardEvaluationSyncRequest request) {

        List<UserBadge> activeBadges = userBadgeRepository.findByUserNameAndActiveTrueOrderByDateAssignedDesc(
                profile.getUserEmail()
        );

        List<UserBadge> matchingBadges = activeBadges.stream()
                .filter(userBadge -> userBadge.getBadge() != null)
                .filter(userBadge -> equalsIgnoreCase(userBadge.getBadge().getName(), badgeName))
                .toList();

        if (matchingBadges.isEmpty()) {
            rewardHistoryRepository.save(buildHistory(
                    profile,
                    badgeName,
                    rewardType,
                    REVOKED_ACTION,
                    request,
                    "Badge revoked after metrics changed."
            ));
            return;
        }

        for (UserBadge userBadge : matchingBadges) {
            userBadge.setActive(false);
            userBadge.setRevokedAt(resolveEventDate(request));
            userBadge.setStatusReason("Badge revoked after metrics changed.");
            userBadgeRepository.save(userBadge);
        }

        rewardHistoryRepository.save(buildHistory(
                profile,
                badgeName,
                rewardType,
                REVOKED_ACTION,
                request,
                "Badge revoked after metrics changed."
        ));

        notificationService.createAndBroadcast(
                profile.getUserEmail(),
                "Badge revoked: " + badgeName + " because the latest metrics no longer satisfy the threshold."
        );
    }

    private int awardEligibleRecompenses(FreelancerRewardProfile profile,
                                         RewardEvaluationSyncRequest request) {

        int totalPoints = safeInteger(request.getTotalPoints());
        int assigned = 0;

        List<Recompense> eligibleRecompenses = recompenseRepository.findByIsActiveTrue()
                .stream()
                .filter(recompense -> safeInteger(recompense.getPointsRequired()) <= totalPoints)
                .filter(recompense -> recompense.getStock() == null || recompense.getStock() == -1 || recompense.getStock() > 0)
                .sorted(Comparator.comparing(Recompense::getPointsRequired, Comparator.nullsFirst(Integer::compareTo)))
                .toList();

        for (Recompense recompense : eligibleRecompenses) {
            boolean alreadyAwarded = rewardHistoryRepository.existsByUserEmailAndRewardNameAndRewardTypeAndActionType(
                    profile.getUserEmail(),
                    recompense.getTitle(),
                    RECOMPENSE_TYPE,
                    AWARDED_ACTION
            );

            if (alreadyAwarded) {
                continue;
            }

            String description = "Automatic reward unlocked after reaching "
                    + safeInteger(recompense.getPointsRequired())
                    + " points.";

            RewardHistory history = buildHistory(
                    profile,
                    recompense.getTitle(),
                    RECOMPENSE_TYPE,
                    AWARDED_ACTION,
                    request,
                    description
            );
            rewardHistoryRepository.save(history);

            if (recompense.getStock() != null && recompense.getStock() > 0) {
                recompense.setStock(recompense.getStock() - 1);
                recompenseRepository.save(recompense);
            }

            notificationService.createAndBroadcast(
                    profile.getUserEmail(),
                    "Reward unlocked: " + recompense.getTitle()
                            + " | total points " + totalPoints
            );

            String emailBody = "Hello " + safe(profile.getUserName(), profile.getUserEmail()) + ",\n\n"
                    + "You unlocked the reward \"" + recompense.getTitle() + "\".\n"
                    + "Required points: " + safeInteger(recompense.getPointsRequired()) + "\n"
                    + "Your total points: " + totalPoints + "\n"
                    + "Average score: " + formatDecimal(request.getAverageScore()) + "\n"
                    + "Level: " + safe(profile.getCurrentLevel(), "N/A") + "\n\n"
                    + safe(recompense.getDescription(), "Congratulations on your new reward.");

            emailService.sendBadgeEmail(
                    profile.getUserEmail(),
                    "New reward unlocked: " + recompense.getTitle(),
                    emailBody,
                    null,
                    null
            );
            assigned++;
        }

        return assigned;
    }

    private void transitionLevel(FreelancerRewardProfile profile,
                                 String nextLevel,
                                 RewardEvaluationSyncRequest request) {

        String currentLevel = profile.getCurrentLevel();
        if (Objects.equals(currentLevel, nextLevel)) {
            return;
        }

        String action = levelRank(nextLevel) >= levelRank(currentLevel) ? UPGRADED_ACTION : DOWNGRADED_ACTION;
        profile.setCurrentLevel(nextLevel);

        RewardHistory history = buildHistory(
                profile,
                nextLevel,
                LEVEL_TYPE,
                action,
                request,
                "Freelancer level changed from " + safe(currentLevel, "N/A") + " to " + nextLevel + "."
        );
        rewardHistoryRepository.save(history);

        notificationService.createAndBroadcast(
                profile.getUserEmail(),
                "Level updated: " + nextLevel
                        + " | completed projects " + request.getCompletedProjects()
                        + " | positive evaluations " + request.getPositiveEvaluations()
        );
    }

    private RewardHistory buildHistory(FreelancerRewardProfile profile,
                                       String rewardName,
                                       String rewardType,
                                       String actionType,
                                       RewardEvaluationSyncRequest request,
                                       String description) {

        RewardHistory history = new RewardHistory();
        history.setUserEmail(profile.getUserEmail());
        history.setUserName(profile.getUserName());
        history.setRewardName(rewardName);
        history.setRewardType(rewardType);
        history.setActionType(actionType);
        history.setDescription(description);
        history.setEvaluationId(request.getEvaluationId());
        history.setScoreSnapshot(request.getCurrentScore());
        history.setAverageScoreSnapshot(request.getAverageScore());
        history.setTotalPointsSnapshot(request.getTotalPoints());
        history.setTotalEvaluationsSnapshot(request.getTotalEvaluations());
        history.setPositiveEvaluationsSnapshot(request.getPositiveEvaluations());
        history.setCompletedProjectsSnapshot(request.getCompletedProjects());
        history.setEventDate(resolveEventDate(request));
        return history;
    }

    private RewardEvaluationSyncRequest buildRewardRequestFromProfile(FreelancerRewardProfile profile) {
        RewardEvaluationSyncRequest request = new RewardEvaluationSyncRequest();
        request.setFreelancerEmail(profile.getUserEmail());
        request.setFreelancerName(profile.getUserName());
        request.setCurrentScore(safeInteger(profile.getLatestScore()));
        request.setAverageScore(profile.getAverageScore() == null ? 0.0 : profile.getAverageScore());
        request.setTotalPoints(safeInteger(profile.getTotalPoints()));
        request.setTotalEvaluations(safeInteger(profile.getTotalEvaluations()));
        request.setPositiveEvaluations(safeInteger(profile.getPositiveEvaluations()));
        request.setCompletedProjects(safeInteger(profile.getCompletedProjects()));
        request.setEvaluatedAt(profile.getLastEvaluationAt() == null ? LocalDateTime.now() : profile.getLastEvaluationAt());
        return request;
    }

    private String buildAwardReason(Badge badge, RewardEvaluationSyncRequest request) {
        if (SCORE_CONDITION.equalsIgnoreCase(badge.getConditionType())) {
            return "Unlocked after reaching average score " + formatDecimal(request.getAverageScore());
        }
        if (POINTS_CONDITION.equalsIgnoreCase(badge.getConditionType())) {
            return "Unlocked after reaching " + request.getTotalPoints() + " cumulative points.";
        }
        return "Badge assigned by rewards engine.";
    }

    private String calculateLevel(RewardEvaluationSyncRequest request) {
        return calculateLevel(
                request.getAverageScore(),
                request.getCompletedProjects(),
                request.getPositiveEvaluations(),
                request.getTotalPoints()
        );
    }

    private String calculateLevel(Double averageScore,
                                  Integer completedProjectsValue,
                                  Integer positiveEvaluationsValue,
                                  Integer totalPointsValue) {
        double average = averageScore == null ? 0.0 : averageScore;
        int completedProjects = safeInteger(completedProjectsValue);
        int positiveEvaluations = safeInteger(positiveEvaluationsValue);
        int totalPoints = safeInteger(totalPointsValue);

        if (average >= 4.5 && positiveEvaluations >= 8
                && (completedProjects >= 10 || totalPoints >= 500)) {
            return "Niveau 4 - Elite Freelancer";
        }
        if (average >= 4.0 && positiveEvaluations >= 5
                && (completedProjects >= 6 || totalPoints >= 250)) {
            return "Niveau 3 - Top Performer";
        }
        if (average >= 3.0 && positiveEvaluations >= 2
                && (completedProjects >= 3 || totalPoints >= 100)) {
            return "Niveau 2 - Trusted Freelancer";
        }
        return "Niveau 1 - New Freelancer";
    }

    private int levelRank(String level) {
        if (level == null || level.isBlank()) {
            return 0;
        }
        if (level.startsWith("Niveau 4")) {
            return 4;
        }
        if (level.startsWith("Niveau 3")) {
            return 3;
        }
        if (level.startsWith("Niveau 2")) {
            return 2;
        }
        return 1;
    }

    private String findMostFrequentBadge(List<RewardHistory> history) {
        return history.stream()
                .filter(this::isBadgeAwardEvent)
                .collect(Collectors.groupingBy(RewardHistory::getRewardName, Collectors.counting()))
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("No badge assigned yet");
    }

    private List<TopFreelancerDTO> buildTopFreelancers(List<FreelancerRewardProfile> profiles) {
        Comparator<FreelancerRewardProfile> comparator = Comparator
                .comparing(FreelancerRewardProfile::getTotalPoints, Comparator.nullsLast(Integer::compareTo))
                .reversed()
                .thenComparing(Comparator.comparing(
                        FreelancerRewardProfile::getAverageScore,
                        Comparator.nullsLast(Double::compareTo)
                ).reversed())
                .thenComparing(Comparator.comparing(
                        FreelancerRewardProfile::getPositiveEvaluations,
                        Comparator.nullsLast(Integer::compareTo)
                ).reversed());

        return profiles.stream()
                .sorted(comparator)
                .limit(10)
                .map(this::toTopFreelancer)
                .toList();
    }

    private FreelancerRewardInsightDTO buildRewardInsight(FreelancerRewardProfile profile) {
        double averageScore = profile.getAverageScore() == null ? 0.0 : profile.getAverageScore();
        int totalPoints = safeInteger(profile.getTotalPoints());

        Badge nextScoreBadge = nextBadge(SCORE_CONDITION, averageScore);
        Badge nextPointsBadge = nextBadge(POINTS_CONDITION, totalPoints);

        List<RewardOpportunityDTO> opportunities = buildRewardOpportunities(profile, totalPoints);
        Optional<RewardOpportunityDTO> nextRecompense = opportunities.stream()
                .filter(opportunity -> RECOMPENSE_TYPE.equals(opportunity.getType()))
                .filter(opportunity -> !Boolean.TRUE.equals(opportunity.getAlreadyAwarded()))
                .filter(opportunity -> Boolean.TRUE.equals(opportunity.getAvailable()))
                .filter(opportunity -> !Boolean.TRUE.equals(opportunity.getEligible()))
                .sorted(Comparator.comparing(RewardOpportunityDTO::getRequiredValue, Comparator.nullsLast(Double::compareTo)))
                .findFirst();

        int eligibleRecompensesCount = (int) opportunities.stream()
                .filter(opportunity -> RECOMPENSE_TYPE.equals(opportunity.getType()))
                .filter(opportunity -> Boolean.TRUE.equals(opportunity.getEligible()))
                .count();
        int lockedRecompensesCount = (int) opportunities.stream()
                .filter(opportunity -> RECOMPENSE_TYPE.equals(opportunity.getType()))
                .filter(opportunity -> !Boolean.TRUE.equals(opportunity.getEligible()))
                .filter(opportunity -> !Boolean.TRUE.equals(opportunity.getAlreadyAwarded()))
                .count();
        int availableRecompensesCount = (int) opportunities.stream()
                .filter(opportunity -> RECOMPENSE_TYPE.equals(opportunity.getType()))
                .filter(opportunity -> Boolean.TRUE.equals(opportunity.getAvailable()))
                .count();

        FreelancerRewardInsightDTO insight = new FreelancerRewardInsightDTO();
        insight.setUserEmail(profile.getUserEmail());
        insight.setUserName(profile.getUserName());
        insight.setCurrentLevel(profile.getCurrentLevel());
        insight.setPerformanceStatus(calculatePerformanceStatus(profile));
        insight.setNextScoreBadge(nextScoreBadge == null ? null : nextScoreBadge.getName());
        insight.setScoreToNextBadge(nextScoreBadge == null
                ? 0.0
                : roundRemaining(safeDouble(nextScoreBadge.getConditionValue()) - averageScore));
        insight.setNextPointsBadge(nextPointsBadge == null ? null : nextPointsBadge.getName());
        insight.setPointsToNextBadge(nextPointsBadge == null
                ? 0
                : Math.max(0, safeDouble(nextPointsBadge.getConditionValue()).intValue() - totalPoints));
        insight.setNextRecompense(nextRecompense.map(RewardOpportunityDTO::getTitle).orElse(null));
        insight.setPointsToNextRecompense(nextRecompense
                .map(RewardOpportunityDTO::getRemainingValue)
                .map(Double::intValue)
                .orElse(0));
        insight.setEligibleRecompensesCount(eligibleRecompensesCount);
        insight.setLockedRecompensesCount(lockedRecompensesCount);
        insight.setAvailableRecompensesCount(availableRecompensesCount);
        insight.setOpportunities(opportunities);
        insight.setRecommendations(buildRecommendations(profile, nextScoreBadge, nextPointsBadge, nextRecompense));
        return insight;
    }

    private Badge nextBadge(String conditionType, double currentValue) {
        return badgeService.findActiveBadgesByCondition(conditionType)
                .stream()
                .filter(badge -> Boolean.TRUE.equals(badge.getAutoAssignable()))
                .filter(badge -> safeDouble(badge.getConditionValue()) > currentValue)
                .min(Comparator.comparing(Badge::getConditionValue, Comparator.nullsLast(Double::compareTo)))
                .orElse(null);
    }

    private List<RewardOpportunityDTO> buildRewardOpportunities(FreelancerRewardProfile profile, int totalPoints) {
        List<RewardOpportunityDTO> opportunities = new ArrayList<>();

        for (Recompense recompense : recompenseRepository.findByIsActiveTrue()) {
            int requiredPoints = safeInteger(recompense.getPointsRequired());
            boolean available = recompense.getStock() == null || recompense.getStock() == -1 || recompense.getStock() > 0;
            boolean alreadyAwarded = rewardHistoryRepository.existsByUserEmailAndRewardNameAndRewardTypeAndActionType(
                    profile.getUserEmail(),
                    recompense.getTitle(),
                    RECOMPENSE_TYPE,
                    AWARDED_ACTION
            );

            RewardOpportunityDTO opportunity = new RewardOpportunityDTO();
            opportunity.setType(RECOMPENSE_TYPE);
            opportunity.setTitle(recompense.getTitle());
            opportunity.setDescription(recompense.getDescription());
            opportunity.setCurrentValue((double) totalPoints);
            opportunity.setRequiredValue((double) requiredPoints);
            opportunity.setRemainingValue((double) Math.max(0, requiredPoints - totalPoints));
            opportunity.setEligible(totalPoints >= requiredPoints && available && !alreadyAwarded);
            opportunity.setAlreadyAwarded(alreadyAwarded);
            opportunity.setAvailable(available);
            opportunities.add(opportunity);
        }

        opportunities.sort(Comparator
                .comparing(RewardOpportunityDTO::getEligible, Comparator.nullsLast(Boolean::compareTo)).reversed()
                .thenComparing(RewardOpportunityDTO::getRequiredValue, Comparator.nullsLast(Double::compareTo)));

        return opportunities;
    }

    private String calculatePerformanceStatus(FreelancerRewardProfile profile) {
        double averageScore = profile.getAverageScore() == null ? 0.0 : profile.getAverageScore();
        int totalEvaluations = safeInteger(profile.getTotalEvaluations());
        int totalPoints = safeInteger(profile.getTotalPoints());

        if (totalEvaluations == 0) {
            return "WAITING_FOR_EVALUATIONS";
        }
        if (averageScore >= 4.5 && totalPoints >= 500) {
            return "ELITE_READY";
        }
        if (averageScore >= 4.0 && totalPoints >= 250) {
            return "PREMIUM_PROGRESS";
        }
        if (averageScore < 3.0) {
            return "NEEDS_ATTENTION";
        }
        return "PROGRESSING";
    }

    private List<String> buildRecommendations(FreelancerRewardProfile profile,
                                              Badge nextScoreBadge,
                                              Badge nextPointsBadge,
                                              Optional<RewardOpportunityDTO> nextRecompense) {
        List<String> recommendations = new ArrayList<>();
        double averageScore = profile.getAverageScore() == null ? 0.0 : profile.getAverageScore();
        int totalPoints = safeInteger(profile.getTotalPoints());

        if (nextScoreBadge != null) {
            recommendations.add("Improve the average score by "
                    + formatDecimal(roundRemaining(safeDouble(nextScoreBadge.getConditionValue()) - averageScore))
                    + " to unlock the badge " + nextScoreBadge.getName() + ".");
        } else {
            recommendations.add("All score badges are currently unlocked for this score level.");
        }

        if (nextPointsBadge != null) {
            recommendations.add("Earn "
                    + Math.max(0, safeDouble(nextPointsBadge.getConditionValue()).intValue() - totalPoints)
                    + " more points to unlock the badge " + nextPointsBadge.getName() + ".");
        }

        nextRecompense.ifPresent(opportunity -> recommendations.add("The next automatic reward is "
                + opportunity.getTitle()
                + "; "
                + opportunity.getRemainingValue().intValue()
                + " more points are required."));

        if ("NEEDS_ATTENTION".equals(calculatePerformanceStatus(profile))) {
            recommendations.add("Review the last client feedback before assigning high value rewards.");
        }

        return recommendations;
    }

    private TopFreelancerDTO toTopFreelancer(FreelancerRewardProfile profile) {
        TopFreelancerDTO dto = new TopFreelancerDTO();
        dto.setUserEmail(profile.getUserEmail());
        dto.setUserName(profile.getUserName());
        dto.setAverageScore(profile.getAverageScore());
        dto.setTotalPoints(profile.getTotalPoints());
        dto.setTotalEvaluations(profile.getTotalEvaluations());
        dto.setPositiveEvaluations(profile.getPositiveEvaluations());
        dto.setCompletedProjects(profile.getCompletedProjects());
        dto.setCurrentLevel(profile.getCurrentLevel());
        dto.setCurrentScoreBadge(profile.getCurrentScoreBadge());
        dto.setCurrentPointsBadge(profile.getCurrentPointsBadge());
        return dto;
    }

    private List<MonthlyRewardProgressDTO> buildMonthlyProgress(List<RewardHistory> history) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        Map<YearMonth, long[]> aggregate = new LinkedHashMap<>();

        List<RewardHistory> orderedHistory = new ArrayList<>(history);
        orderedHistory.sort(Comparator.comparing(RewardHistory::getEventDate));

        for (RewardHistory rewardHistory : orderedHistory) {
            if (!SCORE_BADGE_TYPE.equals(rewardHistory.getRewardType())
                    && !POINTS_BADGE_TYPE.equals(rewardHistory.getRewardType())) {
                continue;
            }

            YearMonth month = YearMonth.from(rewardHistory.getEventDate());
            long[] values = aggregate.computeIfAbsent(month, ignored -> new long[]{0L, 0L});
            if (AWARDED_ACTION.equals(rewardHistory.getActionType())) {
                values[0]++;
            } else if (REVOKED_ACTION.equals(rewardHistory.getActionType())) {
                values[1]++;
            }
        }

        return aggregate.entrySet().stream()
                .map(entry -> {
                    MonthlyRewardProgressDTO dto = new MonthlyRewardProgressDTO();
                    dto.setMonth(formatter.format(entry.getKey()));
                    dto.setAwardedCount(entry.getValue()[0]);
                    dto.setRevokedCount(entry.getValue()[1]);
                    return dto;
                })
                .toList();
    }

    private boolean isBadgeAwardEvent(RewardHistory history) {
        return AWARDED_ACTION.equals(history.getActionType())
                && (SCORE_BADGE_TYPE.equals(history.getRewardType()) || POINTS_BADGE_TYPE.equals(history.getRewardType()));
    }

    private LocalDateTime resolveEventDate(RewardEvaluationSyncRequest request) {
        return request.getEvaluatedAt() == null ? LocalDateTime.now() : request.getEvaluatedAt();
    }

    private boolean equalsIgnoreCase(String left, String right) {
        if (left == null && right == null) {
            return true;
        }
        if (left == null || right == null) {
            return false;
        }
        return left.equalsIgnoreCase(right);
    }

    private String formatDecimal(Double value) {
        return value == null ? "0.00" : String.format(Locale.US, "%.2f", value);
    }

    private String buildCertificateFileName(String email, String badgeName) {
        String sanitizedEmail = safe(email, "freelancer").replaceAll("[^a-zA-Z0-9._-]", "_");
        String sanitizedBadge = safe(badgeName, "badge").replaceAll("[^a-zA-Z0-9._-]", "_");
        return "certificate-" + sanitizedEmail + "-" + sanitizedBadge + ".pdf";
    }

    private int safeInteger(Integer value) {
        return value == null ? 0 : value;
    }

    private Double safeDouble(Double value) {
        return value == null ? 0.0 : value;
    }

    private Double roundRemaining(Double value) {
        double safeValue = value == null ? 0.0 : value;
        return Math.max(0.0, Math.round(safeValue * 100.0) / 100.0);
    }

    private String safe(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
